package net.cowpi.client;

import net.cowpi.client.config.HardcodedClientConfig;

import java.util.concurrent.ExecutionException;

public class Main{

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CowpiComponent cowpiComponent = DaggerCowpiComponent.builder()
                .config(new HardcodedClientConfig())
                .build();

        cowpiComponent.clientServiceManager().start();
    }


}
