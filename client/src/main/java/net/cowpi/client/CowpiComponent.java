package net.cowpi.client;

import dagger.BindsInstance;
import dagger.Component;
import javafx.fxml.FXMLLoader;
import net.cowpi.ServiceManager;
import net.cowpi.client.config.ClientConfiguration;
import net.cowpi.client.config.ConfigModule;
import net.cowpi.client.tcp.TcpModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ClientModule.class, TcpModule.class, ConfigModule.class})
public interface CowpiComponent {

    @ClientServiceManager
    ServiceManager clientServiceManager();

    @Component.Builder
    interface Builder{
        @BindsInstance Builder config(ClientConfiguration config);
        CowpiComponent build();
    }

}
