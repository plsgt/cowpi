package net.cowpi.perf.config;

import dagger.Module;
import dagger.Provides;
import io.netty.buffer.ByteBufUtil;
import net.cowpi.crypto.KeyPair;
import net.cowpi.server.config.OesPublicKeys;

import java.util.HashMap;
import java.util.Map;

@Module
public abstract class ConfigModule {

    @Provides
    @RouterName
    static String routerName(PerfConfig config){
        return config.getRouterName();
    }

    @Provides
    @RouterKeyPair
    static KeyPair routerKeyPair(PerfConfig config){
        byte[] priv = ByteBufUtil.decodeHexDump(config.getRouterPrivateKey());
        byte[] pub = ByteBufUtil.decodeHexDump(config.getRouterPublicKey());
        return new KeyPair(priv, pub);
    }

    @Provides
    @OesPublicKeys
    static Map<String, byte[]> oesPublicKeys(PerfConfig config){
        Map<String, byte[]> result = new HashMap<>();
        for(Oes oes: config.getOes()){
            result.put(oes.getName(), ByteBufUtil.decodeHexDump(oes.getLongtermPublicKey()));
        }
        return result;
    }

    @Provides
    @RouterThreadCount
    static int routerThreadCount(PerfConfig config){
        return config.getRouterThreadCount();
    }

}
