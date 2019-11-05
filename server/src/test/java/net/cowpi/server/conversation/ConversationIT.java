package net.cowpi.server.conversation;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import net.cowpi.protobuf.MessageProto.*;
import net.cowpi.protobuf.MessageProto.CowpiMessage.MessageType;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.server.BaseIT;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConversationIT extends BaseIT {
    private static final Logger logger = LoggerFactory.getLogger(ConversationIT.class);

    private TestClient setupUser(String username) throws InterruptedException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, InvalidProtocolBufferException {

        TestClient testClient = newTestClient();
        testClient.connect().toCompletableFuture().join();
        testClient.register(username);
        testClient.login();
        testClient.uploadPrekeys();

        return testClient;
    }

    private TestOes setupOes(String name) throws InterruptedException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, InvalidProtocolBufferException {
        Map<String, KeyPair> oesKeys = component.oesKeyPairs();

        TestOes oes = newTestOes();
        oes.connect().toCompletableFuture().join();
        oes.setOes(name, oesKeys.get(name));
        oes.login();
        oes.uploadPrekeys();
        return oes;
    }

    @Test
    public void testSetupConversation() throws InterruptedException, InvalidProtocolBufferException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        //Arrange
        TestOes oes1 = setupOes("oes_1");
        TestOes oes2 = setupOes("oes_2");


        TestClient user1 = setupUser("test_user_1");

        TestClient user2 = setupUser("test_user_2");

        CowpiMessage.Builder setupBuilder = CowpiMessage.newBuilder()
                .setType(MessageType.SETUP)
                .setConversationId(0L)
                .putParticipantData("test_user_2", ParticipantData.newBuilder().build())
                .putOesData("oes_1", OesCiphertext.newBuilder().build())
                .putOesData("oes_2", OesCiphertext.newBuilder().build());

        ServerContainer.Builder serverBuilder = ServerContainer.newBuilder()
                .setCowpiMessage(setupBuilder);

        //Action
        user1.send(serverBuilder.build());

        //Assert
        ServerContainer next = oes1.getNextServerContainer();
        assertTrue(next.hasOesContainer());
        assertTrue(next.getOesContainer().hasCowpiMessage());
        assertEquals("test_user_1", next.getOesContainer().getCowpiMessage().getAuthor());
        assertTrue(next.getOesContainer().getCowpiMessage().containsParticipantData("test_user_2"));
        assertTrue(next.getOesContainer().getCowpiMessage().containsOesData("test_user_1"));
        assertTrue(next.getOesContainer().getCowpiMessage().containsOesData("test_user_2"));

        next = oes2.getNextServerContainer();
        assertTrue(next.hasOesContainer());
        assertTrue(next.getOesContainer().hasCowpiMessage());
        assertEquals("test_user_1", next.getOesContainer().getCowpiMessage().getAuthor());
        assertTrue(next.getOesContainer().getCowpiMessage().containsParticipantData("test_user_2"));
        assertTrue(next.getOesContainer().getCowpiMessage().containsOesData("test_user_1"));
        assertTrue(next.getOesContainer().getCowpiMessage().containsOesData("test_user_2"));

    }

    @Test
    public void testOesSetupReply() throws InterruptedException, InvalidProtocolBufferException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        //Arrange
        TestOes oes1 = setupOes("oes_1");
        TestOes oes2 = setupOes("oes_2");


        TestClient user1 = setupUser("test_user_1");

        TestClient user2 = setupUser("test_user_2");


        user1.sendSetup(0L, "test_user_2");

        //Action
        CowpiMessage setupMessage = oes1.getNextServerContainer().getOesContainer().getCowpiMessage();

        OesContainer.Builder oesContainer = OesContainer.newBuilder()
                .setCowpiMessage(setupMessage);

        oes1.send(oesContainer.build());

        setupMessage = oes2.getNextServerContainer().getOesContainer().getCowpiMessage();
        oesContainer = OesContainer.newBuilder()
                .setCowpiMessage(setupMessage);

        oes2.send(oesContainer.build());

        //Assert

        ServerContainer next = user1.getNextServerContainer();
        assertTrue(next.hasCowpiMessage());
        assertEquals("test_user_1", next.getCowpiMessage().getAuthor());
        assertTrue(next.getCowpiMessage().containsOesData("oes_1"));
        assertTrue(next.getCowpiMessage().containsOesData("oes_2"));
        assertEquals(0, next.getCowpiMessage().getParticipantDataCount());

        next = user2.getNextServerContainer();
        assertTrue(next.hasCowpiMessage());
        assertEquals(MessageType.SETUP, next.getCowpiMessage().getType());
        assertEquals("test_user_1", next.getCowpiMessage().getAuthor());
        assertTrue(next.getCowpiMessage().containsOesData("oes_1"));
        assertTrue(next.getCowpiMessage().containsOesData("oes_2"));
        assertTrue(next.getCowpiMessage().containsParticipantData("test_user_1"));
        assertEquals(1, next.getCowpiMessage().getParticipantDataCount());

    }

    @Test
    public void testSendReceipt() throws InterruptedException, InvalidProtocolBufferException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        //Arrange
        TestOes oes1 = setupOes("oes_1");
        TestOes oes2 = setupOes("oes_2");


        TestClient user1 = setupUser("test_user_1");

        TestClient user2 = setupUser("test_user_2");


        user1.sendSetup(0L, "test_user_2");


        oes1.sendOesSetup();
        oes2.sendOesSetup();

        //Action
        CowpiMessage next = user1.getNextServerContainer().getCowpiMessage();

        CowpiMessage.Builder builder = CowpiMessage.newBuilder()
                .setType(MessageType.RECIEPT)
                .setConversationId(next.getConversationId())
                .setPrevIndex(0)
                .putParticipantData("test_user_2", ParticipantData.newBuilder().build())
                .putOesData("oes_1", OesCiphertext.newBuilder().build())
                .putOesData("oes_2", OesCiphertext.newBuilder().build());

        user1.send(builder.build());

        //Assert
        ServerContainer result = oes1.getNextServerContainer();
        assertTrue(result.hasOesContainer());
        assertTrue(result.getOesContainer().hasCowpiMessage());
        CowpiMessage tmp = result.getOesContainer().getCowpiMessage();
        assertEquals("test_user_1", tmp.getAuthor());
        assertEquals(1, tmp.getIndex());
        assertEquals(0, tmp.getPrevIndex());
        assertEquals(1, tmp.getParticipantDataCount());
        assertTrue(tmp.containsParticipantData("test_user_2"));
        assertEquals(2, tmp.getOesDataCount());
        assertTrue(tmp.containsOesData("test_user_1"));
        assertTrue(tmp.containsOesData("test_user_2"));

        result = oes2.getNextServerContainer();
        assertTrue(result.hasOesContainer());
        assertTrue(result.getOesContainer().hasCowpiMessage());
        tmp = result.getOesContainer().getCowpiMessage();
        assertEquals("test_user_1", tmp.getAuthor());
        assertEquals(1, tmp.getIndex());
        assertEquals(0, tmp.getPrevIndex());
        assertEquals(1, tmp.getParticipantDataCount());
        assertTrue(tmp.containsParticipantData("test_user_2"));
        assertEquals(2, tmp.getOesDataCount());
        assertTrue(tmp.containsOesData("test_user_1"));
        assertTrue(tmp.containsOesData("test_user_2"));

    }

    @Test
    public void testOesReceiptReply() throws InterruptedException, InvalidProtocolBufferException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        //Arrange
        TestOes oes1 = setupOes("oes_1");
        TestOes oes2 = setupOes("oes_2");


        TestClient user1 = setupUser("test_user_1");

        TestClient user2 = setupUser("test_user_2");


        user1.sendSetup(0L, "test_user_2");


        oes1.sendOesSetup();
        oes2.sendOesSetup();

        user1.sendSetupReceipt("test_user_2");

        //Action
        OesContainer next = oes1.getNextServerContainer().getOesContainer();
        oes1.send(next);

        next = oes2.getNextServerContainer().getOesContainer();
        oes2.send(next);


        //Assert
        ServerContainer result = user1.getNextServerContainer();
        assertTrue(result.hasCowpiMessage());
        assertEquals(MessageType.RECIEPT, result.getCowpiMessage().getType());
        CowpiMessage tmp = result.getCowpiMessage();
        assertEquals("test_user_1", tmp.getAuthor());
        assertEquals(1, tmp.getIndex());
        assertEquals(0, tmp.getPrevIndex());
        assertEquals(0, tmp.getParticipantDataCount());
        assertEquals(2, tmp.getOesDataCount());
        assertTrue(tmp.containsOesData("oes_1"));
        assertTrue(tmp.containsOesData("oes_2"));

        user2.getNextServerContainer(); // discard setup message
        result = user2.getNextServerContainer();
        assertTrue(result.hasCowpiMessage());
        tmp = result.getCowpiMessage();
        assertEquals(MessageType.RECIEPT, tmp.getType());
        assertEquals("test_user_1", tmp.getAuthor());
        assertEquals(1, tmp.getIndex());
        assertEquals(0, tmp.getPrevIndex());
        assertEquals(1, tmp.getParticipantDataCount());
        assertTrue(tmp.containsParticipantData("test_user_1"));
        assertEquals(2, tmp.getOesDataCount());
        assertTrue(tmp.containsOesData("oes_1"));
        assertTrue(tmp.containsOesData("oes_2"));
    }

    @Test
    public void testSendMessage() throws InterruptedException, InvalidProtocolBufferException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        //Arrange
        TestOes oes1 = setupOes("oes_1");
        TestOes oes2 = setupOes("oes_2");


        TestClient user1 = setupUser("test_user_1");

        TestClient user2 = setupUser("test_user_2");


        user1.sendSetup(0L, "test_user_2");


        oes1.sendOesConfirmation();
        oes2.sendOesConfirmation();

        user1.sendSetupReceipt("test_user_2");

        oes1.sendOesConfirmation();
        oes2.sendOesConfirmation();

        user1.getNextServerContainer();
        user2.getNextServerContainer();

        //Action
        CowpiMessage.Builder conversationMessage = CowpiMessage.newBuilder()
                .setType(MessageType.CONVERSATION)
                .setConversationId(0L)
                .setIndex(2)
                .setCiphertext(ByteString.copyFrom("here".getBytes()))
                .putParticipantData("test_user_2", ParticipantData.newBuilder().build())
                .putOesData("oes_1", OesCiphertext.newBuilder().build())
                .putOesData("oes_2", OesCiphertext.newBuilder().build());

        user1.send(conversationMessage.build());

        //Assert
        ServerContainer next = oes1.getNextServerContainer();
        assertTrue(next.hasOesContainer());
        assertTrue(next.getOesContainer().hasCowpiMessage());
        CowpiMessage msg = next.getOesContainer().getCowpiMessage();
        assertEquals(MessageType.CONVERSATION, msg.getType());
        assertEquals(0L, msg.getConversationId());
        assertEquals(2, msg.getIndex());
        assertEquals("test_user_1", msg.getAuthor());
        assertEquals(ByteString.copyFrom("here".getBytes()), msg.getCiphertext());
        assertEquals(1, msg.getParticipantDataCount());
        assertTrue(msg.containsParticipantData("test_user_2"));
        assertEquals(2, msg.getOesDataCount());
        assertTrue(msg.containsOesData("test_user_1"));
        assertTrue(msg.containsOesData("test_user_2"));

        next = oes2.getNextServerContainer();
        assertTrue(next.hasOesContainer());
        assertTrue(next.getOesContainer().hasCowpiMessage());
        msg = next.getOesContainer().getCowpiMessage();
        assertEquals(MessageType.CONVERSATION, msg.getType());
        assertEquals(0L, msg.getConversationId());
        assertEquals(2, msg.getIndex());
        assertEquals("test_user_1", msg.getAuthor());
        assertEquals(ByteString.copyFrom("here".getBytes()), msg.getCiphertext());
        assertEquals(1, msg.getParticipantDataCount());
        assertTrue(msg.containsParticipantData("test_user_2"));
        assertEquals(2, msg.getOesDataCount());
        assertTrue(msg.containsOesData("test_user_1"));
        assertTrue(msg.containsOesData("test_user_2"));
    }

    @Test
    public void testOesMessageReply() throws InterruptedException, InvalidProtocolBufferException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        //Arrange
        TestOes oes1 = setupOes("oes_1");
        TestOes oes2 = setupOes("oes_2");


        TestClient user1 = setupUser("test_user_1");

        TestClient user2 = setupUser("test_user_2");


        user1.sendSetup(0L, "test_user_2");


        oes1.sendOesConfirmation();
        oes2.sendOesConfirmation();

        user1.sendSetupReceipt("test_user_2");

        oes1.sendOesConfirmation();
        oes2.sendOesConfirmation();

        user1.getNextServerContainer();
        user2.getNextServerContainer();
        user2.getNextServerContainer();

        CowpiMessage.Builder conversationMessage = CowpiMessage.newBuilder()
                .setType(MessageType.CONVERSATION)
                .setConversationId(0L)
                .setIndex(2)
                .setCiphertext(ByteString.copyFrom("here".getBytes()))
                .putParticipantData("test_user_2", ParticipantData.newBuilder().build())
                .putOesData("oes_1", OesCiphertext.newBuilder().build())
                .putOesData("oes_2", OesCiphertext.newBuilder().build());

        user1.send(conversationMessage.build());

        //Action

        oes1.sendOesConfirmation();
        oes2.sendOesConfirmation();


        //Assert
        ServerContainer result = user1.getNextServerContainer();
        assertTrue(result.hasCowpiMessage());
        CowpiMessage tmp = result.getCowpiMessage();
        assertEquals(MessageType.CONVERSATION, tmp.getType());
        assertEquals("test_user_1", tmp.getAuthor());
        assertEquals(2, tmp.getIndex());
        assertEquals(ByteString.copyFrom("here".getBytes()), tmp.getCiphertext());
        assertEquals(0, tmp.getParticipantDataCount());
        assertEquals(2, tmp.getOesDataCount());
        assertTrue(tmp.containsOesData("oes_1"));
        assertTrue(tmp.containsOesData("oes_2"));

        result = user2.getNextServerContainer();
        assertTrue(result.hasCowpiMessage());
        tmp = result.getCowpiMessage();
        assertEquals(MessageType.CONVERSATION, tmp.getType());
        assertEquals("test_user_1", tmp.getAuthor());
        assertEquals(ByteString.copyFrom("here".getBytes()), tmp.getCiphertext());
        assertEquals(2, tmp.getIndex());
        assertEquals(1, tmp.getParticipantDataCount());
        assertTrue(tmp.containsParticipantData("test_user_1"));
        assertEquals(2, tmp.getOesDataCount());
        assertTrue(tmp.containsOesData("oes_1"));
        assertTrue(tmp.containsOesData("oes_2"));
    }
}
