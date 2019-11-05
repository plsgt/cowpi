package net.cowpi.server.test;

import dagger.BindsInstance;
import dagger.Module;
import dagger.Subcomponent;
import io.netty.channel.nio.NioEventLoopGroup;


@ClientScope
@Subcomponent(modules = {TestClientModule.class})
public interface TestClientComponent {

    TestClient testClient();
    TestOes testOes();

    @Subcomponent.Builder
    interface Builder{

        @BindsInstance
        Builder workerGroup(@ClientWorkerGroup NioEventLoopGroup eventLoopGroup);
        TestClientComponent build();
    }

}
