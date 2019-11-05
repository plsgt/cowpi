package net.cowpi.oes;

import dagger.Module;
import dagger.Provides;
import net.cowpi.ManagedService;
import net.cowpi.ServiceManager;
import net.cowpi.oes.db.Database;
import net.cowpi.oes.schedule.Schedule;
import net.cowpi.oes.tcp.RoutingServiceClientPool;

import javax.inject.Singleton;

@Module
public abstract class OesModule {

    @Provides
    @Singleton
    @OesServiceManager
    public static ServiceManager oesServiceManager(Database database, RoutingServiceClientPool clientPool,
                                                   @Schedule ManagedService schedule){
        ServiceManager serviceManager = new ServiceManager();
        serviceManager.addService(database);
        serviceManager.addService(clientPool);
        serviceManager.addService(schedule);
        return serviceManager;
    }
}
