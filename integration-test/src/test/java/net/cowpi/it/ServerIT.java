package net.cowpi.it;

import net.cowpi.ServiceManager;
import net.cowpi.server.DaggerMainComponent;
import net.cowpi.server.Main;
import net.cowpi.server.MainComponent;
import net.cowpi.server.config.HardCodedServerConfig;
import org.junit.Test;

public class ServerIT {

    @Test
    public void testStartStopServer(){
        MainComponent component = DaggerMainComponent.builder()
                .routerServerConfig(new HardCodedServerConfig())
                .build();

        ServiceManager serviceManager = component.routingServiceManager();
        serviceManager.start().toCompletableFuture().join();
        serviceManager.shutdown().toCompletableFuture().join();
    }

}
