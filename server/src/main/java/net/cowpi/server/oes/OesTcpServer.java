package net.cowpi.server.oes;

import io.netty.channel.nio.NioEventLoopGroup;
import net.cowpi.ManagedTcpServer;
import net.cowpi.server.config.OesBindAddress;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.net.SocketAddress;

@Singleton
public class OesTcpServer extends ManagedTcpServer {

    @Inject
    public OesTcpServer(@OesBindAddress SocketAddress bindAddress,
                        OesServerInitializer initializer,
                        Provider<NioEventLoopGroup> bossGroup,
                        Provider<NioEventLoopGroup> workerGroup){
        super(bindAddress, initializer, bossGroup, workerGroup);
    }
}
