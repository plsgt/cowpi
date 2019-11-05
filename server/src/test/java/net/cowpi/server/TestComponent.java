package net.cowpi.server;

import dagger.BindsInstance;
import dagger.Component;
import net.cowpi.crypto.CryptoModule;
import net.cowpi.server.config.ConfigModule;
import net.cowpi.server.config.OesPublicKeys;
import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.KeyPair;
import net.cowpi.server.config.RouterFlyway;
import net.cowpi.server.oes.OesModule;
import net.cowpi.server.tcp.TcpModule;
import net.cowpi.server.test.TestClientManager;
import org.flywaydb.core.Flyway;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
@Component(modules = {TcpModule.class, OesModule.class, ConfigModule.class, TestModule.class, CryptoModule.class})
public interface TestComponent extends MainComponent {

    TestClientManager testClientManager();
    CryptoEngine cryptoEngine();

    @RouterFlyway
    Flyway flyway();

    @OesKeyPairs
    Map<String, KeyPair> oesKeyPairs();

    @OesPublicKeys
    Map<String, byte[]> oesPublicKeys();

    @Component.Builder
    interface Builder {
        @BindsInstance Builder config(TestServerConfig config);
        TestComponent build();
    }

}
