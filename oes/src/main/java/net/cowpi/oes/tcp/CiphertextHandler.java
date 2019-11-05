package net.cowpi.oes.tcp;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.cowpi.crypto.*;
import net.cowpi.oes.config.OesKeyPair;
import net.cowpi.oes.config.OesName;
import net.cowpi.oes.config.RouterLongterPublicKey;
import net.cowpi.oes.config.RouterServiceName;
import net.cowpi.protobuf.CiphertextProto.Ciphertext;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;
import net.cowpi.protobuf.CiphertextProto.OesLogin;
import net.cowpi.protobuf.CiphertextProto.Plaintext;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.inject.Inject;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class CiphertextHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(CiphertextHandler.class);

    private final String oesName;
    private final KeyPair longtermKeyPair;
    private final String routerName;
    private final byte[] routerLongtermPublicKey;
    private final CryptoEngine crypto;

    enum AuthState {
        INITIAL,
        SUCESS
    }

    private volatile AuthState state = AuthState.INITIAL;

    private volatile KeyRatchet keyRatchet;

    @Inject
    public CiphertextHandler(@OesName String oesName, @OesKeyPair KeyPair longtermKeyPair,
                             @RouterServiceName String routerName,
                             @RouterLongterPublicKey  byte[] routerLongtermPublicKey, CryptoEngine crypto) {
        this.oesName = oesName;
        this.longtermKeyPair = longtermKeyPair;
        this.routerName = routerName;
        this.routerLongtermPublicKey = routerLongtermPublicKey;
        this.crypto = crypto;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        keyRatchet = crypto.generateKeyRatchet(oesName, longtermKeyPair, routerName, routerLongtermPublicKey);
        EphemeralKeyPair ekp = keyRatchet.nextEphemeralKeyPair();

        OesLogin.Builder builder = OesLogin.newBuilder()
                .setOesName(oesName)
                .setEphemralPublicKey(ByteString.copyFrom(ekp.getPublicKey()));

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setOesLogin(builder);

        ctx.writeAndFlush(container.build());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        if(obj instanceof CiphertextContainer){
            CiphertextContainer container = (CiphertextContainer) obj;

            switch (container.getInnerCase()){
                case OES_LOGIN:
                    handleOesLogin(ctx, container.getOesLogin());
                    break;
                case CIPHERTEXT:
                    handleCiphertext(ctx, container.getCiphertext());
                    break;
            }
        }
    }

    private void handleCiphertext(ChannelHandlerContext ctx, Ciphertext ciphertext) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidProtocolBufferException {
        if(state != AuthState.SUCESS){
            throw new IllegalStateException();
        }

        long keyId = ciphertext.getKeyId();
        byte[] key = keyRatchet.getNaxosDecryptionKey(keyId);
        byte[] plaintext = crypto.decrypt(key, ciphertext.getCiphertext().toByteArray());

        Plaintext container = Plaintext.parseFrom(plaintext);

        EphemeralPublicKey nextKey = new EphemeralPublicKey(container.getNextKeyId(), container.getNextPubKey().toByteArray());
        keyRatchet.setNextEphPublicKey(nextKey);

        ctx.fireChannelRead(container.getInner().getOesContainer());
    }

    private void handleOesLogin(ChannelHandlerContext ctx, OesLogin oesLogin) {
        if(state != AuthState.INITIAL){
            throw new IllegalStateException();
        }

        EphemeralPublicKey next = new EphemeralPublicKey(0, oesLogin.getEphemralPublicKey().toByteArray());
        keyRatchet.setNextEphPublicKey(next);

        state = AuthState.SUCESS;
        ctx.fireUserEventTriggered(oesLogin);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
        if(state != AuthState.SUCESS){
            throw new IllegalStateException();
        }
        if(obj instanceof OesContainer){
            OesContainer oesContainer = (OesContainer) obj;

            ServerContainer.Builder inner = ServerContainer.newBuilder()
                    .setOesContainer(oesContainer);


            long keyId = keyRatchet.getLastPublicKey().getId();
            byte[] key = keyRatchet.getNaxosEncryptionKey();

            EphemeralKeyPair nextKeyPair = keyRatchet.nextEphemeralKeyPair();

            Plaintext.Builder plaintextBuilder = Plaintext.newBuilder()
                    .setNextKeyId(nextKeyPair.getId())
                    .setNextPubKey(ByteString.copyFrom(nextKeyPair.getPublicKey()))
                    .setInner(inner.build());

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Caught Exception", cause);
    }
}
