package net.cowpi.server.user;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.protobuf.UserProtos.FetchPrekey;
import net.cowpi.protobuf.UserProtos.PreKey;
import net.cowpi.protobuf.UserProtos.UploadPrekey;
import net.cowpi.protobuf.UserProtos.UserContainer;
import net.cowpi.server.BaseIT;
import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.KeyPair;
import net.cowpi.server.test.TestClient;
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

public class UserIT extends BaseIT {
    private static final Logger logger = LoggerFactory.getLogger(UserIT.class);

    @Test
    public void testUploadPrekeys() throws InterruptedException, InvalidKeyException, BadPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, InvalidProtocolBufferException {
        //Assign
        TestClient user1 = newTestClient();
        user1.connect().toCompletableFuture().join();
        user1.register("test_user_1");
        user1.login();

        UploadPrekey.Builder upload = UploadPrekey.newBuilder();

        for(int i=0; i<10; i++){
            CryptoEngine crypto = component.cryptoEngine();
            EphemeralKeyPair ekp = crypto.generateEphemeralKeyPair(i, user1.getLongtermKeyPair());
            PreKey.Builder prekeyBuilder = PreKey.newBuilder()
                    .setKeyId(ekp.getId())
                    .setPrekey(ByteString.copyFrom(ekp.getPublicKey()));
            upload.addPrekey(prekeyBuilder);
        }

        UserContainer.Builder container = UserContainer.newBuilder()
                .setUploadPrekey(upload);

        ServerContainer.Builder serverContainer = ServerContainer.newBuilder()
                .setUserContainer(container);

        //Action
        user1.send(serverContainer.build());

        //Assert
        ServerContainer result = user1.getNextServerContainer();
    }


    @Test
    public void testFetchPrekeys() throws InterruptedException, InvalidKeyException, BadPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, InvalidProtocolBufferException {
        //Assign
        TestClient user1 = newTestClient();
        user1.connect().toCompletableFuture().join();
        user1.register("test_user_1");
        user1.login();

        user1.uploadPrekeys();

        TestClient user2 = newTestClient();
        user2.connect().toCompletableFuture().join();
        user2.register("test_user_2");
        user2.login();

        Map<String, KeyPair> oesKeys = component.oesKeyPairs();

        TestOes oes1 = newTestOes();
        oes1.connect().toCompletableFuture().join();
        oes1.setOes("oes_1", oesKeys.get("oes_1"));
        oes1.login();
        oes1.uploadPrekeys();

        TestOes oes2 = newTestOes();
        oes2.connect().toCompletableFuture().join();
        oes2.setOes("oes_2", oesKeys.get("oes_2"));
        oes2.login();
        oes2.uploadPrekeys();
        FetchPrekey.Builder fetch = FetchPrekey.newBuilder()
                .addUsers("test_user_1");

        UserContainer.Builder container = UserContainer.newBuilder()
                .setFetchPrekey(fetch);

        ServerContainer.Builder serverContainer = ServerContainer.newBuilder()
                .setUserContainer(container);

        //Action
        user2.send(serverContainer.build());

        //Assert
        ServerContainer result = user2.getNextServerContainer();
        assertTrue(result.hasUserContainer());
        assertTrue(result.getUserContainer().hasFetchPrekey());
        assertTrue(result.getUserContainer().getFetchPrekey().containsKeys("test_user_1"));
    }

}
