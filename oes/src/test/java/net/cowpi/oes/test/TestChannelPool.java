package net.cowpi.oes.test;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TestChannelPool {
    private static final Logger logger = LoggerFactory.getLogger(TestChannelPool.class);

    private final Provider<TestRoutingChannel> channelProvider;

    private volatile int counter = 0;

    public final Map<Integer, TestRoutingChannel> channels = new ConcurrentHashMap<>();

    @Inject
    public TestChannelPool(Provider<TestRoutingChannel> channelProvider){
        this.channelProvider = channelProvider;
    }

    private synchronized TestRoutingChannel getOrPut(int index){
        TestRoutingChannel routingChannel = channels.get(index);
        if(routingChannel == null){
            routingChannel = channelProvider.get();
            channels.put(index, routingChannel);
        }
        return routingChannel;
    }

    public void addChannel(Channel channel){
        int index = counter++;
        TestRoutingChannel routingChannel = getOrPut(index);
        routingChannel.setChannel(channel);
    }

    public TestRoutingChannel getChannel(int i){
        TestRoutingChannel routingChannel = getOrPut(i);
        routingChannel.await();
        return  routingChannel;
    }

}
