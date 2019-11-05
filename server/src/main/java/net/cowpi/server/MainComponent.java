package net.cowpi.server;

import dagger.BindsInstance;
import dagger.Component;
import net.cowpi.crypto.CryptoModule;
import net.cowpi.server.config.ConfigModule;
import net.cowpi.server.config.RoutingServerConfig;
import net.cowpi.server.oes.OesModule;
import net.cowpi.server.tcp.TcpModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {TcpModule.class, OesModule.class, ConfigModule.class, CryptoModule.class})
public interface MainComponent {

    RoutingServiceManager routingServiceManager();

    @Component.Builder
    interface Builder  {
        @BindsInstance Builder routerServerConfig(RoutingServerConfig config);
        MainComponent build();
    }
}
