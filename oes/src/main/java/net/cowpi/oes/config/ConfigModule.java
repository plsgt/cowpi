package net.cowpi.oes.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import io.netty.buffer.ByteBufUtil;
import net.cowpi.crypto.KeyPair;
import net.cowpi.oes.OesConstants;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Properties;

@Module
public abstract class ConfigModule {

    @Provides
    @RoutingServerOesAddress
    public static SocketAddress routingServerOesAddress(OesConfig config){
        return new InetSocketAddress(config.getRouterAddress(), config.getRouterPort());
    }

    @Provides
    @OesName
    public static String oesName(OesConfig config){
        return config.getOesName();
    }

    @Provides
    @OesKeyPair
    public static KeyPair oesKeyPair(OesConfig config){
        byte[] priv = ByteBufUtil.decodeHexDump(config.getLongtermPrivateKey());
        byte[] pub = ByteBufUtil.decodeHexDump(config.getLongtermPublicKey());
        return new KeyPair(priv, pub);
    }

    @Provides
    @RouterServiceName
    public static String routerServiceName(OesConfig config){
        return config.getRouterName();
    }

    @Provides
    @RouterLongterPublicKey
    public static byte[] routerLongtermPublicKey(OesConfig config){
        return ByteBufUtil.decodeHexDump(config.getRouterLongtermPublicKey());
    }

    @Provides
    @ClientPoolSize
    static int clientPoolSize(OesConfig config){
        return config.getClientPoolSize();
    }

    @Provides
    @MessageQueueThreads
    static int messageQueueThreads(OesConfig config){
        return config.getMessageQueueThreads();
    }

    @Provides
    @MessageQueueSize
    static int messageQueueSize(OesConfig config){
        return config.getMessageQueueSize();
    }

    @Provides
    @Singleton
    @OesDslContext
    static DSLContext oesDslContext(@OesDataSource HikariDataSource dataSource){
        return DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Provides
    @OesFlyway
    static Flyway oesFlyway(@OesDataSource HikariDataSource dataSource){
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(OesConstants.DB_MIGRATION_PATH)
                .load();
    }


    @Provides
    @Singleton
    @OesDataSource
    public static HikariDataSource database(OesConfig config){
        Database database = config.getDatabase();

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
