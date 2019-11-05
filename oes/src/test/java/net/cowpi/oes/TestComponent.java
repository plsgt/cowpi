package net.cowpi.oes;

import dagger.BindsInstance;
import dagger.Component;
import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.CryptoModule;
import net.cowpi.crypto.KeyPair;
import net.cowpi.oes.config.ConfigModule;
import net.cowpi.oes.config.OesFlyway;
import net.cowpi.oes.config.OesKeyPair;
import net.cowpi.oes.engine.EngineModule;
import net.cowpi.oes.schedule.ScheduleModule;
import net.cowpi.oes.test.*;
import org.flywaydb.core.Flyway;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ConfigModule.class, OesModule.class, TestModule.class, CryptoModule.class, EngineModule.class})
public interface TestComponent extends OesComponent {

    TestRoutingServer testRoutingServer();

    TestChannelPool channelPool();

    CryptoEngine crypto();

    TestOesConfig config();

    TestUser testUser();

    @OesFlyway
    Flyway flyway();

    @OesKeyPair
    KeyPair oesKeyPair();

    @TestRoutingServerKeyPair
    KeyPair routingServerKeyPair();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder config(TestOesConfig config);
        TestComponent build();
    }
}
