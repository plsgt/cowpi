package net.cowpi.server.oes;

import io.netty.channel.Channel;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.server.engine.OesBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TcpOesBroker implements OesBroker {
    private static final Logger logger = LoggerFactory.getLogger(TcpOesBroker.class);

    private final Map<Long, OesChannelPool> channels = new ConcurrentHashMap<>();

    @Inject
    TcpOesBroker(){
    }

    synchronized void addOesChannel(Long id, Channel channel) {
        OesChannelPool set = channels.get(id);
        if(set == null){
            set = new OesChannelPool();
            channels.put(id, set);
        }
        set.add(channel);
    }

    synchronized void remove(Long id, Channel channel){
        OesChannelPool set = channels.get(id);
        if(set != null) {
            set.remove(channel);
        }
    }

    private Channel getChannel(Long id, Long conversationId){
        OesChannelPool set = channels.get(id);
        if(set != null){
            return set.get(conversationId);
        }
        else{
            return null;
        }
    }

    private Channel getChannel(Long id){
        OesChannelPool set = channels.get(id);
        if(set != null){
            return set.get();
        }
        else{
            return null;
        }
    }

    @Override
    public void sendMessage(Map<Long, OesContainer> messages) {
        for(Map.Entry<Long, OesContainer> message: messages.entrySet()){
            Channel channel = null;
            if(message.getValue().hasCowpiMessage()) {
                channel = getChannel(message.getKey(), message.getValue().getCowpiMessage().getConversationId());
            }
            else{
                channel = getChannel(message.getKey());
            }
            if(channel != null){
                channel.writeAndFlush(message.getValue());
            }
        }
    }
}
