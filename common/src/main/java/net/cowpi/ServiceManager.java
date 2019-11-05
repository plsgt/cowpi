package net.cowpi;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ServiceManager {

    private final LinkedList<ManagedService> services = new LinkedList<>();
    private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();


    /**
     * Add a {@link ManagedService} to the application.
     * @param service
     */
    public void addService(ManagedService service){
        services.add(service);
    }

    /**
     * Start the servers in order.
     *
     * @return {@link CompletionStage<Void>} completes when the last service starts
     */
    public CompletionStage<Void> start(){
        Iterator<ManagedService> iterator = services.iterator();
        CompletionStage<Void> stage = CompletableFuture.completedFuture(null);
        while(iterator.hasNext()){
            stage.thenCompose(__ -> iterator.next().start());
        }
        return stage;
    }

    public CompletionStage<Void> shutdown(){
        shutdownServices()
                .thenApply(__ -> shutdownFuture.complete(null));
        return shutdownFuture;
    }

    public CompletionStage<Void> getShutdownFuture(){
        return shutdownFuture;
    }

    private CompletionStage<Void> shutdownServices(){
        Iterator<ManagedService> iterator = services.descendingIterator();
        CompletionStage<Void> stage = CompletableFuture.completedFuture(null);
        while(iterator.hasNext()){
            stage.thenCompose(__ -> iterator.next().shutdown());
        }
        return stage;
    }
}
