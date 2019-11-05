package net.cowpi.perf.router;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.cowpi.perf.config.Database;
import net.cowpi.perf.config.PerfConfig;
import net.cowpi.server.RouterConstants;
import net.cowpi.server.config.RouterDataSource;
import net.cowpi.server.config.RouterDslContext;
import net.cowpi.server.config.RouterFlyway;
import net.cowpi.server.engine.MessageBroker;
import net.cowpi.server.engine.OesBroker;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.inject.Singleton;
import java.util.Properties;

@Module
public abstract class RouterModule {

    @Binds
    abstract OesBroker oesBroker(QueuedOesBroker oesBroker);

    @Binds
    abstract MessageBroker messageBroker(QueuedMessageBroker messageBroker);

    @Provides
    @RouterFlyway
    static Flyway routerFlyway(@RouterDataSource HikariDataSource dataSource){
        return Flyway.configure()
                .locations(RouterConstants.DB_MIGRATION_PATH)
                .dataSource(dataSource)
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
    static HikariDataSource routerDatasource(PerfConfig config){
        Database database = config.getRouterDatabase();

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
