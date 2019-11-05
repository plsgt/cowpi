package net.cowpi.server.engine;

import com.google.protobuf.ByteString;
import net.cowpi.protobuf.MessageProto.*;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.server.config.OesPublicKeys;
import net.cowpi.server.config.RouterDslContext;
import net.cowpi.server.jooq.tables.records.*;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep2;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static net.cowpi.server.jooq.Tables.*;

public class CowpiEngine {
    private static final Logger logger = LoggerFactory.getLogger(CowpiEngine.class);

    private final DSLContext dslContext;
    private final MessageBroker clientBroker;
    private final OesBroker oesBroker;
    private final int oesCount;

    @Inject
    CowpiEngine(@RouterDslContext DSLContext dslContext, final MessageBroker clientBroker, final OesBroker oesBroker,
                @OesPublicKeys final Map<String, byte[]> oesPublicKeys) {
        this.dslContext = dslContext;
        this.clientBroker = clientBroker;
        this.oesBroker = oesBroker;
        oesCount = oesPublicKeys.size();
    }

    public void setupConversation(CowpiMessage setup) {
        Map<Long, OesContainer> result = dslContext.transactionResult(config -> {
            DSLContext inner = DSL.using(config);

            ConversationMessageRecord conversationMessageRecord = inner.selectFrom(CONVERSATION_MESSAGE)
                    .where(CONVERSATION_MESSAGE.CONVERSATION_ID.eq(setup.getConversationId()))
                    .limit(1)
                    .fetchAny();

            if(conversationMessageRecord != null){
                throw new IllegalStateException("Conversation already exists.");
            }

            conversationMessageRecord = inner.insertInto(CONVERSATION_MESSAGE)
                    .columns(CONVERSATION_MESSAGE.CONVERSATION_ID, CONVERSATION_MESSAGE.MESSAGE_INDEX, CONVERSATION_MESSAGE.MESSAGE)
                    .values(setup.getConversationId(), 0L, setup.toByteArray())
                    .returning()
                    .fetchOne();

            Result<CowpiUserRecord> records = inner.selectFrom(COWPI_USER)
                    .where(COWPI_USER.USERNAME.in(setup.getParticipantDataMap().keySet()))
                    .fetch();

            Map<Long, OesContainer> oesMessages = new HashMap<>();

            Result<OesServiceRecord> oesServices = inner.selectFrom(OES_SERVICE)
                    .fetch();

            for(OesServiceRecord record : oesServices) {
                CowpiMessage.Builder builder = setup.toBuilder()
                        .clearOesData()
                        .putOesData(setup.getAuthor(), setup.getOesDataMap().get(record.getOesName()));

                for (CowpiUserRecord userRecord : records) {
                    if (!setup.getAuthor().equals(record.getOesName())) {
                        PrekeyRecord prekeyRecord = inner.selectFrom(PREKEY)
                                .where(PREKEY.USER_ID.eq(record.getId()))
                                .limit(1)
                                .fetchOne();

                        OesCiphertext.Builder oesCiphertext = OesCiphertext.newBuilder()
                                .setKeyId(prekeyRecord.getKeyId())
                                .setLongtermKey(ByteString.copyFrom(userRecord.getLongtermKey()))
                                .setEphKey(ByteString.copyFrom(prekeyRecord.getPrekey()));

                        builder.putOesData(userRecord.getUsername(), oesCiphertext.build());
                    }
                }

                OesContainer container = OesContainer.newBuilder()
                        .setCowpiMessage(builder)
                        .build();

                oesMessages.put(record.getId(), container);

                inner.insertInto(OES_MESSAGE)
                        .columns(OES_MESSAGE.OES_SERVICE, OES_MESSAGE.CONVERSATION_MESSAGE, OES_MESSAGE.OUTGOING)
                        .values(record.getId(), conversationMessageRecord.getId(), container.toByteArray())
                        .execute();
            }

            return oesMessages;
        });

        oesBroker.sendMessage(result);
    }

    public void handleOesMessage(final long oesId, final CowpiMessage message) {
        ConversationMessageRecord conversationMessageRecord = dslContext.transactionResult(config -> {
            DSLContext inner = DSL.using(config);

            ConversationMessageRecord tmp = inner.update(CONVERSATION_MESSAGE)
                    .set(CONVERSATION_MESSAGE.OES_RESPONSE_COUNT, CONVERSATION_MESSAGE.OES_RESPONSE_COUNT.add(1))
                    .where(CONVERSATION_MESSAGE.CONVERSATION_ID.eq(message.getConversationId())
                            .and(CONVERSATION_MESSAGE.MESSAGE_INDEX.eq(message.getIndex())))
                    .returning()
                    .fetchOne();

            inner.update(OES_MESSAGE)
                    .set(OES_MESSAGE.MESSAGE, message.toByteArray())
                    .where(OES_MESSAGE.OES_SERVICE.eq(oesId)
                            .and(OES_MESSAGE.CONVERSATION_MESSAGE.eq(tmp.getId())))
                    .execute();
            return tmp;
        });

        if (conversationMessageRecord.getOesResponseCount() < oesCount) {
            return;
        }

        Map<Long, CowpiMessage> result = dslContext.transactionResult(config -> {
            DSLContext inner = DSL.using(config);

            Map<Long, CowpiMessage> messages = new HashMap<>();

            Result<Record3<Long, String, byte[]>> oesMessageRecords = inner.select(OES_SERVICE.ID, OES_SERVICE.OES_NAME, OES_MESSAGE.MESSAGE)
                    .from(OES_MESSAGE)
                    .join(OES_SERVICE)
                    .on(OES_MESSAGE.OES_SERVICE.eq(OES_SERVICE.ID))
                    .where(OES_MESSAGE.CONVERSATION_MESSAGE.eq(conversationMessageRecord.getId()))
                    .orderBy(OES_MESSAGE.ID.desc())
                    .fetch();

            CowpiMessage main = CowpiMessage.parseFrom(conversationMessageRecord.getMessage());

            Map<String, CowpiMessage> oesMessages = new HashMap<>();

            for (Record3<Long, String, byte[]> record : oesMessageRecords) {
                oesMessages.put(record.component2(), CowpiMessage.parseFrom(record.component3()));
            }

            InsertValuesStep2<MailboxRecord, Long, byte[]> mailboxInsert = inner.insertInto(MAILBOX)
                    .columns(MAILBOX.USER_TO, MAILBOX.MESSAGE);

            for (Map.Entry<String, ParticipantData> entry : main.getParticipantDataMap().entrySet()) {
                CowpiMessage.Builder builder = message.toBuilder()
                        .clearParticipantData()
                        .clearOesData()
                        .putParticipantData(main.getAuthor(), entry.getValue());

                for (Map.Entry<String, CowpiMessage> tmp : oesMessages.entrySet()) {
                    builder.putOesData(tmp.getKey(), tmp.getValue().getOesDataMap().get(entry.getKey()));
                }

                CowpiUserRecord userRecord = inner.selectFrom(COWPI_USER)
                        .where(COWPI_USER.USERNAME.eq(entry.getKey()))
                        .fetchOne();

                messages.put(userRecord.getId(), builder.build());
                mailboxInsert.values(userRecord.getId(), builder.build().toByteArray());
            }

            CowpiMessage.Builder builder = message.toBuilder()
                    .clearParticipantData()
                    .clearOesData();

            for (Map.Entry<String, CowpiMessage> tmp : oesMessages.entrySet()) {
                builder.putOesData(tmp.getKey(), tmp.getValue().getOesDataMap().get(main.getAuthor()));
            }

            CowpiUserRecord userRecord = inner.selectFrom(COWPI_USER)
                    .where(COWPI_USER.USERNAME.eq(main.getAuthor()))
                    .fetchOne();

            messages.put(userRecord.getId(), builder.build());
            mailboxInsert.values(userRecord.getId(), builder.build().toByteArray());

            mailboxInsert.execute();

            return messages;

        });
        clientBroker.sendMessages(result);
    }

    public void handleReceipt(CowpiMessage receipt) {
        Map<Long, OesContainer> result = dslContext.transactionResult(config -> {
            DSLContext inner = DSL.using(config);

            long lastIndex = inner.select(CONVERSATION_MESSAGE.MESSAGE_INDEX)
                    .from(CONVERSATION_MESSAGE)
                    .where(CONVERSATION_MESSAGE.CONVERSATION_ID.eq(receipt.getConversationId()))
                    .orderBy(CONVERSATION_MESSAGE.MESSAGE_INDEX.desc())
                    .limit(1)
                    .fetchOne().value1();

            CowpiMessage tmp = receipt.toBuilder()
                    .setIndex(lastIndex + 1)
                    .build();

            long messageId = inner.insertInto(CONVERSATION_MESSAGE)
                    .columns(CONVERSATION_MESSAGE.CONVERSATION_ID, CONVERSATION_MESSAGE.MESSAGE_INDEX, CONVERSATION_MESSAGE.MESSAGE)
                    .values(receipt.getConversationId(), lastIndex + 1, tmp.toByteArray())
                    .returning(CONVERSATION_MESSAGE.ID)
                    .fetchOne()
                    .value1();

            Result<CowpiUserRecord> records = inner.selectFrom(COWPI_USER)
                    .where(COWPI_USER.USERNAME.in(receipt.getParticipantDataMap().keySet()))
                    .fetch();

            Map<Long, OesContainer> oesMessages = new HashMap<>();

            Result<OesServiceRecord> oesServices = inner.selectFrom(OES_SERVICE)
                    .fetch();

            for (OesServiceRecord record : oesServices) {

                CowpiMessage.Builder builder = tmp.toBuilder()
                        .clearOesData()
                        .putOesData(receipt.getAuthor(), receipt.getOesDataMap().get(record.getOesName()));

                for (CowpiUserRecord userRecord : records) {
                    builder.putOesData(userRecord.getUsername(), OesCiphertext.newBuilder().build());
                }

                OesContainer container = OesContainer.newBuilder()
                        .setCowpiMessage(builder)
                        .build();

                oesMessages.put(record.getId(), container);

                inner.insertInto(OES_MESSAGE)
                        .columns(OES_MESSAGE.OES_SERVICE, OES_MESSAGE.CONVERSATION_MESSAGE, OES_MESSAGE.OUTGOING)
                        .values(record.getId(), messageId, container.toByteArray())
                        .execute();
            }

            return oesMessages;
        });

        oesBroker.sendMessage(result);
    }

    public void handleConversationMessage(CowpiMessage conversationMessage) {
        logger.info("User message");
        Map<Long, OesContainer> result = dslContext.transactionResult(config -> {
            long start = System.currentTimeMillis();
            DSLContext inner = DSL.using(config);

            long lastIndex = inner.select(CONVERSATION_MESSAGE.MESSAGE_INDEX)
                    .from(CONVERSATION_MESSAGE)
                    .where(CONVERSATION_MESSAGE.CONVERSATION_ID.eq(conversationMessage.getConversationId()))
                    .orderBy(CONVERSATION_MESSAGE.MESSAGE_INDEX.desc())
                    .limit(1)
                    .fetchOne().value1();

            if (lastIndex + 1 != conversationMessage.getIndex()) {
                throw new IllegalStateException();
            }

            long messageId = inner.insertInto(CONVERSATION_MESSAGE)
                    .columns(CONVERSATION_MESSAGE.CONVERSATION_ID, CONVERSATION_MESSAGE.MESSAGE_INDEX, CONVERSATION_MESSAGE.MESSAGE)
                    .values(conversationMessage.getConversationId(), conversationMessage.getIndex(), conversationMessage.toByteArray())
                    .returning(CONVERSATION_MESSAGE.ID)
                    .fetchOne()
                    .value1();

            Result<CowpiUserRecord> records = inner.selectFrom(COWPI_USER)
                    .where(COWPI_USER.USERNAME.in(conversationMessage.getParticipantDataMap().keySet()))
                    .fetch();

            Map<Long, OesContainer> oesMessages = new HashMap<>();

            Result<OesServiceRecord> oesServices = inner.selectFrom(OES_SERVICE)
                    .fetch();

            for (OesServiceRecord record : oesServices) {

                CowpiMessage.Builder builder = conversationMessage.toBuilder()
                        .clearOesData()
                        .putOesData(conversationMessage.getAuthor(), conversationMessage.getOesDataMap().get(record.getOesName()));

                for (CowpiUserRecord userRecord : records) {
                    builder.putOesData(userRecord.getUsername(), OesCiphertext.newBuilder().build());
                }

                OesContainer container = OesContainer.newBuilder()
                        .setCowpiMessage(builder)
                        .build();

                oesMessages.put(record.getId(), container);

                inner.insertInto(OES_MESSAGE)
                        .columns(OES_MESSAGE.OES_SERVICE, OES_MESSAGE.CONVERSATION_MESSAGE, OES_MESSAGE.OUTGOING)
                        .values(record.getId(), messageId, container.toByteArray())
                        .execute();
            }

            long end = System.currentTimeMillis();
            System.out.printf("Server message: %d\n", end - start);

            return oesMessages;
        });

        oesBroker.sendMessage(result);
    }
}
