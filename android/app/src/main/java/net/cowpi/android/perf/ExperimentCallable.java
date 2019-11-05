package net.cowpi.android.perf;

import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.EphemeralPublicKey;
import net.cowpi.crypto.KeyPair;
import net.cowpi.crypto.KeyRatchet;
import net.cowpi.protobuf.MessageProto.CowpiMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Provider;

public class ExperimentCallable implements Callable<Void> {
    private static final Logger logger = Logger.getLogger(ExperimentCallable.class.toString());

    private static final int RUNS = 100;

    private final Provider<GenerateUserCallable> generateUserCallableProvider;
    private final Provider<SetupSessionCallable> setupSessionCallableProvider;
    private final CryptoEngine crypto;

    private int min;
    private int max;
    private ExecutorService executor;

    @Inject
    public ExperimentCallable(Provider<GenerateUserCallable> generateUserCallableProvider, Provider<SetupSessionCallable> setupSessionCallableProvider, CryptoEngine crypto) {
        this.generateUserCallableProvider = generateUserCallableProvider;
        this.setupSessionCallableProvider = setupSessionCallableProvider;
        this.crypto = crypto;
    }

    public void init(int min, int max, ExecutorService executor){
        this.min = min;
        this.max = max;
        this.executor = executor;
    }

    @Override
    public Void call() throws Exception {

        Map<String, KeyPair> oesKeyPairs = new HashMap<>();
        oesKeyPairs.put("OES_1", crypto.generateLongtermKeyPair());
        oesKeyPairs.put("OES_2", crypto.generateLongtermKeyPair());

        logger.info("Generating users..." + max);
        List<Callable<CowpiUser>> generateUsers = new ArrayList<>();
        for(int i=min; i <= max; i++){
            GenerateUserCallable callable = generateUserCallableProvider.get();
            callable.init("USER "+i, i);
            generateUsers.add(callable);
        }

        List<Future<CowpiUser>> userFutures = executor.invokeAll(generateUsers);

        List<CowpiUser> users = new ArrayList<>(max);
        for(Future<CowpiUser> userFuture: userFutures){
            users.add(userFuture.get());
        }

        List<SetupSessionCallable> setupCallables = new ArrayList<>();
        for(int i=0; i<RUNS; i++){
            Collections.shuffle(users);
            List<CowpiUser> others = new ArrayList<>();
            for(int j=1; j < max-min; j++){
                others.add(users.get(j));
            }

            CowpiUser author = users.get(0);

            Map<String, KeyRatchet> oesRatchets = new HashMap<>();
            for(Map.Entry<String, KeyPair> oesKeyPair: oesKeyPairs.entrySet()){
                EphemeralKeyPair prekey = crypto.generateEphemeralKeyPair(0, oesKeyPair.getValue());
                KeyRatchet ratchet = crypto.generateKeyRatchet(author.getName(), author.getLongtermKeyPair(),
                        oesKeyPair.getKey(), oesKeyPair.getValue().getPublicKey());
                EphemeralPublicKey epk = new EphemeralPublicKey(prekey.getId(), prekey.getPublicKey());

                ratchet.setNextEphPublicKey(epk);

                oesRatchets.put(oesKeyPair.getKey(), ratchet);
            }

            SetupSessionCallable callable = setupSessionCallableProvider.get();
            callable.init(i, author, others, oesRatchets);
            setupCallables.add(callable);
        }



        logger.info("sleeping...");
        Thread.sleep(7000);
        logger.info("Setting up..." + max);
        long start = System.currentTimeMillis();
        List<Future<CowpiMessage>> results = executor.invokeAll(setupCallables);

        for(Future<CowpiMessage> future: results){
            future.get();
        }
        long end = System.currentTimeMillis();

        logger.info("Setup complete: " + max + ", " + (end-start));

        return null;
    }
}
