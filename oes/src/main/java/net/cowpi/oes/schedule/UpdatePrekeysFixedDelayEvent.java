package net.cowpi.oes.schedule;

import net.cowpi.oes.engine.OesEngine;
import net.cowpi.oes.tcp.RoutingServiceClientPool;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.OesProto.OesUploadPrekeys;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class UpdatePrekeysFixedDelayEvent implements FixedDelayEvent {

    private final RoutingServiceClientPool clientPool;
    private final OesEngine oesEngine;

    @Inject
    UpdatePrekeysFixedDelayEvent(RoutingServiceClientPool clientPool, OesEngine oesEngine){
        this.clientPool = clientPool;
        this.oesEngine = oesEngine;
    }

    @Override
    public long getDelay() {
        return 10;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.HOURS;
    }

    @Override
    public void run() {


        OesUploadPrekeys prekeys = oesEngine.generatePrekeys(10000);
        OesContainer.Builder container = OesContainer.newBuilder()
                .setOesUploadPrekeys(prekeys);

        clientPool.send(container.build());

    }
}
