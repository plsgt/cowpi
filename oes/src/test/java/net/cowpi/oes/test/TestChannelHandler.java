package net.cowpi.oes.test;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.LinkedBlockingQueue;

public class TestChannelHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(TestChannelHandler.class);

    private final TestChannelPool pool;

    private LinkedBlockingQueue<CiphertextContainer> messageQueue;

    @Inject
    TestChannelHandler(TestChannelPool pool) {
        this.pool = pool;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        pool.addChannel(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        messageQueue.put((CiphertextContainer) obj);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
        if(obj instanceof  LinkedBlockingQueue){
            messageQueue = (LinkedBlockingQueue<CiphertextContainer>) obj;
        }
        else {
            super.write(ctx, obj, promise);
        }
    }
}
