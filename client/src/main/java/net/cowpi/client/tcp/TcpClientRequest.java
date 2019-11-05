package net.cowpi.client.tcp;

import net.cowpi.protobuf.ServerProtos.ServerContainer;
import net.cowpi.protobuf.ServerProtos.ServerContainer.Builder;

import java.util.concurrent.CompletableFuture;

public class TcpClientRequest {

    private final ServerContainer.Builder builder;
    private final CompletableFuture<ServerContainer> responseFuture;

    public TcpClientRequest(Builder builder, CompletableFuture<ServerContainer> responseFuture) {
        this.builder = builder;
        this.responseFuture = responseFuture;
    }

    public Builder getBuilder() {
        return builder;
    }

    public CompletableFuture<ServerContainer> getResponseFuture() {
        return responseFuture;
    }
}
