package net.cowpi.server;

import net.cowpi.ServiceManager;
import net.cowpi.server.db.Database;
import net.cowpi.server.oes.OesTcpServer;
import net.cowpi.server.tcp.ClientTcpServer;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RoutingServiceManager extends ServiceManager {

    @Inject
    public RoutingServiceManager(Database database, OesTcpServer oesTcpServer, ClientTcpServer clientTcpServer){
        addService(database);
        addService(oesTcpServer);
        addService(clientTcpServer);
    }
}
