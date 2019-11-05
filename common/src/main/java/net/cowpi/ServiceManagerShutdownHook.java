package net.cowpi;

public class ServiceManagerShutdownHook extends Thread {

    private final ServiceManager serviceManager;

    public ServiceManagerShutdownHook(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void run() {
        serviceManager.shutdown().toCompletableFuture().join();
    }
}
