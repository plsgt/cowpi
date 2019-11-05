package net.cowpi.crypto;

public class KeyPair {

    private final byte[] priv;
    private final byte[] pub;

    public KeyPair(byte[] priv, byte[] pub) {
        this.priv = priv;
        this.pub = pub;
    }

    public byte[] getPrivateKey(){
        return priv;
    }

    public byte[] getPublicKey(){
        return pub;
    }
}
