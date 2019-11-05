package net.cowpi.client.tcp;

import com.google.protobuf.ByteString;
import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.protobuf.UserProtos.UserContainer;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class UserClient {

    private final TcpClient client;

    @Inject
    public UserClient(TcpClient client) {
        this.client = client;
    }

    public CompletionStage<Void> registerUser(String username, byte[] longtermPublicKey){
//        RegisterUser.Builder builder = RegisterUser.newBuilder()
//                .setUsername(username)
//                .setLongtermKey(ByteString.copyFrom(longtermPublicKey));
//
//        AuthenticationContainer.Builder userContainer = AuthenticationContainer.newBuilder()
//                .setRegisterUser(builder);
//
//        ServerContainer.Builder serverContainer = ServerContainer.newBuilder()
//                .setAuthenticationContainer(userContainer);
//
//        return client.writeAndFlush(serverContainer).thenAccept(__ -> {});
        return null;
    }

    public CompletionStage<Void> loginUser(String username) {
//        Login.Builder builder = Login.newBuilder()
//                .setUsername(username);
//
//        AuthenticationContainer.Builder userContainer = AuthenticationContainer.newBuilder()
//                .setLogin(builder);
//
//        ServerContainer.Builder serverContainer = ServerContainer.newBuilder()
//                .setAuthenticationContainer(userContainer);
//
//        return client.writeAndFlush(serverContainer).thenAccept(__ -> {});
        return null;
    }

    public CompletionStage<Void> prekeyUser(String username) {
        //UploadPrekey.Builder builder = UploadPrekey.newBuilder();

        UserContainer.Builder userContainer = UserContainer.newBuilder();
                //.setUploadPrekey(builder);

        ServerContainer.Builder serverContainer = ServerContainer.newBuilder()
                .setUserContainer(userContainer);

        return client.writeAndFlush(serverContainer).thenAccept(__ -> {});
    }

    public CompletionStage<Void> setupConversation(String username, List<String> particpants){

        return null;
    }
}
