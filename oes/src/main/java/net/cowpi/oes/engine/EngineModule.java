package net.cowpi.oes.engine;

import dagger.Module;
import dagger.Provides;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Module
public abstract class EngineModule {

    @Provides
    static ThreadFactory threadFactory(){
        return Executors.defaultThreadFactory();
    }

}
