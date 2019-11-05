package net.cowpi.oes.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.cowpi.oes.config.RoutingServerOesAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Singleton
public class TestRoutingServer {
    private static final Logger logger = LoggerFactory.getLogger(TestRoutingServer.class);

    private final SocketAddress socketAddress;
    private final Provider<TestChannelInitializer> initializerProvider;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;


    @Inject
    public TestRoutingServer(@RoutingServerOesAddress SocketAddress socketAddress, Provider<TestChannelInitializer> initializerProvider){
        this.socketAddress = socketAddress;
        this.initializerProvider = initializerProvider;
    }

    public void start() throws InterruptedException {

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        new ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(initializerProvider.get())
            .localAddress(socketAddress)
            .bind().sync();
    }

    public void shutdown() throws ExecutionException, InterruptedException {
        bossGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS).get();
        workerGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS).get();
    }


}
