package net.cowpi.client.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import net.cowpi.protobuf.ServerProtos.ServerContainer;

import javax.inject.Inject;
import javax.inject.Provider;

public class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Provider<TcpChannelHandler> handlerProvider;

    @Inject
    public TcpChannelInitializer(Provider<TcpChannelHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast(new LengthFieldPrepender(4));
        channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
        channel.pipeline().addLast(new ProtobufDecoder(ServerContainer.getDefaultInstance()));
        channel.pipeline().addLast(new ProtobufEncoder());
        channel.pipeline().addLast(handlerProvider.get());
    }
}
