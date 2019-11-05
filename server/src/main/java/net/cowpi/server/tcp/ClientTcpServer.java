package net.cowpi.server.tcp;

import io.netty.channel.nio.NioEventLoopGroup;
import net.cowpi.ManagedTcpServer;
import net.cowpi.server.config.ClientBindAddress;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.net.SocketAddress;

@Singleton
public class ClientTcpServer extends ManagedTcpServer {

    @Inject
    public ClientTcpServer(@ClientBindAddress SocketAddress bindAddress,
                           TcpServerInitializer initializer,
                           Provider<NioEventLoopGroup> bossGroup,
                           Provider<NioEventLoopGroup> workerGroup){
        super(bindAddress, initializer, bossGroup, workerGroup);
    }
}
