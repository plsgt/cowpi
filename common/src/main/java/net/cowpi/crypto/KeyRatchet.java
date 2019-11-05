package net.cowpi.crypto;

import java.util.Deque;
import java.util.LinkedList;

public class KeyRatchet {

    private final String localName;
    private final KeyPair longtermKeyPair;

    private final String remoteName;
    private final byte[] remoteLongtermPublicKey;
    private final CryptoEngine crypto;

    private volatile long keyId = 0;

    private final Deque<EphemeralKeyPair> ephKeyPairs = new LinkedList<>();
    private EphemeralPublicKey lastPubKey;

    public KeyRatchet(String localName, KeyPair longtermKeyPair, String remoteName, byte[] remoteLongtermPublicKey, CryptoEngine crypto) {
        this.localName = localName;
        this.longtermKeyPair = longtermKeyPair;
        this.remoteName = remoteName;
        this.remoteLongtermPublicKey = remoteLongtermPublicKey;
        this.crypto = crypto;
    }

    public void setNextEphPublicKey(EphemeralPublicKey ephPublicKey){
        this.lastPubKey = ephPublicKey;
    }

    public EphemeralKeyPair nextEphemeralKeyPair(){
        long id = keyId++;
        EphemeralKeyPair next = crypto.generateEphemeralKeyPair(id, longtermKeyPair);
        ephKeyPairs.add(next);
        return next;
    }

    public byte[] getNaxosDecryptionKey(long keyId){
        EphemeralKeyPair ephemeralKeyPair = getEphemarlKeyPair(keyId);
        return crypto.getNaxosDecryptionKey(localName, longtermKeyPair, ephemeralKeyPair,
                remoteName, remoteLongtermPublicKey, lastPubKey.getPublicKey());
    }

    public byte[] getNaxosEncryptionKey(){
        EphemeralKeyPair ephemeralKeyPair = ephKeyPairs.peekLast();
        return crypto.getNaxosEncryptionKey(localName, longtermKeyPair, ephemeralKeyPair,
                remoteName, remoteLongtermPublicKey, lastPubKey.getPublicKey());
    }

    private EphemeralKeyPair getEphemarlKeyPair(long keyId) {
        EphemeralKeyPair next = ephKeyPairs.peek();
        while(next != null){
            if(next.getId() < keyId){
                ephKeyPairs.remove();
                next = ephKeyPairs.peek();
            }
            else if(next.getId() == keyId){
                return next;
            }
        }
        throw new IllegalStateException("Invalid Key ID");
    }

    public EphemeralPublicKey getLastPublicKey(){
        return lastPubKey;
    }
}
