package net.cowpi.oes.test;

import dagger.Module;
import dagger.Provides;
import io.netty.buffer.ByteBufUtil;
import net.cowpi.ManagedService;
import net.cowpi.crypto.KeyPair;
import net.cowpi.oes.TestOesConfig;
import net.cowpi.oes.config.OesConfig;
import net.cowpi.oes.schedule.Schedule;
import net.cowpi.protobuf.CiphertextProto.CiphertextContainer;

import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;

@Module
public abstract class TestModule {

    @Provides
    static OesConfig oesConfig(TestOesConfig config){
        return config.getOesConfig();
    }

    @Provides
    @TestRoutingServerKeyPair
    static KeyPair routingServerKeyPair(TestOesConfig config){
        byte[] priv = ByteBufUtil.decodeHexDump(config.getRouterLongtermPrivateKey());
        byte[] pub = ByteBufUtil.decodeHexDump(config.getRouterLongtermPublicKey());
        return new KeyPair(priv, pub);
    }

    @Provides
    @Singleton
    static LinkedBlockingQueue<CiphertextContainer> messageQueue(){
        return new LinkedBlockingQueue<>();
    }

    @Schedule
    @Provides
    @Singleton
    static ManagedService shedule(){
        return new ManagedService() {
            @Override
            public CompletionStage<Void> start() {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletionStage<Void> shutdown() {
                return CompletableFuture.completedFuture(null);
            }
        };
    }

}
