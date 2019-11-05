package net.cowpi.server.tcp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.server.tcp.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@UserScope
public class TcpServerHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(TcpServerHandler.class);

    private final String username;
    private final long userId;
    private final UserHandler userHandler;
    private final MessageHandler messageHandler;
    private final ChannelBroker broker;

    @Inject
    public TcpServerHandler(@Username String username, @UserId long userId, UserHandler userHandler, MessageHandler messageHandler, ChannelBroker broker) {
        this.username = username;
        this.userId = userId;
        this.userHandler = userHandler;
        this.messageHandler = messageHandler;
        this.broker = broker;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        broker.add(userId, ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        if(obj instanceof ServerContainer){
            ServerContainer container = (ServerContainer) obj;
            switch (container.getInnerCase()){
                case USER_CONTAINER:
                    handleUserContainer(ctx, container);
                    break;
                case COWPI_MESSAGE:
                    messageHandler.handleMessageContainer(ctx, container.getCowpiMessage());
                    break;
                default:
                    break;
            }
        }
    }

    private void handleUserContainer(ChannelHandlerContext ctx, ServerContainer container){
        userHandler.handleUserContainer(ctx, container);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
        if(obj instanceof CowpiMessage){
            CowpiMessage container = (CowpiMessage) obj;
            ServerContainer.Builder builder = ServerContainer.newBuilder()
                    .setCowpiMessage(container);
            ctx.write(builder.build(), promise);
        }
    }
}
