package net.cowpi.server.oes;

import dagger.Binds;
import dagger.Module;
import net.cowpi.server.engine.OesBroker;

@Module
public abstract class OesModule {

    @Binds
    public abstract OesBroker oesBroker(TcpOesBroker tcpMirrorBroker);
}
