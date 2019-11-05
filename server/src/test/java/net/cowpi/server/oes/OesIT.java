package net.cowpi.server.oes;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer.InnerCase;
import net.cowpi.protobuf.CiphertextProto.OesLogin;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.OesProto.OesPreKey;
import net.cowpi.protobuf.OesProto.OesUploadPrekeys;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.server.BaseIT;
import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.KeyPair;
import net.cowpi.server.test.TestOes;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Map;

import static org.junit.Assert.*;

public class OesIT extends BaseIT {
    private static final Logger logger = LoggerFactory.getLogger(OesIT.class);

//    @Test
//    public void temp(){
//        KeyPair oes1 = component.cryptoEngine().generateLongtermKeyPair();
//        KeyPair oes2 = component.cryptoEngine().generateLongtermKeyPair();
//
//        logger.info("oes 1 priv: {}", ByteBufUtil.hexDump(oes1.getPrivateKey()));
//        logger.info("oes 1 pub: {}", ByteBufUtil.hexDump(oes1.getPublicKey()));
//
//        logger.info("oes 2 priv: {}", ByteBufUtil.hexDump(oes2.getPrivateKey()));
//        logger.info("oes 2 pub: {}", ByteBufUtil.hexDump(oes2.getPublicKey()));
//    }

    @Test
    public void testLoginOes() throws InterruptedException {
        //Assign
        TestOes oes1 = newTestOes();
        oes1.connect().toCompletableFuture().join();

        Map<String, KeyPair> oesKeyPairs = component.oesKeyPairs();

        EphemeralKeyPair keyPair = component.cryptoEngine().generateEphemeralKeyPair(0, oesKeyPairs.get("oes_1"));

        OesLogin.Builder login = OesLogin.newBuilder()
                .setOesName("oes_1")
                .setEphemralPublicKey(ByteString.copyFrom(keyPair.getPublicKey()));

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setOesLogin(login);

        //Action
        oes1.send(container.build());

        //Assert
        CiphertextContainer next = oes1.getNextCiphertextContainer();
        assertEquals(next.getInnerCase(), InnerCase.OES_LOGIN);
        assertNotNull(next.getOesLogin().getEphemralPublicKey());
    }

    @Test
    public void testUploadOesPrekeys() throws InterruptedException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, InvalidProtocolBufferException {
        //Assign
        TestOes oes1 = newTestOes();
        oes1.connect().toCompletableFuture().join();

        Map<String, KeyPair> oesKeyPairs = component.oesKeyPairs();
        oes1.setOes("oes_1", oesKeyPairs.get("oes_1"));
        oes1.login();

        OesUploadPrekeys.Builder oesUploadPrekeys = OesUploadPrekeys.newBuilder();

        for(int i=0; i<10; i++){
            CryptoEngine crypto = component.cryptoEngine();
            EphemeralKeyPair ekp = crypto.generateEphemeralKeyPair(i, oes1.getLongtermKeyPair());
            OesPreKey.Builder prekeyBuilder = OesPreKey.newBuilder()
                    .setKeyId(ekp.getId())
                    .setPublicKey(ByteString.copyFrom(ekp.getPublicKey()));
            oesUploadPrekeys.addOesPrekey(prekeyBuilder);
        }

        OesContainer.Builder oesContainer = OesContainer.newBuilder()
                .setOesUploadPrekeys(oesUploadPrekeys);

        ServerContainer.Builder container = ServerContainer.newBuilder()
                .setOesContainer(oesContainer);

        //Action
        oes1.send(container.build());

        //Assert
        ServerContainer next = oes1.getNextServerContainer();
    }
}
