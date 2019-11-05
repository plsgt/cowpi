package net.cowpi.server.tcp;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.cowpi.crypto.*;
import net.cowpi.protobuf.CiphertextProto.*;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.server.config.ServerLongtermKeyPair;
import net.cowpi.server.config.ServerName;
import net.cowpi.server.engine.UserEngine;
import net.cowpi.server.jooq.tables.CowpiUser;
import net.cowpi.server.jooq.tables.records.CowpiUserRecord;
import net.cowpi.server.tcp.user.TcpComponent;
import net.cowpi.server.tcp.user.TcpComponent.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.inject.Inject;
import javax.inject.Provider;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class CiphertextHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChannelHandler.class);


    private enum AuthenticationState {
        INITIAL, STARTED, SUCCESS, FAILURE;
    }

    private volatile AuthenticationState state = AuthenticationState.INITIAL;


    private final String serverName;
    private final KeyPair longtermKeyPair;

    private final UserEngine userEngine;
    private final Provider<TcpComponent.Builder> componentBuilder;
    private final CryptoEngine crypto;


    private KeyRatchet keyRatchet = null;

    @Inject
    public CiphertextHandler(@ServerName  String serverName, @ServerLongtermKeyPair KeyPair longtermKeyPair,
                             UserEngine userEngine, Provider<Builder> componentBuilder, CryptoEngine crypto) {
        this.serverName = serverName;
        this.longtermKeyPair = longtermKeyPair;
        this.userEngine = userEngine;
        this.componentBuilder = componentBuilder;
        this.crypto = crypto;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        if(obj instanceof CiphertextContainer){
            CiphertextContainer container = (CiphertextContainer) obj;

            switch (container.getInnerCase()){
                case REGISTER_USER:
                    handleRegisterUser(ctx, container.getRegisterUser());
                    break;
                case VERIFY_USER:
                    handleVerifyUser(ctx, container.getVerifyUser());
                    break;
                case LOGIN:
                    handleLogin(ctx, container.getLogin());
                    break;
                case CIPHERTEXT:
                    handleCiphertext(ctx, container.getCiphertext());
                    break;
            }
        }
    }

    private void handleRegisterUser(ChannelHandlerContext ctx, RegisterUser registerUser){
        if(state != AuthenticationState.INITIAL){
            throw new IllegalStateException();
        }
        state = AuthenticationState.STARTED;

        userEngine.addUser(registerUser);
        state = AuthenticationState.INITIAL;

        RegisterUser.Builder inner = RegisterUser.newBuilder()
                .setSuccess(true);

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setRegisterUser(inner);

        ctx.writeAndFlush(container.build());
    }

    private void handleVerifyUser(ChannelHandlerContext ctx, VerifyUser verifyUser) {
        if(state != AuthenticationState.INITIAL){
            throw new IllegalStateException();
        }
        state = AuthenticationState.STARTED;

        CowpiUserRecord record = userEngine.getUserRecord(verifyUser.getUsername());

        VerifyUser.Builder inner = VerifyUser.newBuilder()
                .setSuccess(true);

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setVerifyUser(inner);

        ctx.writeAndFlush(container.build());

        setupPipeline(ctx, verifyUser.getUsername(), record.getId());
        state = AuthenticationState.SUCCESS;


    }

    private void handleLogin(ChannelHandlerContext ctx, Login login){
        if(state != AuthenticationState.INITIAL){
            throw new IllegalStateException();
        }
        state = AuthenticationState.STARTED;

        CowpiUserRecord record = userEngine.getUserRecord(login.getUsername());

        keyRatchet = crypto.generateKeyRatchet(serverName, longtermKeyPair, record.getUsername(), record.getLongtermKey());
        keyRatchet.setNextEphPublicKey(new EphemeralPublicKey(0, login.getEphmeralPublicKey().toByteArray()));

        EphemeralKeyPair next = keyRatchet.nextEphemeralKeyPair();

        Login.Builder inner = Login.newBuilder()
                .setEphmeralPublicKey(ByteString.copyFrom(next.getPublicKey()));

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setLogin(inner);

        ctx.writeAndFlush(container.build());

        setupPipeline(ctx, login.getUsername(), record.getId());

        state = AuthenticationState.SUCCESS;
    }

    private void setupPipeline(ChannelHandlerContext ctx, String username, long userId){
        TcpServerHandler serverHandler = componentBuilder.get()
                .username(username)
                .userId(userId)
                .build()
                .serverHandler();
        ctx.pipeline().addLast(serverHandler);
    }

    private void handleCiphertext(ChannelHandlerContext ctx, Ciphertext ciphertext) throws InvalidProtocolBufferException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        if(state != AuthenticationState.SUCCESS){
            throw new IllegalStateException();
        }

        long keyId = ciphertext.getKeyId();
        byte[] key = keyRatchet.getNaxosDecryptionKey(keyId);
        byte[] plaintext = crypto.decrypt(key, ciphertext.getCiphertext().toByteArray());

        Plaintext container = Plaintext.parseFrom(plaintext);

        EphemeralPublicKey nextKey = new EphemeralPublicKey(container.getNextKeyId(), container.getNextPubKey().toByteArray());
        keyRatchet.setNextEphPublicKey(nextKey);

        ctx.fireChannelRead(container.getInner());
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
        if(state != AuthenticationState.SUCCESS){
            throw new IllegalStateException();
        }
        if(obj instanceof ServerContainer){
            ServerContainer inner = (ServerContainer) obj;


            long keyId = keyRatchet.getLastPublicKey().getId();
            byte[] key = keyRatchet.getNaxosEncryptionKey();

            EphemeralKeyPair nextKeyPair = keyRatchet.nextEphemeralKeyPair();

            Plaintext.Builder plaintextBuilder = Plaintext.newBuilder()
                    .setNextKeyId(nextKeyPair.getId())
                    .setNextPubKey(ByteString.copyFrom(nextKeyPair.getPublicKey()))
                    .setInner(inner);

            byte[] ciphertxt = crypto.encrypt(key, plaintextBuilder.build().toByteArray());

            Ciphertext.Builder ciphertextBuilder = Ciphertext.newBuilder()
                    .setKeyId(keyId)
                    .setCiphertext(ByteString.copyFrom(ciphertxt));

            CiphertextContainer container = CiphertextContainer.newBuilder()
                    .setCiphertext(ciphertextBuilder)
                    .build();

            ctx.write(container, promise);
        }
    }
}
