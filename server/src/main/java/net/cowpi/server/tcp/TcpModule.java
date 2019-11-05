package net.cowpi.server.tcp;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.netty.channel.nio.NioEventLoopGroup;
import net.cowpi.server.engine.MessageBroker;
import net.cowpi.server.tcp.user.TcpComponent;

@Module(subcomponents = {TcpComponent.class})
public abstract class TcpModule {

    @Binds
    abstract MessageBroker messageBroker(ChannelBroker channelBroker);

    @Provides
    static NioEventLoopGroup eventLoopGroup(){
        return new NioEventLoopGroup();
    }

}
