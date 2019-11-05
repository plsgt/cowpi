package net.cowpi.oes.schedule;

import net.cowpi.ManagedService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
public class ScheduleManagedService implements ManagedService {

    private ScheduledExecutorService schedule;

    private final Set<FixedDelayEvent> fixedDelayEvents;

    @Inject
    public ScheduleManagedService(Set<FixedDelayEvent> fixedDelayEvents) {
        this.fixedDelayEvents = fixedDelayEvents;
    }

    @Override
    public CompletionStage<Void> start() {
        schedule = Executors.newScheduledThreadPool(1);

        for(FixedDelayEvent event: fixedDelayEvents) {
            schedule.scheduleWithFixedDelay(event, 0, event.getDelay(), event.getTimeUnit());
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> shutdown() {
        schedule.shutdown();
        return CompletableFuture.completedFuture(null);
    }
}
