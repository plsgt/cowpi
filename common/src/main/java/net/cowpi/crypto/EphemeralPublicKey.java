package net.cowpi.crypto;

public class EphemeralPublicKey {

    private final long id;
    private final byte[] pub;

    public EphemeralPublicKey(long id, byte[] pub) {
        this.id = id;
        this.pub = pub;
    }

    public long getId(){
        return id;
    }

    public byte[] getPublicKey(){
        return pub;
    }
}
