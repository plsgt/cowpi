package net.cowpi.oes.test;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import net.cowpi.crypto.*;
import net.cowpi.oes.config.OesKeyPair;
import net.cowpi.oes.config.OesName;
import net.cowpi.oes.config.RouterServiceName;
import net.cowpi.protobuf.CiphertextProto.Ciphertext;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;
import net.cowpi.protobuf.CiphertextProto.OesLogin;
import net.cowpi.protobuf.CiphertextProto.Plaintext;
import net.cowpi.protobuf.MessageProto.*;
import net.cowpi.protobuf.MessageProto.CowpiMessage.MessageType;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.OesProto.OesPreKey;
import net.cowpi.protobuf.OesProto.OesUploadPrekeys;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.inject.Inject;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class TestRoutingChannel {
    private static final Logger logger = LoggerFactory.getLogger(TestRoutingChannel.class);

    private final String routerName;
    private final KeyPair longtermKeyPair;

    private final String oesName;
    private final byte[] oesPublicKey;

    private final LinkedBlockingQueue<CiphertextContainer> messageQueue = new LinkedBlockingQueue<>();
    private final CryptoEngine crypto;


    private final CompletableFuture<Void> channelFuture = new CompletableFuture<>();
    private volatile Channel channel;

    private KeyRatchet keyRatchet;

    @Inject
    public TestRoutingChannel(@RouterServiceName String routerName, @TestRoutingServerKeyPair KeyPair longtermKeyPair,
                              @OesName String oesName, @OesKeyPair KeyPair oesKeyPair, CryptoEngine crypto){
        this.routerName = routerName;
        this.longtermKeyPair = longtermKeyPair;

        this.oesName = oesName;
        this.oesPublicKey = oesKeyPair.getPublicKey();
        this.crypto = crypto;
    }

    public void setChannel(Channel channel){
        this.channel = channel;
        channel.writeAndFlush(messageQueue);
        channelFuture.complete(null);
    }

    public void await(){
        channelFuture.join();
    }

    public CiphertextContainer getNextCiphertextContainer() throws InterruptedException {
        return messageQueue.take();
    }

    public void send(CiphertextContainer container) {
        channel.writeAndFlush(container);
    }

    public void handleLogin() throws InterruptedException {
        OesLogin login = getNextCiphertextContainer().getOesLogin();
        EphemeralPublicKey epk = new EphemeralPublicKey(0, login.getEphemralPublicKey().toByteArray());

        keyRatchet = crypto.generateKeyRatchet(routerName, longtermKeyPair, oesName, oesPublicKey);
        keyRatchet.setNextEphPublicKey(epk);

        EphemeralKeyPair ekp = keyRatchet.nextEphemeralKeyPair();

        OesLogin.Builder builder = OesLogin.newBuilder()
                .setEphemralPublicKey(ByteString.copyFrom(ekp.getPublicKey()));

        CiphertextContainer container = CiphertextContainer.newBuilder()
                .setOesLogin(builder)
                .build();

        send(container);
    }

    public void send(OesContainer oesContainer) throws InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        ServerContainer.Builder server = ServerContainer.newBuilder()
                .setOesContainer(oesContainer);

        send(server.build());
    }

    public void send(ServerContainer inner) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        long keyId = keyRatchet.getLastPublicKey().getId();
        byte[] key = keyRatchet.getNaxosEncryptionKey();

        EphemeralKeyPair next = keyRatchet.nextEphemeralKeyPair();

        Plaintext plaintext = Plaintext.newBuilder()
                .setInner(inner)
                .setNextKeyId(next.getId())
                .setNextPubKey(ByteString.copyFrom(next.getPublicKey()))
                .build();

        byte[] ciphertextBytes = crypto.encrypt(key, plaintext.toByteArray());
        Ciphertext.Builder ciphertext = Ciphertext.newBuilder()
                .setKeyId(keyId)
                .setCiphertext(ByteString.copyFrom(ciphertextBytes));

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setCiphertext(ciphertext);

        send(container.build());
    }

    public OesContainer getNextOesContainer() throws InterruptedException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidProtocolBufferException {
        CiphertextContainer container = getNextCiphertextContainer();

        Ciphertext innerCiphertext = container.getCiphertext();

        long keyId = innerCiphertext.getKeyId();
        byte[] key = keyRatchet.getNaxosDecryptionKey(keyId);

        byte[] plaintext = crypto.decrypt(key, innerCiphertext.getCiphertext().toByteArray());

        Plaintext innerPlaintext = Plaintext.parseFrom(plaintext);

        EphemeralPublicKey next = new EphemeralPublicKey(innerPlaintext.getNextKeyId(),
                innerPlaintext.getNextPubKey().toByteArray());

        keyRatchet.setNextEphPublicKey(next);

        return innerPlaintext.getInner().getOesContainer();
    }

    public OesUploadPrekeys requestPreKeys(int count) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidProtocolBufferException, InterruptedException {
        OesUploadPrekeys.Builder prekeys = OesUploadPrekeys.newBuilder()
                .setPrekeyCount(count);

        OesContainer.Builder container = OesContainer.newBuilder()
                .setOesUploadPrekeys(prekeys);

        send(container.build());

        return getNextOesContainer().getOesUploadPrekeys();
    }

    public CowpiMessage setupConversation(long convId, TestUser... users) throws BadPaddingException, InterruptedException, InvalidKeyException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidProtocolBufferException {

        CowpiMessage.Builder setupBuilder = CowpiMessage.newBuilder()
                .setType(MessageType.SETUP)
                .setAuthor(users[0].getUsername())
                .setConversationId(convId)
                .addParticipant(users[0].getUsername());

        for(int i=1; i<users.length; i++){
            EphemeralKeyPair epk = users[i].generateEphemerKeyPair();

            OesCiphertext.Builder oesData = OesCiphertext.newBuilder()
                    .setLongtermKey(ByteString.copyFrom(users[i].getKeyPair().getPublicKey()))
                    .setKeyId(epk.getId())
                    .setEphKey(ByteString.copyFrom(epk.getPublicKey()));

            setupBuilder.addParticipant(users[i].getUsername())
                    .putParticipantData(users[i].getUsername(), ParticipantData.newBuilder().build())
                    .putOesData(users[i].getUsername(), oesData.build());
        }

        OesUploadPrekeys prekeys = requestPreKeys(1);
        OesPreKey prekey1 = prekeys.getOesPrekey(0);

        EphemeralKeyPair epk1 = users[0].generateEphemerKeyPair();
        EphemeralKeyPair nextEpk1 = users[0].generateEphemerKeyPair();

        OesPlaintext.Builder plaintext = OesPlaintext.newBuilder()
                .setKeyId(nextEpk1.getId())
                .setEphKey(ByteString.copyFrom(nextEpk1.getPublicKey()));

        byte[] encKey = crypto.getNaxosEncryptionKey(users[0].getUsername(), users[0].getKeyPair(), epk1,
                oesName, oesPublicKey, prekey1.getPublicKey().toByteArray());

        byte[] ad = crypto.messageToAuthBytesOes(setupBuilder.build(), users[0].getUsername());

        byte[] ciphertext = crypto.encrypt(encKey, plaintext.build().toByteArray(), ad);

        OesCiphertext.Builder oesData1 = OesCiphertext.newBuilder()
                .setLongtermKey(ByteString.copyFrom(users[0].getKeyPair().getPublicKey()))
                .setKeyId(prekey1.getKeyId())
                .setEphKey(ByteString.copyFrom(epk1.getPublicKey()))
                .setCiphertext(ByteString.copyFrom(ciphertext));

        setupBuilder.putOesData(users[0].getUsername(), oesData1.build());

        OesContainer.Builder container = OesContainer.newBuilder()
                .setCowpiMessage(setupBuilder);

        send(container.build());

        return getNextOesContainer().getCowpiMessage();
    }

    public CowpiMessage receipt(CowpiMessage setup, TestUser... users) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidProtocolBufferException, InterruptedException {

        CowpiMessage.Builder receipt = CowpiMessage.newBuilder()
                .setType(MessageType.RECIEPT)
                .setAuthor(users[0].getUsername())
                .setConversationId(setup.getConversationId())
                .setPrevIndex(0)
                .setIndex(1);

        for(int i=1; i<users.length; i++){
                receipt.putParticipantData(users[i].getUsername(), ParticipantData.newBuilder().build());
        }

        TestUser user1 = users[0];

        OesCiphertext oesCiphertext = setup.getOesDataMap().get(user1.getUsername());

        byte[] decKey = crypto.getNaxosDecryptionKey(user1.getUsername(), user1.getKeyPair(), user1.getEphemeralKeyPair(oesCiphertext.getKeyId()),
                oesName, oesPublicKey, oesCiphertext.getEphKey().toByteArray());

        byte[] ad = crypto.messageToAuthBytesOes(setup, user1.getUsername());

        byte[] plaintext = crypto.decrypt(decKey, oesCiphertext.getCiphertext().toByteArray(), ad);

        OesPlaintext oesPlaintext = OesPlaintext.parseFrom(plaintext);
        user1.setLastRemoteKeyId(oesPlaintext.getKeyId());
        user1.setLastRemotePubKey(oesPlaintext.getEphKey().toByteArray());

        EphemeralKeyPair nextEpk = user1.generateEphemerKeyPair();

        OesPlaintext.Builder plaintextBuilder = OesPlaintext.newBuilder()
                .setKeyId(nextEpk.getId())
                .setEphKey(ByteString.copyFrom(nextEpk.getPublicKey()));

        byte[] encKey = crypto.getNaxosEncryptionKey(user1.getUsername(), user1.getKeyPair(), user1.getEphemeralKeyPair(oesCiphertext.getKeyId()),
                oesName, oesPublicKey, user1.getLastRemotePubKey());

        ad = crypto.messageToAuthBytesOes(receipt.build(), user1.getUsername());

        byte[] ciphertext = crypto.encrypt(encKey, plaintextBuilder.build().toByteArray(), ad);

        OesCiphertext.Builder ciphertextBuilder = OesCiphertext.newBuilder()
                .setKeyId(user1.getLastRemoteKeyId())
                .setCiphertext(ByteString.copyFrom(ciphertext));

        receipt.putOesData(user1.getUsername(), ciphertextBuilder.build());

        OesContainer.Builder container = OesContainer.newBuilder()
                .setCowpiMessage(receipt);

        send(container.build());
        return getNextOesContainer().getCowpiMessage();
    }
}
