package net.cowpi.server.tcp.user;

import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.protobuf.UserProtos.FetchPrekey;
import net.cowpi.protobuf.UserProtos.PreKey;
import net.cowpi.protobuf.UserProtos.UploadPrekey;
import net.cowpi.protobuf.UserProtos.UserContainer;
import net.cowpi.crypto.EphemeralPublicKey;
import net.cowpi.server.engine.UserEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

@UserScope
public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);

    private final long userId;
    private final String username;
    private final UserEngine userEngine;

    @Inject
    public UserHandler(@UserId long userId, @Username String username, UserEngine userEngine) {
        this.userId = userId;
        this.username = username;
        this.userEngine = userEngine;
    }

    public void handleUserContainer(ChannelHandlerContext ctx, ServerContainer container){
        switch (container.getUserContainer().getInnerCase()){
            case UPLOAD_PREKEY:
                uploadPreKey(ctx, container);
                break;
            case FETCH_PREKEY:
                fetchPreKeys(ctx, container);
                break;
        }
    }

    private void fetchPreKeys(ChannelHandlerContext ctx, ServerContainer container) {
        FetchPrekey fetch = container.getUserContainer().getFetchPrekey();

        FetchPrekey result = userEngine.fetchPreKeys(fetch.getUsersList());
        UserContainer.Builder userContainer = UserContainer.newBuilder()
                .setFetchPrekey(result);

        ServerContainer.Builder serverContainer = ServerContainer.newBuilder()
                .setUserContainer(userContainer);

        ctx.writeAndFlush(serverContainer.build());
    }

    public void uploadPreKey(ChannelHandlerContext ctx, ServerContainer container){
        UploadPrekey uploadPrekey = container.getUserContainer().getUploadPrekey();

        userEngine.addPreKeys(userId, uploadPrekey.getPrekeyList());
       ctx.writeAndFlush(ServerContainer.newBuilder().build());
    }
}
