package net.cowpi.oes.test;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;

import javax.inject.Inject;
import javax.inject.Provider;

public class TestChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private final Provider<TestChannelHandler> handlerProvider;

    @Inject
    public TestChannelInitializer(Provider<TestChannelHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    @Override
    protected void initChannel(NioSocketChannel channel) throws Exception {
        //channel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
        channel.pipeline().addLast(new LengthFieldPrepender(4));
        channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
        channel.pipeline().addLast(new ProtobufDecoder(CiphertextContainer.getDefaultInstance()));
        channel.pipeline().addLast(new ProtobufEncoder());
        channel.pipeline().addLast(handlerProvider.get());
    }
}
