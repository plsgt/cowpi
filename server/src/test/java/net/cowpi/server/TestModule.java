package net.cowpi.server;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.netty.buffer.ByteBufUtil;
import net.cowpi.server.config.Oes;
import net.cowpi.server.config.RoutingServerConfig;
import net.cowpi.crypto.KeyPair;
import net.cowpi.server.test.TestClientComponent;

import java.util.HashMap;
import java.util.Map;

@Module(subcomponents = {TestClientComponent.class})
public abstract class TestModule {

    @Provides
    public static RoutingServerConfig routingServerConfig(TestServerConfig config){
        return config.getRoutingServerConfig();
    }

    @Provides
    @OesKeyPairs
    public static Map<String, KeyPair> oesKeyPairs(TestServerConfig config){
        Map<String, KeyPair> oesKeys = new HashMap<>();

        for(TestOes oes: config.getOes()){
            byte[] priv = ByteBufUtil.decodeHexDump(oes.getLongtermPrivateKey());
            byte[] pub = ByteBufUtil.decodeHexDump(oes.getLongtermPublicKey());

            KeyPair keyPair = new KeyPair(priv, pub);
            oesKeys.put(oes.getName(), keyPair);
        }

        return oesKeys;
    }

}
