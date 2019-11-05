package net.cowpi.client.db;

import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.sqlite.SQLiteDataSource;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.cowpi.client.jooq.Tables.COWPI_USER;
import static net.cowpi.client.jooq.Tables.PREKEY;

@Singleton
public class Database {
    private static final String JDBC_URL = "jdbc:sqlite:";

    private SQLiteDataSource datasource;
    private final String dbFile;

    @Inject
    public Database(@DatabaseFile  String dbFile){
        this.dbFile = dbFile;
    }
    
    public void start(){
        String url = new StringBuilder()
                .append(JDBC_URL)
                .append(dbFile)
                .toString();

        datasource = new SQLiteDataSource();
        datasource.setUrl(url);

        migrate();
    }

    private void migrate(){
        Flyway flyway = Flyway.configure().dataSource(datasource).load();
        flyway.migrate();
    }

    public void shutdown(){
        datasource = null;
    }



    public void addUser(String username, byte[] longtermKey){
        DSLContext create = DSL.using(datasource, SQLDialect.SQLITE);
        create.insertInto(COWPI_USER)
                .columns(COWPI_USER.USERNAME, COWPI_USER.LONGTERM_KEY)
                .values(username, longtermKey)
                .execute();
    }

    public void addPreKey(String username, int keyId, byte[] prekey){
        DSLContext create = DSL.using(datasource, SQLDialect.SQLITE);
        create.transaction(configuration -> {
            DSLContext inner = DSL.using(configuration);

            Integer id = inner.select(COWPI_USER.ID)
                    .from(COWPI_USER)
                    .where(COWPI_USER.USERNAME.eq(username))
                    .fetchOne().value1();
            inner.insertInto(PREKEY)
                    .columns(PREKEY.USER_ID, PREKEY.KEY_ID, COWPI_USER.LONGTERM_KEY)
                    .values(id, keyId, prekey)
                    .execute();
        });

    }
}
