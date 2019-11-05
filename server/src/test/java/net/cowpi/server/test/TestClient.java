package net.cowpi.server.test;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.cowpi.crypto.*;
import net.cowpi.protobuf.CiphertextProto.*;
import net.cowpi.protobuf.MessageProto.*;
import net.cowpi.protobuf.MessageProto.CowpiMessage.MessageType;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.protobuf.UserProtos.PreKey;
import net.cowpi.protobuf.UserProtos.UploadPrekey;
import net.cowpi.protobuf.UserProtos.UserContainer;
import net.cowpi.server.config.ClientBindAddress;
import net.cowpi.server.config.ServerLongtermKeyPair;
import net.cowpi.server.config.ServerName;
import net.cowpi.util.FutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.inject.Inject;
import java.net.SocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;

@ClientScope
public class TestClient{
    private static final Logger logger = LoggerFactory.getLogger(TestClient.class);

    private Channel channel;

    private final Bootstrap bootstrap;

    private final LinkedBlockingQueue<CiphertextContainer> messages;

    private final CryptoEngine crypto;

    private final String remoteName;
    private final byte[] serverPublicKey;

    private String username = null;
    private KeyPair longtermKeyPair = null;
    private KeyRatchet keyRatchet = null;
    private List<EphemeralKeyPair> prekeys = new ArrayList<>();

    @Inject
    public TestClient(@ClientBindAddress SocketAddress serverAddress, @ClientWorkerGroup NioEventLoopGroup workerGroup,
                      LinkedBlockingQueue<CiphertextContainer> messages, TestClientInitializer initializer,
                      CryptoEngine crypto, @ServerName String remoteName, @ServerLongtermKeyPair KeyPair serverLongtermKeyPair) {
        this.messages = messages;
        this.crypto = crypto;
        this.remoteName = remoteName;
        this.serverPublicKey = serverLongtermKeyPair.getPublicKey();
        bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(initializer)
                .remoteAddress(serverAddress);
    }

    public CompletionStage<Void> connect(){
        ChannelFuture result = bootstrap.connect();
        channel = result.channel();

        return FutureUtil.toVoidCompletionStage(result);
    }

    public void register(String username) throws InterruptedException {
        this.username = username;

        longtermKeyPair = crypto.generateLongtermKeyPair();
        keyRatchet = crypto.generateKeyRatchet(username, longtermKeyPair, remoteName, serverPublicKey);

        RegisterUser.Builder inner = RegisterUser.newBuilder()
                .setUsername(username)
                .setLongtermKey(ByteString.copyFrom(longtermKeyPair.getPublicKey()));

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setRegisterUser(inner);

        send(container.build());

        getNextCiphertextContainer();
    }

    public CompletionStage<Void> disconnect(){
        return FutureUtil.toVoidCompletionStage(channel.close());
    }

    public void login() throws InterruptedException {

        EphemeralKeyPair ekp = keyRatchet.nextEphemeralKeyPair();

        Login.Builder login = Login.newBuilder()
                .setUsername(username)
                .setEphmeralPublicKey(ByteString.copyFrom(ekp.getPublicKey()));

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setLogin(login);

        send(container.build());
        CiphertextContainer result = getNextCiphertextContainer();
        EphemeralPublicKey next = new EphemeralPublicKey(0, result.getLogin().getEphmeralPublicKey().toByteArray());
        keyRatchet.setNextEphPublicKey(next);
    }

    public void uploadPrekeys() throws BadPaddingException, InterruptedException, InvalidKeyException,
            IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidProtocolBufferException {
        UploadPrekey.Builder upload = UploadPrekey.newBuilder();

        for(int i=0; i<10; i++){
            EphemeralKeyPair ekp = crypto.generateEphemeralKeyPair(i, longtermKeyPair);
            prekeys.add(ekp);
            PreKey.Builder prekeyBuilder = PreKey.newBuilder()
                    .setKeyId(ekp.getId())
                    .setPrekey(ByteString.copyFrom(ekp.getPublicKey()));
            upload.addPrekey(prekeyBuilder);
        }

        UserContainer.Builder container = UserContainer.newBuilder()
                .setUploadPrekey(upload);

        ServerContainer.Builder serverContainer = ServerContainer.newBuilder()
                .setUserContainer(container);
        send(serverContainer.build());
        getNextServerContainer();
    }

    public void sendSetup(long conversationId, String... users) throws InvalidKeyException, BadPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException {
        CowpiMessage.Builder setupBuilder = CowpiMessage.newBuilder()
                .setType(MessageType.SETUP)
                .setConversationId(conversationId)
                .putOesData("oes_1", OesCiphertext.newBuilder().build())
                .putOesData("oes_2", OesCiphertext.newBuilder().build());

        for(int i=0; i<users.length; i++){
                setupBuilder.putParticipantData(users[i], ParticipantData.newBuilder().build());
        }

        ServerContainer.Builder serverBuilder = ServerContainer.newBuilder()
                .setCowpiMessage(setupBuilder);

        send(serverBuilder.build());
    }

    public void sendSetupReceipt(String... users) throws BadPaddingException, InterruptedException, InvalidKeyException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidProtocolBufferException {
        CowpiMessage next = getNextServerContainer().getCowpiMessage();

        CowpiMessage.Builder builder = CowpiMessage.newBuilder()
                .setType(MessageType.RECIEPT)
                .setConversationId(next.getConversationId())
                .setPrevIndex(0)
                .putOesData("oes_1", OesCiphertext.newBuilder().build())
                .putOesData("oes_2", OesCiphertext.newBuilder().build());

        for(int i=0; i<users.length; i++){
            builder.putParticipantData(users[i], ParticipantData.newBuilder().build());
        }

        send(builder.build());
    }

    public void send(ServerContainer inner) throws IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {

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

    public void send(CiphertextContainer container){
        channel.writeAndFlush(container);
    }

    public CiphertextContainer getNextCiphertextContainer() throws InterruptedException {
        return messages.take();
    }

    public ServerContainer getNextServerContainer() throws InterruptedException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidProtocolBufferException {
        CiphertextContainer container = messages.take();

        Ciphertext innerCiphertext = container.getCiphertext();

        long keyId = innerCiphertext.getKeyId();
        byte[] key = keyRatchet.getNaxosDecryptionKey(keyId);

        byte[] plaintext = crypto.decrypt(key, innerCiphertext.getCiphertext().toByteArray());

        Plaintext innerPlaintext = Plaintext.parseFrom(plaintext);

        EphemeralPublicKey next = new EphemeralPublicKey(innerPlaintext.getNextKeyId(),
                innerPlaintext.getNextPubKey().toByteArray());

        keyRatchet.setNextEphPublicKey(next);

        return innerPlaintext.getInner();
    }

    public KeyPair getLongtermKeyPair(){
        return  longtermKeyPair;
    }

    public void send(CowpiMessage inner) throws InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {

        ServerContainer.Builder builder = ServerContainer.newBuilder()
                .setCowpiMessage(inner);

        send(builder.build());
    }
}
