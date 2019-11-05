package net.cowpi.oes.engine;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.KeyPair;
import net.cowpi.oes.config.OesDslContext;
import net.cowpi.oes.config.OesKeyPair;
import net.cowpi.oes.config.OesName;
import net.cowpi.oes.jooq.tables.records.*;
import net.cowpi.protobuf.MessageProto.*;
import net.cowpi.protobuf.MessageProto.CowpiMessage.MessageType;
import net.cowpi.protobuf.OesProto.OesPreKey;
import net.cowpi.protobuf.OesProto.OesUploadPrekeys;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.inject.Inject;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.cowpi.oes.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

public class OesEngine {
    private static final Logger logger = LoggerFactory.getLogger(OesEngine.class);

    private final String name;
    private final KeyPair keyPair;
    private final DSLContext dslContext;
    private final CryptoEngine crypto;

    @Inject
    OesEngine(@OesName String name, @OesKeyPair KeyPair keyPair, @OesDslContext DSLContext dslContext, CryptoEngine crypto) {
        this.name = name;
        this.keyPair = keyPair;
        this.dslContext = dslContext;
        this.crypto = crypto;
    }

    public OesUploadPrekeys generatePrekeys(int count){
        return dslContext.transactionResult(config -> {

            DSLContext ctx = DSL.using(config);

            InsertValuesStep2<PrekeyRecord, byte[], byte[]> insertPrekeys = ctx.insertInto(PREKEY)
                    .columns(PREKEY.PRIVATE_KEY, PREKEY.PUBLIC_KEY);

            List<byte[]> publicKeys = new ArrayList<>(count);

            OesUploadPrekeys.Builder builder = OesUploadPrekeys.newBuilder();
            for(int i=0; i<count; i++) {
                EphemeralKeyPair keyPair = crypto.generateEphemeralKeyPair(i, this.keyPair);
                publicKeys.add(keyPair.getPublicKey());
                insertPrekeys = insertPrekeys.values(keyPair.getPrivateKey(), keyPair.getPublicKey());
            }
            insertPrekeys.execute();

            Result<PrekeyRecord> records = ctx.selectFrom(PREKEY)
                    .where(PREKEY.PUBLIC_KEY.in(publicKeys))
                    .fetch();

            for(PrekeyRecord record: records){
                OesPreKey.Builder prekey = OesPreKey.newBuilder()
                        .setKeyId(record.getId())
                        .setPublicKey(ByteString.copyFrom(record.getPublicKey()));

                builder.addOesPrekey(prekey);
            }

            return builder.build();
        });
    }

    private void insertParticipantState(DSLContext ctx, ConversationRecord conversation, String author, Map<String, OesCiphertext> oesCiphertexts){
        InsertValuesStep5<ParticipantStateRecord, Long, String, byte[], Long, byte[]> insertState = ctx.insertInto(PARTICIPANT_STATE)
                .columns(PARTICIPANT_STATE.CONVERSATION, PARTICIPANT_STATE.USERNAME, PARTICIPANT_STATE.LONGTERM_KEY,
                        PARTICIPANT_STATE.REMOTE_EPHEMERAL_KEY_ID, PARTICIPANT_STATE.REMOTE_EPHEMERAL_KEY);

        for(Map.Entry<String, OesCiphertext> entry: oesCiphertexts.entrySet()){
            String user = entry.getKey();
            OesCiphertext oesData = entry.getValue();

            if(user.equals(author)){
                insertState.values(conversation.getId(), user, oesData.getLongtermKey().toByteArray(),
                        0L, oesData.getEphKey().toByteArray());
            }
            else {
                insertState.values(conversation.getId(), user, oesData.getLongtermKey().toByteArray(),
                        oesData.getKeyId(), oesData.getEphKey().toByteArray());
            }
        }
        insertState.execute();
    }

    private void updateParticipantState(DSLContext ctx, ConversationRecord conversation, String author, OesCiphertext oesCiphertext, CowpiMessage message) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidProtocolBufferException {
        ParticipantStateRecord participantState = ctx.selectFrom(PARTICIPANT_STATE)
                .where(PARTICIPANT_STATE.CONVERSATION.eq(conversation.getId())
                        .and(PARTICIPANT_STATE.USERNAME.eq(author)))
                .fetchOne();

        LocalKeyRecord localKey = ctx.selectFrom(LOCAL_KEY)
                .where(LOCAL_KEY.STATE.eq(participantState.getId())
                        .and(LOCAL_KEY.LOCAL_EPHEMERAL_KEY_ID.eq(oesCiphertext.getKeyId())))
                .fetchOne();

        EphemeralKeyPair ekp = crypto.computeEphemeralKeyPair(localKey.getLocalEphemeralKeyId(), keyPair,
                localKey.getLocalEphemeralPrivKey());

        byte[] decKey = crypto.getNaxosDecryptionKey(name, keyPair, ekp, participantState.getUsername(),
                participantState.getLongtermKey(), participantState.getRemoteEphemeralKey());

        byte[] associatedData = crypto.messageToAuthBytesOes(message, author);

        byte[] plaintext = crypto.decrypt(decKey, oesCiphertext.getCiphertext().toByteArray(), associatedData);

        OesPlaintext oesPlaintext = OesPlaintext.parseFrom(plaintext);

        ctx.update(PARTICIPANT_STATE)
                .set(PARTICIPANT_STATE.REMOTE_EPHEMERAL_KEY_ID, oesPlaintext.getKeyId())
                .set(PARTICIPANT_STATE.REMOTE_EPHEMERAL_KEY, oesPlaintext.getEphKey().toByteArray())
                .where(PARTICIPANT_STATE.ID.eq(participantState.getId()))
                .execute();
    }

    private void processSetupOesCiphertexts(DSLContext ctx, CowpiMessage.Builder result, ConversationRecord conversation, CowpiMessage message) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidProtocolBufferException {
        Result<ParticipantStateRecord> records = ctx.selectFrom(PARTICIPANT_STATE)
                .where(PARTICIPANT_STATE.CONVERSATION.eq(conversation.getId()))
                .fetch();

        InsertValuesStep3<LocalKeyRecord, Long, Long, byte[]> insertLocalKey = ctx.insertInto(LOCAL_KEY)
                .columns(LOCAL_KEY.STATE, LOCAL_KEY.LOCAL_EPHEMERAL_KEY_ID, LOCAL_KEY.LOCAL_EPHEMERAL_PRIV_KEY);

        for(ParticipantStateRecord record: records){
            boolean isAuthor = record.getUsername().equals(message.getAuthor());
            OesCiphertext oesCiphertext = message.getOesDataMap().get(record.getUsername());
            OesCiphertext.Builder oesBuilder = OesCiphertext.newBuilder();

            EphemeralKeyPair ephKeyPair;

            if(isAuthor){
                PrekeyRecord prekeyRecord = ctx.selectFrom(PREKEY)
                        .where(PREKEY.ID.eq(oesCiphertext.getKeyId()))
                        .fetchOne();

                ctx.deleteFrom(PREKEY)
                        .where(PREKEY.ID.eq(prekeyRecord.getId()))
                        .execute();

                ephKeyPair = new EphemeralKeyPair(0, prekeyRecord.getPrivateKey(), prekeyRecord.getPublicKey());
            }
            else {
                ephKeyPair = crypto.generateEphemeralKeyPair(0, keyPair);
            }

            EphemeralKeyPair nextKeyPair = crypto.generateEphemeralKeyPair(1, keyPair);

            insertLocalKey.values(record.getId(), ephKeyPair.getId(), ephKeyPair.getPrivateKey());
            insertLocalKey.values(record.getId(), nextKeyPair.getId(), nextKeyPair.getPrivateKey());

            if(isAuthor){
                byte[] decKey = crypto.getNaxosDecryptionKey(name, keyPair, ephKeyPair,
                        record.getUsername(), record.getLongtermKey(), record.getRemoteEphemeralKey());

                byte[] associatedData = crypto.messageToAuthBytesOes(message, record.getUsername());

                byte[] plaintext = crypto.decrypt(decKey, oesCiphertext.getCiphertext().toByteArray(), associatedData);

                OesPlaintext oesPlaintext = OesPlaintext.parseFrom(plaintext);
                ctx.update(PARTICIPANT_STATE)
                        .set(PARTICIPANT_STATE.REMOTE_EPHEMERAL_KEY_ID, oesPlaintext.getKeyId())
                        .set(PARTICIPANT_STATE.REMOTE_EPHEMERAL_KEY, oesPlaintext.getEphKey().toByteArray())
                        .where(PARTICIPANT_STATE.ID.eq(record.getId()))
                        .execute();

                byte[] encKey = crypto.getNaxosEncryptionKey(name, keyPair, ephKeyPair,
                        record.getUsername(), record.getLongtermKey(), oesPlaintext.getEphKey().toByteArray());

                OesPlaintext.Builder oesPlaintextBuilder = OesPlaintext.newBuilder()
                        .setKeyId(nextKeyPair.getId())
                        .setEphKey(ByteString.copyFrom(nextKeyPair.getPublicKey()));

                byte[] ciphertext = crypto.encrypt(encKey, oesPlaintextBuilder.build().toByteArray(), associatedData);

                oesBuilder.setKeyId(oesPlaintext.getKeyId())
                        .setEphKey(ByteString.copyFrom(ephKeyPair.getPublicKey()))
                        .setCiphertext(ByteString.copyFrom(ciphertext))
                        .build();
            }
            else {

                byte[] encKey = crypto.getNaxosEncryptionKey(name, keyPair, ephKeyPair,
                        record.getUsername(), record.getLongtermKey(), record.getRemoteEphemeralKey());

                OesPlaintext.Builder plaintext = OesPlaintext.newBuilder()
                        .setKeyId(nextKeyPair.getId())
                        .setEphKey(ByteString.copyFrom(nextKeyPair.getPublicKey()));

                byte[] associatedData = crypto.messageToAuthBytesOes(message, record.getUsername());

                byte[] ciphertext = crypto.encrypt(encKey, plaintext.build().toByteArray(), associatedData);

                oesBuilder.setKeyId(record.getRemoteEphemeralKeyId())
                        .setEphKey(ByteString.copyFrom(ephKeyPair.getPublicKey()))
                        .setCiphertext(ByteString.copyFrom(ciphertext))
                        .build();
            }

            result.putOesData(record.getUsername(), oesBuilder.build());
        }

        insertLocalKey.execute();
    }

    private void processExistingOesCiphertexts(DSLContext ctx, CowpiMessage.Builder result, ConversationRecord conversation, CowpiMessage message) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidProtocolBufferException {
        InsertValuesStep3<LocalKeyRecord, Long, Long, byte[]> insertLocalKeys = ctx.insertInto(LOCAL_KEY)
                .columns(LOCAL_KEY.STATE, LOCAL_KEY.LOCAL_EPHEMERAL_KEY_ID, LOCAL_KEY.LOCAL_EPHEMERAL_PRIV_KEY);

        CommonTableExpression<Record8<Long, String, byte[], Long, byte[], Long, byte[], Integer>> curKey = name("curkey")
                .fields("state", "username", "longterm_key", "remote_key_id", "remote_ephemeral",
                        "local_key_id", "local_private", "row_number")
                .as(select(PARTICIPANT_STATE.ID, PARTICIPANT_STATE.USERNAME, PARTICIPANT_STATE.LONGTERM_KEY,
                        PARTICIPANT_STATE.REMOTE_EPHEMERAL_KEY_ID, PARTICIPANT_STATE.REMOTE_EPHEMERAL_KEY,
                        LOCAL_KEY.LOCAL_EPHEMERAL_KEY_ID, LOCAL_KEY.LOCAL_EPHEMERAL_PRIV_KEY,
                        rowNumber().over()
                                .partitionBy(LOCAL_KEY.STATE)
                                .orderBy(LOCAL_KEY.LOCAL_EPHEMERAL_KEY_ID.desc())
                                .as("rn"))
                        .from(LOCAL_KEY)
                        .innerJoin(PARTICIPANT_STATE).on(LOCAL_KEY.STATE.eq(PARTICIPANT_STATE.ID))
                        .where(PARTICIPANT_STATE.CONVERSATION.eq(conversation.getId())));

        Result<Record8<Long, String, byte[], Long, byte[], Long, byte[], Integer>> records = ctx.with(curKey)
                .selectFrom(curKey)
                .where(curKey.field("row_number", Integer.class).eq(1))
                .fetch();

        for(Record8<Long, String, byte[], Long, byte[], Long, byte[], Integer> record: records){
            EphemeralKeyPair nextEkp = crypto.generateEphemeralKeyPair(record.component6()+1, keyPair);
            insertLocalKeys.values(record.component1(), nextEkp.getId(), nextEkp.getPrivateKey());

            EphemeralKeyPair ekp = crypto.computeEphemeralKeyPair(record.component6(), keyPair,
                    record.component7());

            byte[] encKey = crypto.getNaxosEncryptionKey(name, keyPair, ekp, record.component2(),
                    record.component3(), record.component5());

            OesPlaintext.Builder oesPlaintextBuilder = OesPlaintext.newBuilder()
                    .setKeyId(nextEkp.getId())
                    .setEphKey(ByteString.copyFrom(nextEkp.getPublicKey()));

            byte[] associatedData = crypto.messageToAuthBytesOes(message, record.component2());

            byte[] ciphertext = crypto.encrypt(encKey, oesPlaintextBuilder.build().toByteArray(), associatedData);

            OesCiphertext.Builder oesCiphertextBuilder = OesCiphertext.newBuilder()
                    .setKeyId(record.component4())
                    .setCiphertext(ByteString.copyFrom(ciphertext));

            result.putOesData(record.component2(), oesCiphertextBuilder.build());
        }

        insertLocalKeys.execute();
    }

    public CowpiMessage processCowpiMessage(CowpiMessage message){

        return dslContext.transactionResult(config -> {
            DSLContext ctx = DSL.using(config);

            RouterMailboxRecord mailboxRecord = ctx.selectFrom(ROUTER_MAILBOX)
                    .where(ROUTER_MAILBOX.conversation().CONVERSATION_ID.eq(message.getConversationId())
                            .and(ROUTER_MAILBOX.MESSAGE_INDEX.eq(message.getIndex())))
                    .fetchOne();

            if(mailboxRecord != null){
                return CowpiMessage.parseFrom(mailboxRecord.getMessage());
            }

            CowpiMessage.Builder result = message.toBuilder()
                    .clearOesData();

            ConversationRecord conversation;

            if (MessageType.SETUP.equals(message.getType())) {
                 conversation = ctx.insertInto(CONVERSATION)
                        .columns(CONVERSATION.CONVERSATION_ID, CONVERSATION.NEXT_INDEX)
                        .values(message.getConversationId(), 1L)
                        .returning()
                        .fetchOne();

                 insertParticipantState(ctx, conversation, message.getAuthor(), message.getOesDataMap());
                 processSetupOesCiphertexts(ctx, result, conversation, message);
            }
            else {
                conversation = ctx.selectFrom(CONVERSATION)
                        .where(CONVERSATION.CONVERSATION_ID.eq(message.getConversationId())
                            .and(CONVERSATION.NEXT_INDEX.eq(message.getIndex())))
                        .fetchOne();

                ctx.update(CONVERSATION)
                        .set(CONVERSATION.NEXT_INDEX, conversation.getNextIndex()+1)
                        .where(CONVERSATION.ID.eq(conversation.getId()))
                        .execute();

                updateParticipantState(ctx, conversation, message.getAuthor(), message.getOesDataMap().get(message.getAuthor()), message);
                processExistingOesCiphertexts(ctx, result, conversation, message);
            }

            CowpiMessage resultMessage = result.build();

            ctx.deleteFrom(ROUTER_MAILBOX)
                    .where(ROUTER_MAILBOX.CONVERSATION.eq(conversation.getId()))
                    .execute();

            ctx.insertInto(ROUTER_MAILBOX)
                    .columns(ROUTER_MAILBOX.CONVERSATION, ROUTER_MAILBOX.MESSAGE_INDEX, ROUTER_MAILBOX.MESSAGE)
                    .values(conversation.getId(), resultMessage.getIndex(), resultMessage.toByteArray())
                    .execute();

            return resultMessage;
        });
    }
}
