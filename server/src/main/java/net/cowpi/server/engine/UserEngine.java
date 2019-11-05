package net.cowpi.server.engine;

import com.google.protobuf.ByteString;
import net.cowpi.protobuf.CiphertextProto.RegisterUser;
import net.cowpi.protobuf.OesProto.OesPreKey;
import net.cowpi.protobuf.UserProtos.FetchPrekey;
import net.cowpi.protobuf.UserProtos.PreKey;
import net.cowpi.server.config.OesPublicKeys;
import net.cowpi.server.config.RouterDslContext;
import net.cowpi.server.jooq.tables.records.CowpiUserRecord;
import net.cowpi.server.jooq.tables.records.OesPrekeyRecord;
import net.cowpi.server.jooq.tables.records.OesServiceRecord;
import net.cowpi.server.jooq.tables.records.PrekeyRecord;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep3;
import org.jooq.Record3;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static net.cowpi.server.jooq.Tables.*;

public class UserEngine {
    private static final Logger logger = LoggerFactory.getLogger(UserEngine.class);

    private final DSLContext dslContext;

    private final Map<String, byte[]> oesKeys;

    @Inject
    public UserEngine(@RouterDslContext DSLContext dslContext, @OesPublicKeys Map<String, byte[]> oesKeys){
        this.dslContext = dslContext;
        this.oesKeys = oesKeys;
    }

    public void addUser(RegisterUser registerUser) {
        dslContext.insertInto(COWPI_USER)
                    .columns(COWPI_USER.USERNAME, COWPI_USER.LONGTERM_KEY)
                    .values(registerUser.getUsername(), registerUser.getLongtermKey().toByteArray())
                    .execute();
    }

    public void addPreKeys(long userId, List<PreKey> prekeys){
        dslContext.transaction(config -> {
            DSLContext inner = DSL.using(config);

            InsertValuesStep3<PrekeyRecord, Long, Long, byte[]> insertPrekeys = inner.insertInto(PREKEY)
                    .columns(PREKEY.USER_ID, PREKEY.KEY_ID, PREKEY.PREKEY_);
            for(PreKey preKey: prekeys){
                insertPrekeys.values(userId, (long)preKey.getKeyId(), preKey.getPrekey().toByteArray());
            }
            insertPrekeys.execute();
        });
    }

    public CowpiUserRecord getUserRecord(String username) {
        return dslContext.selectFrom(COWPI_USER)
                .where(COWPI_USER.USERNAME.eq(username))
                .fetchOne();
    }

    public OesServiceRecord getOesRecord(String oesName) {
        return dslContext.selectFrom(OES_SERVICE)
                .where(OES_SERVICE.OES_NAME.eq(oesName))
                .fetchOne();
    }

    public FetchPrekey fetchPreKeys(List<String> usernames) {
        return dslContext.transactionResult(config -> {
            FetchPrekey.Builder builder = FetchPrekey.newBuilder();

            DSLContext inner = DSL.using(config);

            for(String username: usernames) {
                Record3<Long, Long, byte[]> record = inner.select(PREKEY.KEY_ID, PREKEY.ID, PREKEY.PREKEY_)
                        .from(PREKEY)
                        .join(COWPI_USER)
                        .on(PREKEY.USER_ID.eq(COWPI_USER.ID))
                        .where(COWPI_USER.USERNAME.eq(username))
                        .fetchAny();

                inner.deleteFrom(PREKEY)
                        .where(PREKEY.ID.eq(record.value2()))
                        .execute();

                PreKey.Builder preKey = PreKey.newBuilder()
                        .setKeyId(record.component1().intValue())
                        .setPrekey(ByteString.copyFrom(record.component3()));

                builder.putKeys(username, preKey.build());
            }

            for(String oes: oesKeys.keySet()) {
                Record3<Long, Long, byte[]> record = inner.select(OES_PREKEY.KEY_ID, OES_PREKEY.ID, OES_PREKEY.PREKEY)
                        .from(OES_PREKEY)
                        .join(OES_SERVICE)
                        .on(OES_PREKEY.OES_ID.eq(OES_SERVICE.ID))
                        .where(OES_SERVICE.OES_NAME.eq(oes))
                        .fetchAny();

                inner.deleteFrom(OES_PREKEY)
                        .where(OES_PREKEY.ID.eq(record.value2()))
                        .execute();

                PreKey.Builder preKey = PreKey.newBuilder()
                        .setKeyId(record.component1().intValue())
                        .setPrekey(ByteString.copyFrom(record.component3()));

                builder.putOesKeys(oes, preKey.build());
            }

           return builder.build();
        });
    }

    public void addOesPreKeys(long oesId, List<OesPreKey> prekeys) {
        dslContext.transaction(config -> {
            DSLContext inner = DSL.using(config);

            InsertValuesStep3<OesPrekeyRecord, Long, Long, byte[]> insertPrekeys = inner.insertInto(OES_PREKEY)
                    .columns(OES_PREKEY.OES_ID, OES_PREKEY.KEY_ID, OES_PREKEY.PREKEY);
            for(OesPreKey preKey: prekeys){
                insertPrekeys.values(oesId, (long) preKey.getKeyId(), preKey.getPublicKey().toByteArray());
            }

            insertPrekeys.execute();
        });
    }
}
