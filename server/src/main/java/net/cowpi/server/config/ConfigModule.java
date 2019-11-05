package net.cowpi.server.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import io.netty.buffer.ByteBufUtil;
import net.cowpi.crypto.KeyPair;
import net.cowpi.server.RouterConstants;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Module
public class ConfigModule {

    @Provides
    @ClientBindAddress
    static SocketAddress clientBindAddress(RoutingServerConfig config){
        SocketAddress socketAddress = new InetSocketAddress(config.getClientBindAddress(), config.getClientBindPort());
        return socketAddress;
    }

    @Provides
    @OesBindAddress
    static SocketAddress oesBindAddress(RoutingServerConfig config){
        SocketAddress socketAddress = new InetSocketAddress(config.getOesBindAddress(), config.getOesBindPort());
        return socketAddress;
    }

    @Provides
    @OesPublicKeys
    static Map<String, byte[]> oesIds(RoutingServerConfig config){
        Map<String, byte[]> publicKeys = new HashMap<>();

        for(Oes oes: config.getOes()){
            publicKeys.put(oes.getName(), ByteBufUtil.decodeHexDump(oes.getLongtermPublicKey()));
        }

        return publicKeys;
    }

    @Provides
    @ServerName
    static String serverName(RoutingServerConfig config){
        return config.getServerName();
    }

    @Provides
    @ServerLongtermKeyPair
    static KeyPair serverLongtermKeyPair(RoutingServerConfig config){
        byte[] priv = ByteBufUtil.decodeHexDump(config.getLongtermPrivateKey());
        byte[] pub = ByteBufUtil.decodeHexDump(config.getLongtermPublicKey());
        return new KeyPair(priv, pub);
    }

    @Provides
    @RouterFlyway
    static Flyway routerFlyway(@RouterDataSource HikariDataSource dataSource){
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(RouterConstants.DB_MIGRATION_PATH)
                .load();
    }

    @Provides
    @Singleton
    @RouterDslContext
    static DSLContext routerDslContext(@RouterDataSource HikariDataSource dataSource){
        return DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Provides
    @RouterDataSource
    @Singleton
    static HikariDataSource database(RoutingServerConfig config){
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
