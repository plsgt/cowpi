package net.cowpi.perf.router;

import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.server.engine.OesBroker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class QueuedOesBroker implements OesBroker {

    private final Map<Long, Queue<OesContainer>> oesQueues = new ConcurrentHashMap<>();

    @Inject
    public QueuedOesBroker(){

    }

    @Override
    public void sendMessage(Map<Long, OesContainer> messages) {
        for(Map.Entry<Long, OesContainer> entry: messages.entrySet()){
            oesQueues.get(entry.getKey()).add(entry.getValue());
        }
    }

    public Map<Long, Queue<OesContainer>> getOesQueues() {
        return oesQueues;
    }
}
