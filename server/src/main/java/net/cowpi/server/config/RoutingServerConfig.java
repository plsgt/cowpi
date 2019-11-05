package net.cowpi.server.config;

import java.util.List;

public class RoutingServerConfig {

    private String serverName;

    private String clientBindAddress;
    private int clientBindPort;

    private String oesBindAddress;
    private int oesBindPort;

    private String longtermPrivateKey;
    private String longtermPublicKey;

    private Database database;

    private List<Oes> oes;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getClientBindAddress() {
        return clientBindAddress;
    }

    public void setClientBindAddress(String clientBindAddress) {
        this.clientBindAddress = clientBindAddress;
    }

    public int getClientBindPort() {
        return clientBindPort;
    }

    public void setClientBindPort(int clientBindPort) {
        this.clientBindPort = clientBindPort;
    }

    public String getOesBindAddress() {
        return oesBindAddress;
    }

    public void setOesBindAddress(String oesBindAddress) {
        this.oesBindAddress = oesBindAddress;
    }

    public int getOesBindPort() {
        return oesBindPort;
    }

    public void setOesBindPort(int oesBindPort) {
        this.oesBindPort = oesBindPort;
    }

    public String getLongtermPrivateKey() {
        return longtermPrivateKey;
    }

    public void setLongtermPrivateKey(String longtermPrivateKey) {
        this.longtermPrivateKey = longtermPrivateKey;
    }

    public String getLongtermPublicKey() {
        return longtermPublicKey;
    }

    public void setLongtermPublicKey(String longtermPublicKey) {
        this.longtermPublicKey = longtermPublicKey;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public List<Oes> getOes() {
        return oes;
    }

    public void setOes(List<Oes> oes) {
        this.oes = oes;
    }
}
