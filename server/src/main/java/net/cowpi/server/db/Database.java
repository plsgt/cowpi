package net.cowpi.server.db;

import com.zaxxer.hikari.HikariDataSource;
import net.cowpi.ManagedService;
import net.cowpi.server.config.RouterDataSource;
import net.cowpi.server.config.RouterFlyway;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class Database implements ManagedService {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    private final HikariDataSource dataSource;
    private final Flyway flyway;

    @Inject
    public Database(@RouterDataSource HikariDataSource dataSource, @RouterFlyway Flyway flyway) {
        this.dataSource = dataSource;
        this.flyway = flyway;
    }

    @Override
    public CompletionStage<Void> start() {
        flyway.clean();
        flyway.migrate();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> shutdown() {
        dataSource.close();
        return CompletableFuture.completedFuture(null);
    }
}
