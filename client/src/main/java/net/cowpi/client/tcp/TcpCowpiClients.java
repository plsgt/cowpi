package net.cowpi.client.tcp;

import net.cowpi.ManagedTcpClients;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TcpCowpiClients {

    private final ManagedTcpClients tcpClients;

    @Inject
    public TcpCowpiClients(@CowpiManagedClients ManagedTcpClients tcpClients) {
        this.tcpClients = tcpClients;
    }
}
