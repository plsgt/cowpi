package net.cowpi.server.oes;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.OesProto.OesUploadPrekeys;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.server.engine.CowpiEngine;
import net.cowpi.server.engine.UserEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class OesHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(OesHandler.class);

    private Long oesId = null;

    private final TcpOesBroker oesBroker;
    private final UserEngine userEngine;
    private final CowpiEngine engine;

    @Inject
    OesHandler(TcpOesBroker oesBroker, UserEngine userEngine, CowpiEngine engine){
        this.oesBroker = oesBroker;
        this.userEngine = userEngine;
        this.engine = engine;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) {
        if(oesId == null){
            throw new IllegalStateException();
        }

        if(obj instanceof ServerContainer){
            ServerContainer serverContainer = (ServerContainer) obj;
            if(serverContainer.hasOesContainer()){
                OesContainer container = serverContainer.getOesContainer();
                switch (container.getInnerCase()){
                    case OES_UPLOAD_PREKEYS:
                        handleOesUploadPrekeys(ctx, container.getOesUploadPrekeys());
                        break;
                    case COWPI_MESSAGE:
                        handleCowpiMessage(ctx, container.getCowpiMessage());
                        break;
                }
            }
        }
    }

    private void handleCowpiMessage(ChannelHandlerContext ctx, CowpiMessage cowpiMessage) {
        switch (cowpiMessage.getType()){
            case SETUP:
                handleSetupMessage(ctx, cowpiMessage);
                break;
            case RECIEPT:
                handleReceiptMessage(ctx, cowpiMessage);
                break;
            case CONVERSATION:
                handleConversationMessage(ctx, cowpiMessage);
                break;
        }
    }

    private void handleConversationMessage(ChannelHandlerContext ctx, CowpiMessage conversationMessage) {
        engine.handleOesMessage(oesId, conversationMessage);
    }

    private void handleReceiptMessage(ChannelHandlerContext ctx, CowpiMessage receiptMessage) {
        engine.handleOesMessage(oesId, receiptMessage);
    }

    private void handleSetupMessage(ChannelHandlerContext ctx, CowpiMessage setupMessage) {
        engine.handleOesMessage(oesId, setupMessage);
    }

    private void handleOesUploadPrekeys(ChannelHandlerContext ctx, OesUploadPrekeys uploadPrekeys) {
        logger.info("prekeys uploaded");
        userEngine.addOesPreKeys(oesId, uploadPrekeys.getOesPrekeyList());
        ctx.writeAndFlush(ServerContainer.newBuilder().build());
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
        if(obj instanceof OesContainer){
            OesContainer inner = (OesContainer) obj;

            ServerContainer.Builder container = ServerContainer.newBuilder()
                    .setOesContainer(inner);

            ctx.write(container.build(), promise);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if(oesId != null){
            throw new IllegalStateException();
        }
        if(obj instanceof OesLoginEvent){
            logger.info("Oes Loggin successful");
            OesLoginEvent event = (OesLoginEvent) obj;
            oesId = event.getOesId();
            oesBroker.addOesChannel(oesId, ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(oesId != null){
            oesBroker.remove(oesId, ctx.channel());
        }
    }
}
