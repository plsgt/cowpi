package net.cowpi.client;

import net.cowpi.client.tcp.TcpClient;
import net.cowpi.client.tcp.UserClient;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class CowpiClient {
    private final TcpClient tcpClient;
    private final UserClient userClient;

    @Inject
    protected CowpiClient(TcpClient tcpClient, UserClient userClient) {
        this.tcpClient = tcpClient;
        this.userClient = userClient;
    }

    public CompletionStage<Void> start(){
        return tcpClient.start();
    }

    public CompletionStage<Void> getShutdownFuture(){
        return tcpClient.getShutdownFuture();
    }

    public CompletionStage<Void> shutdown(){
        tcpClient.shutdown();
        return getShutdownFuture();
    }

    public CompletionStage<Void> registerUser(String username, byte[] longtermKey){
        return userClient.registerUser(username, longtermKey);
    }

    public CompletionStage<Void> loginUser(String username) {
        return userClient.loginUser(username);
    }

    public CompletionStage<Void> prekeyUser(String username) {
        return userClient.prekeyUser(username);
    }
}
