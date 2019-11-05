package net.cowpi.android.perf;

import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.KeyPair;

import java.util.concurrent.Callable;

import javax.inject.Inject;

public class GenerateUserCallable implements Callable<CowpiUser> {

    private String name;
    private long id;

    private final CryptoEngine crypto;

    @Inject
    GenerateUserCallable(CryptoEngine crypto){
        this.crypto = crypto;
    }

    public void init(String name, long id){
        this.name = name;
        this.id = id;
    }

    @Override
    public CowpiUser call() throws Exception {

        KeyPair longtermKeyPair = crypto.generateLongtermKeyPair();
        EphemeralKeyPair ekp = crypto.generateEphemeralKeyPair(0, longtermKeyPair);

        CowpiUser user = new CowpiUser();
        user.init(name, id, longtermKeyPair, ekp);
        return user;
    }
}
