package net.cowpi.crypto;

public class EphemeralKeyPair {

    private final long id;
    private final byte[] priv;
    private final byte[] pub;

    public EphemeralKeyPair(long id, byte[] priv, byte[] pub) {
        this.id = id;
        this.priv = priv;
        this.pub = pub;
    }

    public long getId(){
        return id;
    }

    public byte[] getPrivateKey(){
        return priv;
    }

    public byte[] getPublicKey(){
        return pub;
    }
}
