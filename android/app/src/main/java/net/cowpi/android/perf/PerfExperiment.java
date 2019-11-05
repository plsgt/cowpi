package net.cowpi.android.perf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class PerfExperiment {
    private static final Logger logger = Logger.getLogger(PerfExperiment.class.toString());
    private final int MIN = 2;
    private final int MAX = 50;

    private final Provider<ExperimentCallable> experimentCallableProvider;

    private ExecutorService experimentThread;
    private ExecutorService executor;

    @Inject
    PerfExperiment(Provider<ExperimentCallable> experimentCallableProvider){
        this.experimentCallableProvider = experimentCallableProvider;
    }

    void setup(){
        experimentThread = Executors.newSingleThreadExecutor();
        executor = Executors.newFixedThreadPool(4);
    }

    void run() throws InterruptedException {
        try {
            warmup();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        List<Callable<Void>> rounds = new ArrayList<>();
        for(int i=MIN; i<=MAX; i++){
            ExperimentCallable callable = experimentCallableProvider.get();
            callable.init(MIN, i, executor);
            rounds.add(callable);
        }

        experimentThread.invokeAll(rounds);
    }

    void warmup() throws ExecutionException, InterruptedException {
        logger.info("Warming up...");
        ExperimentCallable warmup = experimentCallableProvider.get();
        warmup.init(MIN, 10, executor);
        experimentThread.submit(warmup).get();
        logger.info("Warm up complete...");
    }

    void shutdown(){
        experimentThread.shutdown();
        executor.shutdown();
    }

}
