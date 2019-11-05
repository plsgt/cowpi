package net.cowpi;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.cowpi.util.FutureUtil;

import javax.inject.Provider;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class ManagedTcpClients implements ManagedService {
    private final InetSocketAddress serverAddress;

    private CompletionStage<Void> shutdownFuture;

    private final Provider<? extends EventLoopGroup> workerGroupProvider;

    private final ChannelInitializer<?> channelInitializer;

    private EventLoopGroup workerGroup;

    public ManagedTcpClients(InetSocketAddress serverAddress, Provider<? extends EventLoopGroup> workerGroupProvider, ChannelInitializer<?> channelInitializer) {
        this.serverAddress = serverAddress;
        this.workerGroupProvider = workerGroupProvider;
        this.channelInitializer = channelInitializer;
    }

    @Override
    public CompletionStage<Void> start() {
        workerGroup = workerGroupProvider.get();
        shutdownFuture = FutureUtil.toVoidCompletionStage(workerGroup.terminationFuture());

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> shutdown() {
        workerGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS);
        return shutdownFuture;
    }

    public CompletionStage<Channel> connect(){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(channelInitializer);
        ChannelFuture cf = bootstrap.connect(serverAddress);

        CompletableFuture<Channel> result = new CompletableFuture<>();

        cf.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                result.complete(cf.channel());
            }
        });

        return result;
    }
}
