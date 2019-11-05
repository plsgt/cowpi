package net.cowpi.client.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.util.FutureUtil;

import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class TcpClient {

    private CompletionStage<Void> shutdownFuture;
    private EventLoopGroup workerGroup;

    private Channel channel;

    private InetSocketAddress serverAddress;

    private final TcpChannelInitializer channelInitializer;

    @Inject
    public TcpClient(InetSocketAddress serverAddress, TcpChannelInitializer channelInitializer){
        this.serverAddress = serverAddress;
        this.channelInitializer = channelInitializer;
    }

    public CompletionStage<Void> start(){
        workerGroup = new NioEventLoopGroup();
        shutdownFuture = FutureUtil.toVoidCompletionStage(workerGroup.terminationFuture());

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(channelInitializer);
        ChannelFuture future = bootstrap.connect(serverAddress);
        channel = future.channel();

        return  FutureUtil.toCompletionStage(future);
    }

    public CompletionStage<Void> shutdown(){
        workerGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS);
        return shutdownFuture;
    }

    public CompletionStage<Void> getShutdownFuture(){
        return shutdownFuture;
    }

    public CompletionStage<ServerContainer> writeAndFlush(ServerContainer.Builder builder){

        CompletableFuture<ServerContainer> result = new CompletableFuture<>();

        TcpClientRequest request = new TcpClientRequest(builder, result);

        channel.writeAndFlush(request);

        return result;
    }


}
