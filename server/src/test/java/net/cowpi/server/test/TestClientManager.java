package net.cowpi.server.test;

import io.netty.channel.nio.NioEventLoopGroup;
import net.cowpi.ManagedService;
import net.cowpi.server.test.TestClientComponent.Builder;
import net.cowpi.util.FutureUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Singleton
public class TestClientManager implements ManagedService {

    private NioEventLoopGroup workerGroup;

    private final Provider<TestClientComponent.Builder> componentBuilder;

    @Inject
    public TestClientManager(Provider<Builder> componentBuilder) {
        this.componentBuilder = componentBuilder;
    }

    public CompletionStage<Void> start(){
        workerGroup = new NioEventLoopGroup();
        return CompletableFuture.completedFuture(null);
    }

    public CompletionStage<Void> shutdown(){
        CompletionStage<Void> f1 = FutureUtil.toVoidCompletionStage(workerGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS));
        return CompletableFuture.allOf(f1.toCompletableFuture());
    }

    public TestClient newTestClient(){
        return componentBuilder.get().workerGroup(workerGroup).build().testClient();
    }

    public TestOes newTestOes(){
        return componentBuilder.get().workerGroup(workerGroup).build().testOes();
    }
}
