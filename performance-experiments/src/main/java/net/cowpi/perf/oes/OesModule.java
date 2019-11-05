package net.cowpi.perf.oes;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import io.netty.buffer.ByteBufUtil;
import net.cowpi.crypto.KeyPair;
import net.cowpi.oes.OesConstants;
import net.cowpi.oes.config.*;
import net.cowpi.perf.config.Database;
import net.cowpi.perf.config.Oes;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.Properties;

@Module
public abstract class OesModule {

    @Provides
    @OesName
    static String oesName(Oes oes){
        return oes.getName();
    }

    @Provides
    @OesKeyPair
    public static KeyPair oesKeyPair(Oes oes){
        byte[] priv = ByteBufUtil.decodeHexDump(oes.getLongtermPrivateKey());
        byte[] pub = ByteBufUtil.decodeHexDump(oes.getLongtermPublicKey());
        return new KeyPair(priv, pub);
    }

    @Provides
    @OesThreadCount
    static int oesThreadCount(Oes oes){
        return oes.getOesThreadCount();
    }

    @Provides
    @OesFlyway
    static Flyway oesFlyway(@OesDataSource HikariDataSource dataSource){
        return Flyway.configure()
                .locations(OesConstants.DB_MIGRATION_PATH)
                .dataSource(dataSource)
                .load();
    }

    @Provides
    @OesScope
    @OesDslContext
    static DSLContext oesDslContext(@OesDataSource HikariDataSource dataSource){
        return DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Provides
    @OesDataSource
    @OesScope
    static HikariDataSource oesDatasource(Oes oes){
        Database database = oes.getDatabase();

        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("schema", database.getSchema());
        props.setProperty("dataSource.user", database.getUsername());
        props.setProperty("dataSource.password", database.getPassword());
        props.setProperty("dataSource.databaseName", database.getName());
        props.setProperty("dataSource.serverName", database.getServer());
        props.setProperty("dataSource.portNumber", database.getPort());

        HikariConfig hikariConfig = new HikariConfig(props);

        return new HikariDataSource(hikariConfig);
    }
}
