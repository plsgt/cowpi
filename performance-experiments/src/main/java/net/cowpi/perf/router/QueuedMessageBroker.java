package net.cowpi.perf.router;

import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.server.engine.MessageBroker;
import net.cowpi.server.engine.OesBroker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class QueuedMessageBroker implements MessageBroker {

    private final Queue<RouterMessage> messageQueue = new ConcurrentLinkedQueue<>();

    @Inject
    public QueuedMessageBroker(){
    }

    public Queue<RouterMessage> getMessageQueue() {
        return messageQueue;
    }

    @Override
    public void sendMessage(long userId, CowpiMessage message) {
        messageQueue.add(new RouterMessage(userId, message));
    }

    @Override
    public void sendMessages(Map<Long, CowpiMessage> messages) {
        for(Map.Entry<Long, CowpiMessage> entry: messages.entrySet()){
            sendMessage(entry.getKey(), entry.getValue());
        }
    }

}
