package net.cowpi.perf.config;

public class Oes {

    private String name;
    private String longtermPublicKey;
    private String longtermPrivateKey;
    private Database database;
    private int oesThreadCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLongtermPublicKey() {
        return longtermPublicKey;
    }

    public void setLongtermPublicKey(String longtermPublicKey) {
        this.longtermPublicKey = longtermPublicKey;
    }

    public String getLongtermPrivateKey() {
        return longtermPrivateKey;
    }

    public void setLongtermPrivateKey(String longtermPrivateKey) {
        this.longtermPrivateKey = longtermPrivateKey;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public int getOesThreadCount() {
        return oesThreadCount;
    }

    public void setOesThreadCount(int oesThreadCount) {
        this.oesThreadCount = oesThreadCount;
    }
}
