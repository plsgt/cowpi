package net.cowpi.server.test;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;

import java.util.concurrent.LinkedBlockingQueue;

@Module
public abstract class TestClientModule {

    @Provides
    @ClientScope
    static LinkedBlockingQueue<CiphertextContainer> messageQueue(){
        return new LinkedBlockingQueue<>();
    }
}
