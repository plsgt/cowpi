package net.cowpi.oes.tcp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.cowpi.oes.engine.OesEngine;
import net.cowpi.protobuf.CiphertextProto.OesLogin;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.OesProto.OesUploadPrekeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class OesClientHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(OesClientHandler.class);

    private final OesEngine oesEngine;
    private final RoutingServiceClientPool clientPool;

    @Inject
    public OesClientHandler(OesEngine oesEngine, RoutingServiceClientPool clientPool) {
        this.oesEngine = oesEngine;
        this.clientPool = clientPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        if(obj instanceof OesContainer){
            OesContainer container = (OesContainer) obj;
            switch (container.getInnerCase()){
                case OES_UPLOAD_PREKEYS:
                    handleUploadOesPrekeys(ctx, container.getOesUploadPrekeys());
                    break;
                case COWPI_MESSAGE:
                    handleCowpiMessage(ctx, container.getCowpiMessage());
                    break;
            }

        }
    }

    private void handleCowpiMessage(ChannelHandlerContext ctx, CowpiMessage message){
        switch (message.getType()){
            case SETUP:
                handleSetupMessage(ctx, message);
                break;
            case RECIEPT:
                handleReceiptMessage(ctx, message);
                break;
            case CONVERSATION:
                handleConversationMessage(ctx, message);
                break;
        }
    }

    private void handleConversationMessage(ChannelHandlerContext ctx, CowpiMessage message) {
        CowpiMessage result = oesEngine.processCowpiMessage(message);
        OesContainer.Builder container = OesContainer.newBuilder()
               .setCowpiMessage(result);

        ctx.writeAndFlush(container.build());
    }

    private void handleReceiptMessage(ChannelHandlerContext ctx, CowpiMessage receiptMessage) {
        CowpiMessage result = oesEngine.processCowpiMessage(receiptMessage);
        OesContainer.Builder container = OesContainer.newBuilder()
               .setCowpiMessage(result);
        ctx.writeAndFlush(container.build());
    }

    private void handleUploadOesPrekeys(ChannelHandlerContext ctx, OesUploadPrekeys oesUploadPrekeys) {
        logger.info("Prekeys requested");
        OesUploadPrekeys prekeys = oesEngine.generatePrekeys(oesUploadPrekeys.getPrekeyCount());
            OesContainer.Builder container = OesContainer.newBuilder()
                    .setOesUploadPrekeys(prekeys);

            ctx.writeAndFlush(container.build());
    }

    private void handleSetupMessage(ChannelHandlerContext ctx, CowpiMessage setupMessage) {
        CowpiMessage result = oesEngine.processCowpiMessage(setupMessage);
            OesContainer.Builder container = OesContainer.newBuilder()
                    .setCowpiMessage(result);
            ctx.writeAndFlush(container.build());

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if(obj instanceof OesLogin){
            clientPool.activateChannel(ctx.channel());
        }
    }
}
