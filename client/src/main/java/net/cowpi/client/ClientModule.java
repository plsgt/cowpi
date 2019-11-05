package net.cowpi.client;

import dagger.Module;
import dagger.Provides;
import net.cowpi.ManagedTcpClients;
import net.cowpi.ServiceManager;
import net.cowpi.client.tcp.CowpiManagedClients;

import javax.inject.Singleton;

@Module
public abstract class ClientModule {

    @Provides
    @ClientServiceManager
    @Singleton
    public static ServiceManager clientServiceManager(@CowpiManagedClients ManagedTcpClients clients) {
        ServiceManager serviceManager = new ServiceManager();
        serviceManager.addService(clients);
        return serviceManager;
    }
}
