package net.cowpi.oes.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.cowpi.ManagedService;
import net.cowpi.oes.config.ClientPoolSize;
import net.cowpi.oes.config.RoutingServerOesAddress;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.util.FutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Singleton
public class RoutingServiceClientPool implements ManagedService {
    private static final Logger logger = LoggerFactory.getLogger(RoutingServiceClientPool.class);

    private final int clientPoolSize;

    private NioEventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    private final ReconnectChannelListener reconnectListener = new ReconnectChannelListener();

    private volatile boolean shutDown = true;

    private final List<Channel> initializing = new ArrayList<>();

    private final List<Channel> active = new ArrayList<>();

    @Inject
    RoutingServiceClientPool(@ClientPoolSize int clientPoolSize,
                             @RoutingServerOesAddress SocketAddress serverSocketAddress,
                             OesClientInitializer clientInitializer) {
        this.clientPoolSize = clientPoolSize;
        bootstrap = new Bootstrap()
                .channel(NioSocketChannel.class)
                .handler(clientInitializer)
                .remoteAddress(serverSocketAddress);
    }

    @Override
    public CompletionStage<Void> start() {
        shutDown = false;
        initializing.clear();
        active.clear();

        workerGroup = new NioEventLoopGroup();
        bootstrap.group(workerGroup);

        for(int i=0; i<clientPoolSize; i++){
            Channel channel = bootstrap.connect().channel();
            initializing.add(channel);
            channel.closeFuture().addListener(reconnectListener);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> shutdown() {
        shutDown = true;
        for(Channel channel: initializing){
            channel.close();
        }
        for(Channel channel: active){
            channel.close();
        }
        return FutureUtil.toVoidCompletionStage(workerGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS));
    }

    public synchronized void activateChannel(final Channel channel){
        initializing.remove(channel);
        active.add(channel);
    }

    private synchronized void channelClosed(final Channel channel){
        if(!shutDown){
            initializing.remove(channel);
            active.remove(channel);
            Channel tmp = bootstrap.connect().channel();
            initializing.add(tmp);
            tmp.closeFuture().addListener(reconnectListener);
        }
    }

    public void send(OesContainer container) {
        Channel channel = active.get(0);
        if(channel != null){
            channel.writeAndFlush(container);
        }
    }

    private class ReconnectChannelListener implements ChannelFutureListener {

        @Override
        public void operationComplete(ChannelFuture future) {
            channelClosed(future.channel());
        }
    }
}
