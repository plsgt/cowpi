package net.cowpi.server.oes;

import io.netty.channel.Channel;

import java.util.*;

public class OesChannelPool {

    private final List<Channel> channels = new ArrayList<>();
    private volatile int next = 0;

    synchronized void add(Channel channel){
        channels.add(channel);
    }

    synchronized void remove(Channel channel){
        channels.remove(channel);
    }

    synchronized Channel get(){
        if(channels.size() == 0){
            return null;
        }

        if(next >= channels.size()){
            next = 0;
        }

        Channel channel = channels.get(next);
        next++;
        return channel;
    }

    synchronized Channel get(Long conversationId){
        int index = conversationId.intValue() % channels.size();

        return channels.get(index);
    }

}
