package net.cowpi.server.tcp;

import io.netty.channel.Channel;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.server.engine.MessageBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ChannelBroker implements MessageBroker {

    private static final Logger logger = LoggerFactory.getLogger(ChannelBroker.class);

    private final Map<Long, Channel> channels = new ConcurrentHashMap<>();

    @Inject
    public ChannelBroker() {
    }

    public synchronized void add(long userId, Channel channel){
        channels.put(userId, channel);
    }

    public synchronized void remove(int userID, Channel channel){
        Channel tmp = channels.get(userID);
        if(tmp != null && tmp.equals(channel)){
            channels.remove(userID);
        }
    }

    @Override
    public void sendMessage(long userId, CowpiMessage message) {
        Channel channel = channels.get(userId);
        if(channel != null){
            channel.writeAndFlush(message);
        }
    }

    @Override
    public void sendMessages(Map<Long, CowpiMessage> messages) {
        for(Map.Entry<Long, CowpiMessage> entry: messages.entrySet()){
            sendMessage(entry.getKey(), entry.getValue());
        }
    }
}
