package net.cowpi.client.tcp;

import dagger.Module;
import dagger.Provides;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import net.cowpi.ManagedTcpClients;
import net.cowpi.client.config.ServerAddress;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.net.InetSocketAddress;

@Module
public abstract class TcpModule {

    @Provides
    public static NioEventLoopGroup eventLoopGroup(){
        return new NioEventLoopGroup();
    }

    @Provides
    @CowpiManagedClients
    @Singleton
    public static ManagedTcpClients cowpiTcpClients(@ServerAddress InetSocketAddress serverAddress,
                                                    Provider<NioEventLoopGroup> workerProvider,
                                                    TcpChannelInitializer channelInitializer){
        return new ManagedTcpClients(serverAddress, workerProvider, channelInitializer);
    }
}
