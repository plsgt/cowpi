package net.cowpi.server;

import net.cowpi.server.config.RoutingServerConfig;

import java.util.List;

public class TestServerConfig {

    private RoutingServerConfig routingServerConfig;

    private List<TestOes> oes;

    public RoutingServerConfig getRoutingServerConfig() {
        return routingServerConfig;
    }

    public void setRoutingServerConfig(RoutingServerConfig routingServerConfig) {
        this.routingServerConfig = routingServerConfig;
    }

    public List<TestOes> getOes() {
        return oes;
    }

    public void setOes(List<TestOes> oes) {
        this.oes = oes;
    }
}
