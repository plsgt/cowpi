package net.cowpi.perf.config;

import java.util.List;

public class PerfConfig {

    private String routerName;
    private String routerPrivateKey;
    private String routerPublicKey;
    private Database routerDatabase;

    private int routerThreadCount;

    private List<Oes> oes;

    public Database getRouterDatabase() {
        return routerDatabase;
    }

    public void setRouterDatabase(Database routerDatabase) {
        this.routerDatabase = routerDatabase;
    }

    public List<Oes> getOes() {
        return oes;
    }

    public void setOes(List<Oes> oes) {
        this.oes = oes;
    }

    public String getRouterName() {
        return routerName;
    }

    public void setRouterName(String routerName) {
        this.routerName = routerName;
    }

    public String getRouterPrivateKey() {
        return routerPrivateKey;
    }

    public void setRouterPrivateKey(String routerPrivateKey) {
        this.routerPrivateKey = routerPrivateKey;
    }

    public String getRouterPublicKey() {
        return routerPublicKey;
    }

    public void setRouterPublicKey(String routerPublicKey) {
        this.routerPublicKey = routerPublicKey;
    }

    public int getRouterThreadCount() {
        return routerThreadCount;
    }

    public void setRouterThreadCount(int routerThreadCount) {
        this.routerThreadCount = routerThreadCount;
    }
}
