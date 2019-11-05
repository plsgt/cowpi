package net.cowpi.android.perf;

import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.KeyPair;

public class CowpiUser {

    private String name;
    private long id;
    private KeyPair longtermKeyPair;
    private EphemeralKeyPair ephemeralKeyPair;

    void init(String name, long id, KeyPair longtermKeyPair, EphemeralKeyPair ephemeralKeyPair){
        this.name = name;
        this.id = id;
        this.longtermKeyPair = longtermKeyPair;
        this.ephemeralKeyPair = ephemeralKeyPair;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public KeyPair getLongtermKeyPair() {
        return longtermKeyPair;
    }

    public EphemeralKeyPair getEphemeralKeyPair() {
        return ephemeralKeyPair;
    }
}
