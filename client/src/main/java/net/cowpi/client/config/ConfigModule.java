package net.cowpi.client.config;

import dagger.Module;
import dagger.Provides;

import java.net.InetSocketAddress;

@Module
public class ConfigModule {

    @Provides
    @ServerAddress
    public static InetSocketAddress serverAddress(ClientConfiguration config){
        return config.getTcpServerAddress();
    }

}
