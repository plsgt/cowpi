package net.cowpi.server.test;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.LinkedBlockingQueue;

@ClientScope
@Sharable
public class TestClientHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(TestClientHandler.class);

    private final LinkedBlockingQueue<CiphertextContainer> objects;

    @Inject
    public TestClientHandler(LinkedBlockingQueue<CiphertextContainer> objects) {
        this.objects = objects;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        objects.put((CiphertextContainer) obj);
    }
}
