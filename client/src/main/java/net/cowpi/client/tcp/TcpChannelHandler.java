package net.cowpi.client.tcp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.cowpi.protobuf.ServerProtos.ServerContainer;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TcpChannelHandler extends ChannelDuplexHandler {

    private int id = 0;

    private final Map<Integer, CompletableFuture<ServerContainer>> replies = new HashMap<>();

    @Inject
    public TcpChannelHandler() {
        super();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
        if(obj instanceof TcpClientRequest){
            TcpClientRequest request = (TcpClientRequest) obj;

            int id = this.id++;

            replies.put(id, request.getResponseFuture());

            ServerContainer container = request.getBuilder()
                    .setId(id)
                    .build();

            ctx.write(container, promise);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        if(obj instanceof ServerContainer){
            ServerContainer container = (ServerContainer) obj;

            CompletableFuture<ServerContainer> future = replies.remove(container.getId());

            if(future != null){
                future.complete(container);
            }
        }
    }


}
