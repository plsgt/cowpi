package net.cowpi.server.user;

import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer.InnerCase;
import net.cowpi.protobuf.CiphertextProto.Login;
import net.cowpi.protobuf.CiphertextProto.RegisterUser;
import net.cowpi.server.*;
import net.cowpi.server.test.TestClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class ClientChannelIT extends BaseIT {
    private static final Logger logger = LoggerFactory.getLogger(ClientChannelIT.class);

    @Test
    public void testRegisterUser() throws InterruptedException {
        //Assign
        TestClient user1 = newTestClient();
        user1.connect().toCompletableFuture().join();

        RegisterUser.Builder inner = RegisterUser.newBuilder()
                .setUsername("test_user_1");

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setRegisterUser(inner);

        //Action
        user1.send(container.build());


        //Assert
        CiphertextContainer next = user1.getNextCiphertextContainer();
        assertEquals(next.getInnerCase(), InnerCase.REGISTER_USER);
        assertTrue(next.getRegisterUser().getSuccess());

    }

    @Test
    public void testLoginUser() throws InterruptedException {
        //Assign
        TestClient user1 = newTestClient();
        user1.connect().toCompletableFuture().join();

        user1.register("test_user_1");

        Login.Builder login = Login.newBuilder()
                .setUsername("test_user_1");

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setLogin(login);

        //Action
        user1.send(container.build());

        //Assert
        CiphertextContainer next = user1.getNextCiphertextContainer();
        assertTrue(next.hasLogin());
        assertNotNull(next.getLogin().getEphmeralPublicKey());
    }
}
