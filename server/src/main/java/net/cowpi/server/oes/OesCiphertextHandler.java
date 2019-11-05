package net.cowpi.server.oes;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.cowpi.crypto.*;
import net.cowpi.protobuf.CiphertextProto.*;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.server.config.OesPublicKeys;
import net.cowpi.server.config.ServerLongtermKeyPair;
import net.cowpi.server.config.ServerName;
import net.cowpi.server.engine.UserEngine;
import net.cowpi.server.jooq.tables.records.OesServiceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.inject.Inject;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Map;

public class OesCiphertextHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(OesCiphertextHandler.class);

    private enum OesState {
        INITIAL,
        STARTED,
        SUCCESSUl
    }

    private volatile OesState state = OesState.INITIAL;
    private volatile KeyRatchet keyRatchet;

    private final String serverName;
    private final KeyPair longtermKeyPair;
    private final Map<String, byte[]> oesLongtermPublicKeys;
    private final UserEngine userEngine;
    private final CryptoEngine crypto;

    @Inject
    public OesCiphertextHandler(@ServerName String serverName, @ServerLongtermKeyPair KeyPair longtermKeyPair,
                                @OesPublicKeys Map<String, byte[]> oesLongtermPublicKeys, UserEngine userEngine,
                                CryptoEngine crypto) {

        this.serverName = serverName;
        this.longtermKeyPair = longtermKeyPair;
        this.oesLongtermPublicKeys = oesLongtermPublicKeys;
        this.userEngine = userEngine;
        this.crypto = crypto;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        if(obj instanceof CiphertextContainer){
            CiphertextContainer container = (CiphertextContainer) obj;
            switch(container.getInnerCase()){
                case OES_LOGIN:
                    handleOesLogin(ctx, container.getOesLogin());
                    break;
                case CIPHERTEXT:
                    handleCiphertext(ctx, container.getCiphertext());
                    break;
            }
        }
    }

    private void handleOesLogin(ChannelHandlerContext ctx, OesLogin login){
        if(state != OesState.INITIAL){
            throw new IllegalStateException();
        }
        state = OesState.STARTED;

        OesServiceRecord record = userEngine.getOesRecord(login.getOesName());
        OesLoginEvent event = new OesLoginEvent(record.getId());
        state = OesState.SUCCESSUl;

        keyRatchet = crypto.generateKeyRatchet(serverName, longtermKeyPair, record.getOesName(), oesLongtermPublicKeys.get(record.getOesName()));
        keyRatchet.setNextEphPublicKey(new EphemeralPublicKey(0, login.getEphemralPublicKey().toByteArray()));

        EphemeralKeyPair next = keyRatchet.nextEphemeralKeyPair();

        OesLogin.Builder inner = OesLogin.newBuilder()
                .setEphemralPublicKey(ByteString.copyFrom(next.getPublicKey()));

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setOesLogin(inner);

        ctx.writeAndFlush(container.build());

        ctx.fireUserEventTriggered(event);
    }

    public void handleCiphertext(ChannelHandlerContext ctx, Ciphertext ciphertext) throws IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException,
            InvalidProtocolBufferException {
        if(state != OesState.SUCCESSUl){
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
        if(state != OesState.SUCCESSUl){
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
