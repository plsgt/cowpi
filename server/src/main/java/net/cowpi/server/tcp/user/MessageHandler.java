package net.cowpi.server.tcp.user;

import io.netty.channel.ChannelHandlerContext;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.server.engine.CowpiEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@UserScope
public class MessageHandler {
    private static Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final String username;

    private final CowpiEngine engine;

    @Inject
    public MessageHandler(@Username String username, CowpiEngine engine) {
        this.username = username;
        this.engine = engine;
    }

    public void handleMessageContainer(ChannelHandlerContext ctx, CowpiMessage container){
        switch (container.getType()){
            case SETUP:
                setupConversation(ctx, container);
                break;
            case RECIEPT:
                handleReceipt(ctx, container);
                break;
            case CONVERSATION:
                handleConversationMessage(ctx, container);
                break;
        }
    }

    private void handleConversationMessage(ChannelHandlerContext ctx, CowpiMessage conversationMessage) {
        engine.handleConversationMessage(conversationMessage.toBuilder().setAuthor(username).build());
    }

    private void handleReceipt(ChannelHandlerContext ctx, CowpiMessage receiptMessage) {
        CowpiMessage working = receiptMessage.toBuilder()
                .setAuthor(username)
                .build();

        engine.handleReceipt(working);
    }

    private void setupConversation(ChannelHandlerContext ctx, CowpiMessage container) {
        CowpiMessage working = container.toBuilder()
                .setAuthor(username)
                .build();
        engine.setupConversation(working);
    }
}
