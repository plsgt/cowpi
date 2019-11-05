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
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.OesProto.OesPreKey;
import net.cowpi.protobuf.OesProto.OesUploadPrekeys;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.server.config.OesBindAddress;
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
public class TestOes {
    private static final Logger logger = LoggerFactory.getLogger(TestOes.class);

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
    public TestOes(@OesBindAddress SocketAddress serverAddress, @ClientWorkerGroup NioEventLoopGroup workerGroup,
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

    public void setOes(String name, KeyPair longtermKeyPair){
        username = name;
        this.longtermKeyPair = longtermKeyPair;
    }

    public CompletionStage<Void> connect(){
        ChannelFuture result = bootstrap.connect();
        channel = result.channel();

        return FutureUtil.toVoidCompletionStage(result);
    }

    public CompletionStage<Void> disconnect(){
        return FutureUtil.toVoidCompletionStage(channel.close());
    }

    public void login() throws InterruptedException {

        keyRatchet = crypto.generateKeyRatchet(username, longtermKeyPair, remoteName, serverPublicKey);
        EphemeralKeyPair ekp = keyRatchet.nextEphemeralKeyPair();

        OesLogin.Builder login = OesLogin.newBuilder()
                .setOesName(username)
                .setEphemralPublicKey(ByteString.copyFrom(ekp.getPublicKey()));

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setOesLogin(login);

        send(container.build());
        CiphertextContainer result = getNextCiphertextContainer();
        EphemeralPublicKey next = new EphemeralPublicKey(0, result.getOesLogin().getEphemralPublicKey().toByteArray());
        keyRatchet.setNextEphPublicKey(next);
    }

    public void uploadPrekeys() throws BadPaddingException, InterruptedException, InvalidKeyException,
            IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidProtocolBufferException {
        OesUploadPrekeys.Builder upload = OesUploadPrekeys.newBuilder();

        for(int i=0; i<10; i++){
            EphemeralKeyPair ekp = crypto.generateEphemeralKeyPair(i, longtermKeyPair);
            prekeys.add(ekp);
            OesPreKey.Builder prekeyBuilder = OesPreKey.newBuilder()
                    .setKeyId(ekp.getId())
                    .setPublicKey(ByteString.copyFrom(ekp.getPublicKey()));
            upload.addOesPrekey(prekeyBuilder);
        }

        OesContainer.Builder container = OesContainer.newBuilder()
                .setOesUploadPrekeys(upload);

        ServerContainer.Builder serverContainer = ServerContainer.newBuilder()
                .setOesContainer(container);
        send(serverContainer.build());
        getNextServerContainer();
    }

    public void sendOesSetup() throws BadPaddingException, InterruptedException, InvalidKeyException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidProtocolBufferException {
        CowpiMessage setupMessage = getNextServerContainer().getOesContainer().getCowpiMessage();

        OesContainer.Builder oesContainer = OesContainer.newBuilder()
                .setCowpiMessage(setupMessage);

        send(oesContainer.build());
    }

    public void sendOesConfirmation() throws BadPaddingException, InterruptedException, InvalidKeyException,
            IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidProtocolBufferException {
        OesContainer container = getNextServerContainer().getOesContainer();
        send(container);
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

    public void send(OesContainer oesContainer) throws InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        ServerContainer.Builder builder = ServerContainer.newBuilder()
                .setOesContainer(oesContainer);

        send(builder.build());
    }
}
