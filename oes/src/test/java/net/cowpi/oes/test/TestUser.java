package net.cowpi.oes.test;

import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.KeyPair;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class TestUser {

    private volatile String username;

    private volatile long lastRemoteKeyId;
    private volatile byte[] lastRemotePubKey;

    private final CryptoEngine crypto;
    private final KeyPair longtermKeyPair;

    private final List<EphemeralKeyPair> epks = new ArrayList<>();

    @Inject
    public TestUser(CryptoEngine crypto) {
        this.crypto = crypto;
        longtermKeyPair = crypto.generateLongtermKeyPair();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public EphemeralKeyPair generateEphemerKeyPair() {
        int id = epks.size();
        EphemeralKeyPair result = crypto.generateEphemeralKeyPair(id, longtermKeyPair);
        epks.add(result);
        return result;
    }

    public KeyPair getKeyPair() {
        return  longtermKeyPair;
    }

    public EphemeralKeyPair getEphemeralKeyPair(long i) {
        return epks.get((int) i);
    }

    public long getLastRemoteKeyId() {
        return lastRemoteKeyId;
    }

    public void setLastRemoteKeyId(long lastRemoteKeyId) {
        this.lastRemoteKeyId = lastRemoteKeyId;
    }

    public byte[] getLastRemotePubKey() {
        return lastRemotePubKey;
    }

    public void setLastRemotePubKey(byte[] lastRemotePubKey) {
        this.lastRemotePubKey = lastRemotePubKey;
    }
}
