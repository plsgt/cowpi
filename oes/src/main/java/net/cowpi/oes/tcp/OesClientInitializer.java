package net.cowpi.oes.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;

import javax.inject.Inject;
import javax.inject.Provider;

public class OesClientInitializer extends ChannelInitializer<SocketChannel> {

    private final Provider<CiphertextHandler> ciphertextHandlerProvider;
    private final Provider<OesClientHandler> clientHandlerProvider;

    @Inject
    public OesClientInitializer(Provider<CiphertextHandler> ciphertextHandlerProvider, Provider<OesClientHandler> clientHandlerProvider) {
        this.ciphertextHandlerProvider = ciphertextHandlerProvider;
        this.clientHandlerProvider = clientHandlerProvider;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        //channel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
        channel.pipeline().addLast(new LengthFieldPrepender(4));
        channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
        channel.pipeline().addLast(new ProtobufDecoder(CiphertextContainer.getDefaultInstance()));
        channel.pipeline().addLast(new ProtobufEncoder());
        channel.pipeline().addLast(ciphertextHandlerProvider.get());
        channel.pipeline().addLast(clientHandlerProvider.get());
    }
}
