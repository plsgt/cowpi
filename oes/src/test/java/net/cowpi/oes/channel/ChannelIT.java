package net.cowpi.oes.channel;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import net.cowpi.ServiceManager;
import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.KeyPair;
import net.cowpi.oes.DaggerTestComponent;
import net.cowpi.oes.TestComponent;
import net.cowpi.oes.TestOesConfig;
import net.cowpi.oes.test.TestRoutingChannel;
import net.cowpi.oes.test.TestRoutingServer;
import net.cowpi.oes.test.TestUser;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;
import net.cowpi.protobuf.CiphertextProto.OesLogin;
import net.cowpi.protobuf.MessageProto.*;
import net.cowpi.protobuf.MessageProto.CowpiMessage.MessageType;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.OesProto.OesPreKey;
import net.cowpi.protobuf.OesProto.OesUploadPrekeys;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class ChannelIT {
    private static final String CONFIG_FILE = "cowpi.oes.config.test";
    private static final Logger logger = LoggerFactory.getLogger(ChannelIT.class);

    private TestComponent component;
    private TestRoutingServer routingServer;
    private ServiceManager serviceManager;
    private TestOesConfig config;
    private CryptoEngine crypto;

    private TestOesConfig loadConfig() throws FileNotFoundException {
        String filename = System.getProperty(CONFIG_FILE);

        Yaml yaml = new Yaml(new Constructor(TestOesConfig.class));

        FileReader reader = new FileReader(filename);
        return yaml.load(reader);
    }


    @Before
    public void before() throws InterruptedException, FileNotFoundException {
        config = loadConfig();

        component = DaggerTestComponent.builder()
                .config(config)
                .build();

        component.flyway().clean();

        routingServer = component.testRoutingServer();

        serviceManager = component.oesServiceManager();

        crypto = component.crypto();

        routingServer.start();

        serviceManager.start().toCompletableFuture().join();
    }

    @After
    public void after() throws ExecutionException, InterruptedException {
        routingServer.shutdown();
        serviceManager.shutdown().toCompletableFuture().join();
    }

    @Test
    public void testChannelLogin() throws InterruptedException {
        TestRoutingChannel channel = component.channelPool().getChannel(0);
        CiphertextContainer container = channel.getNextCiphertextContainer();
        assertTrue(container.hasOesLogin());
        assertEquals("oes_1", container.getOesLogin().getOesName());
    }

    /**
     * Not a useful test at the moment. The test may finish before the oes client processes the message.
     */
    @Test
    public void testChannelLoginResponse() throws InterruptedException {

        TestRoutingChannel channel = component.channelPool().getChannel(0);
        channel.getNextCiphertextContainer();

        EphemeralKeyPair keyPair = component.crypto().generateEphemeralKeyPair(0, component.routingServerKeyPair());

        OesLogin.Builder login = OesLogin.newBuilder()
                .setEphemralPublicKey(ByteString.copyFrom(keyPair.getPublicKey()));

        CiphertextContainer.Builder container = CiphertextContainer.newBuilder()
                .setOesLogin(login);

        channel.send(container.build());
    }

    @Test
    public void testSetupConversation() throws InterruptedException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidProtocolBufferException {
        TestRoutingChannel channel = component.channelPool().getChannel(0);
        channel.handleLogin();
        OesUploadPrekeys prekeys = channel.requestPreKeys(1);

        CowpiMessage.Builder setupBuilder = CowpiMessage.newBuilder()
                .setType(MessageType.SETUP)
                .setAuthor("test_user_1")
                .setConversationId(0l)
                .addParticipant("test_user_1")
                .addParticipant("test_user_2")
                .putParticipantData("test_user_2", ParticipantData.newBuilder().build());

        OesPreKey prekey1 = prekeys.getOesPrekey(0);

        KeyPair keyPair1 = crypto.generateLongtermKeyPair();
        EphemeralKeyPair epk1 = crypto.generateEphemeralKeyPair(0, keyPair1);
        EphemeralKeyPair nextEpk1 = crypto.generateEphemeralKeyPair(1, keyPair1);

        KeyPair keyPair2 = crypto.generateLongtermKeyPair();
        EphemeralKeyPair epk2 = crypto.generateEphemeralKeyPair(0, keyPair2);

        OesPlaintext.Builder plaintext = OesPlaintext.newBuilder()
                .setKeyId(nextEpk1.getId())
                .setEphKey(ByteString.copyFrom(nextEpk1.getPublicKey()));

        byte[] encKey = crypto.getNaxosEncryptionKey("test_user_1", keyPair1, epk1,
                config.getOesConfig().getOesName(),
                component.oesKeyPair().getPublicKey(),
                prekey1.getPublicKey().toByteArray());

        byte[] ad = crypto.messageToAuthBytesOes(setupBuilder.build(), "test_user_1");

        byte[] ciphertext = crypto.encrypt(encKey, plaintext.build().toByteArray(), ad);

        OesCiphertext.Builder oesData1 = OesCiphertext.newBuilder()
                .setLongtermKey(ByteString.copyFrom(keyPair1.getPublicKey()))
                .setKeyId(prekey1.getKeyId())
                .setEphKey(ByteString.copyFrom(epk1.getPublicKey()))
                .setCiphertext(ByteString.copyFrom(ciphertext));

        OesCiphertext.Builder oesData2 = OesCiphertext.newBuilder()
                .setLongtermKey(ByteString.copyFrom(keyPair2.getPublicKey()))
                .setKeyId(epk2.getId())
                .setEphKey(ByteString.copyFrom(epk2.getPublicKey()));

        setupBuilder.putOesData("test_user_1", oesData1.build())
                .putOesData("test_user_2", oesData2.build());

        OesContainer.Builder container = OesContainer.newBuilder()
                .setCowpiMessage(setupBuilder);

        //Action
        channel.send(container.build());

        //Assert
        OesContainer next = channel.getNextOesContainer();
        assertTrue(next.hasCowpiMessage());
        CowpiMessage setup = next.getCowpiMessage();
        assertEquals(2, setup.getOesDataCount());
        assertEquals(1, setup.getParticipantDataCount());
        assertEquals("test_user_1", setup.getAuthor());
        assertEquals(0l, setup.getConversationId());
    }

    @Test
    public void testReceiptMessage() throws InterruptedException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, InvalidProtocolBufferException {
        TestRoutingChannel channel = component.channelPool().getChannel(0);
        channel.handleLogin();

        TestUser user1 = component.testUser();
        user1.setUsername("test_user_1");
        TestUser user2 = component.testUser();
        user2.setUsername("test_user_2");

        CowpiMessage setup = channel.setupConversation(0l, user1, user2);

        CowpiMessage.Builder receipt = CowpiMessage.newBuilder()
                .setType(MessageType.RECIEPT)
                .setAuthor("test_user_1")
                .setConversationId(0l)
                .setPrevIndex(0)
                .setIndex(1)
                .putParticipantData("test_user_2", ParticipantData.newBuilder().build());

        OesCiphertext oesCiphertext = setup.getOesDataMap().get("test_user_1");

        byte[] decKey = crypto.getNaxosDecryptionKey("test_user_1", user1.getKeyPair(), user1.getEphemeralKeyPair(oesCiphertext.getKeyId()),
                config.getOesConfig().getOesName(),
                component.oesKeyPair().getPublicKey(), oesCiphertext.getEphKey().toByteArray());

        byte[] ad = crypto.messageToAuthBytesOes(setup, "test_user_1");

        byte[] plaintext = crypto.decrypt(decKey, oesCiphertext.getCiphertext().toByteArray(), ad);

        OesPlaintext oesPlaintext = OesPlaintext.parseFrom(plaintext);

        EphemeralKeyPair nextEpk = user1.generateEphemerKeyPair();

        OesPlaintext.Builder plaintextBuilder = OesPlaintext.newBuilder()
                .setKeyId(nextEpk.getId())
                .setEphKey(ByteString.copyFrom(nextEpk.getPublicKey()));

        byte[] encKey = crypto.getNaxosEncryptionKey("test_user_1", user1.getKeyPair(), user1.getEphemeralKeyPair(1),
                config.getOesConfig().getOesName(),
                component.oesKeyPair().getPublicKey(), oesPlaintext.getEphKey().toByteArray());

        ad = crypto.messageToAuthBytesOes(receipt.build(), "test_user_1");

        byte[] ciphertext = crypto.encrypt(encKey, plaintextBuilder.build().toByteArray(), ad);

        OesCiphertext.Builder ciphertextBuilder = OesCiphertext.newBuilder()
                .setKeyId(oesPlaintext.getKeyId())
                .setCiphertext(ByteString.copyFrom(ciphertext));

        receipt.putOesData("test_user_1", ciphertextBuilder.build());

        OesContainer.Builder container = OesContainer.newBuilder()
                .setCowpiMessage(receipt);

        //Action
        channel.send(container.build());

        //Assert
        OesContainer result = channel.getNextOesContainer();
        assertTrue(result.hasCowpiMessage());
        CowpiMessage message = result.getCowpiMessage();
        assertEquals(MessageType.RECIEPT, message.getType());
    }

    @Test
    public void testMessage() throws InterruptedException, IllegalBlockSizeException, InvalidKeyException,
            InvalidAlgorithmParameterException, BadPaddingException, InvalidProtocolBufferException {
        TestRoutingChannel channel = component.channelPool().getChannel(0);
        channel.handleLogin();

        TestUser user1 = component.testUser();
        user1.setUsername("test_user_1");
        TestUser user2 = component.testUser();
        user2.setUsername("test_user_2");

        CowpiMessage setup = channel.setupConversation(0l, user1, user2);
        CowpiMessage receipt = channel.receipt(setup, user1, user2);

        CowpiMessage.Builder msg = CowpiMessage.newBuilder()
                .setType(MessageType.CONVERSATION)
                .setAuthor("test_user_1")
                .setConversationId(0l)
                .setCiphertext(ByteString.EMPTY)
                .setIndex(2)
                .putParticipantData("test_user_2", ParticipantData.newBuilder().build());

        OesCiphertext oesCiphertext = receipt.getOesDataMap().get("test_user_1");

        byte[] decKey = crypto.getNaxosDecryptionKey("test_user_1", user1.getKeyPair(), user1.getEphemeralKeyPair(oesCiphertext.getKeyId()),
                config.getOesConfig().getOesName(),
                component.oesKeyPair().getPublicKey(), user1.getLastRemotePubKey());

        byte[] ad = crypto.messageToAuthBytesOes(receipt, "test_user_1");

        byte[] plaintext = crypto.decrypt(decKey, oesCiphertext.getCiphertext().toByteArray(), ad);

        OesPlaintext oesPlaintext = OesPlaintext.parseFrom(plaintext);
        user1.setLastRemoteKeyId(oesPlaintext.getKeyId());
        user1.setLastRemotePubKey(oesPlaintext.getEphKey().toByteArray());

        EphemeralKeyPair nextEpk = user1.generateEphemerKeyPair();

        OesPlaintext.Builder plaintextBuilder = OesPlaintext.newBuilder()
                .setKeyId(nextEpk.getId())
                .setEphKey(ByteString.copyFrom(nextEpk.getPublicKey()));

        byte[] encKey = crypto.getNaxosEncryptionKey("test_user_1", user1.getKeyPair(), user1.getEphemeralKeyPair(oesCiphertext.getKeyId()),
                config.getOesConfig().getOesName(),
                component.oesKeyPair().getPublicKey(), user1.getLastRemotePubKey());

        ad = crypto.messageToAuthBytesOes(msg.build(), "test_user_1");

        byte[] ciphertext = crypto.encrypt(encKey, plaintextBuilder.build().toByteArray(), ad);

        OesCiphertext.Builder ciphertextBuilder = OesCiphertext.newBuilder()
                .setKeyId(user1.getLastRemoteKeyId())
                .setCiphertext(ByteString.copyFrom(ciphertext));

        msg.putOesData("test_user_1", ciphertextBuilder.build());

        OesContainer.Builder container = OesContainer.newBuilder()
                .setCowpiMessage(msg);

        //Action
        channel.send(container.build());

        //Assert
        OesContainer result = channel.getNextOesContainer();
        assertTrue(result.hasCowpiMessage());
    }

    @Test
    public void testUploadPrekeys() throws InterruptedException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidProtocolBufferException {
        TestRoutingChannel channel = component.channelPool().getChannel(0);
        channel.handleLogin();

        OesUploadPrekeys.Builder prekeys = OesUploadPrekeys.newBuilder()
                .setPrekeyCount(1000);

        OesContainer.Builder container = OesContainer.newBuilder()
                .setOesUploadPrekeys(prekeys);

        channel.send(container.build());

        OesContainer next = channel.getNextOesContainer();
        assertTrue(next.hasOesUploadPrekeys());
        OesUploadPrekeys tmp = next.getOesUploadPrekeys();
        assertEquals(1000, tmp.getOesPrekeyCount());
    }


}
