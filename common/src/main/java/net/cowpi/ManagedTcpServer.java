package net.cowpi;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.cowpi.util.FutureUtil;

import javax.inject.Provider;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class ManagedTcpServer implements ManagedService {

    private final SocketAddress socketAddress;
    private final ChannelInitializer<?> channelInitializer;

    private final Provider<? extends EventLoopGroup> bossGroupProvider;
    private final Provider<? extends EventLoopGroup> workerGroupProvider;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private CompletionStage<Void> shutdownFuture;

    public ManagedTcpServer(SocketAddress socketAddress, ChannelInitializer<?> channelInitializer, Provider<? extends EventLoopGroup> bossGroupProvider, Provider<? extends EventLoopGroup> workerGroupProvider) {
        this.socketAddress = socketAddress;
        this.channelInitializer = channelInitializer;
        this.bossGroupProvider = bossGroupProvider;
        this.workerGroupProvider = workerGroupProvider;
    }

    private void setupEventLoopGroups(){
        bossGroup = bossGroupProvider.get();
        workerGroup = workerGroupProvider.get();

        CompletionStage<?> bossFuture = FutureUtil.toCompletionStage(bossGroup.terminationFuture());
        CompletionStage<?> workerFuture = FutureUtil.toCompletionStage(workerGroup.terminationFuture());

        shutdownFuture = CompletableFuture.allOf(bossFuture.toCompletableFuture(), workerFuture.toCompletableFuture());
    }

    @Override
    public CompletionStage<Void> start() {
        setupEventLoopGroups();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer);

        CompletionStage<Void> startFuture = FutureUtil.toCompletionStage(bootstrap.bind(socketAddress));

        return startFuture;
    }

    @Override
    public CompletionStage<Void> shutdown() {
        bossGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS);
        workerGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS);
        return shutdownFuture;
    }
}
