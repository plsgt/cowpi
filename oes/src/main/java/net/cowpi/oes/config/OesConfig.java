package net.cowpi.oes.config;

public class OesConfig {

    private String oesName;

    private int clientPoolSize;

    private String routerAddress;
    private int routerPort;

    private String longtermPrivateKey;
    private String longtermPublicKey;

    private String routerName;
    private String routerLongtermPublicKey;

    private int messageQueueSize;
    private int messageQueueThreads;

    private Database database;

    public String getOesName() {
        return oesName;
    }

    public void setOesName(String oesName) {
        this.oesName = oesName;
    }

    public String getRouterAddress() {
        return routerAddress;
    }

    public void setRouterAddress(String routerAddress) {
        this.routerAddress = routerAddress;
    }

    public int getRouterPort() {
        return routerPort;
    }

    public void setRouterPort(int routerPort) {
        this.routerPort = routerPort;
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

    public String getRouterName() {
        return routerName;
    }

    public void setRouterName(String routerName) {
        this.routerName = routerName;
    }

    public String getRouterLongtermPublicKey() {
        return routerLongtermPublicKey;
    }

    public void setRouterLongtermPublicKey(String routerLongtermPublicKey) {
        this.routerLongtermPublicKey = routerLongtermPublicKey;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public int getClientPoolSize() {
        return clientPoolSize;
    }

    public void setClientPoolSize(int clientPoolSize) {
        this.clientPoolSize = clientPoolSize;
    }

    public int getMessageQueueSize() {
        return messageQueueSize;
    }

    public void setMessageQueueSize(int messageQueueSize) {
        this.messageQueueSize = messageQueueSize;
    }

    public int getMessageQueueThreads() {
        return messageQueueThreads;
    }

    public void setMessageQueueThreads(int messageQueueThreads) {
        this.messageQueueThreads = messageQueueThreads;
    }
}
