package net.cowpi.server.oes;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;

import javax.inject.Inject;
import javax.inject.Provider;

public class OesServerInitializer extends ChannelInitializer<SocketChannel> {

    private final Provider<OesCiphertextHandler> handlerProvider;
    private final Provider<OesHandler> oesHandlerProvider;

    @Inject
    public OesServerInitializer(Provider<OesCiphertextHandler> handlerProvider, Provider<OesHandler> oesHandlerProvider){
        this.handlerProvider = handlerProvider;
        this.oesHandlerProvider = oesHandlerProvider;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast(new LengthFieldPrepender(4));
        channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
        channel.pipeline().addLast(new ProtobufDecoder(CiphertextContainer.getDefaultInstance()));
        channel.pipeline().addLast(new ProtobufEncoder());
        channel.pipeline().addLast(handlerProvider.get());
        channel.pipeline().addLast(oesHandlerProvider.get());
    }
}
