package net.cowpi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface ManagedService {

    public CompletionStage<Void> start();

    public CompletionStage<Void> shutdown();
}
