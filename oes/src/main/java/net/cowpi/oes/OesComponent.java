package net.cowpi.oes;

import dagger.BindsInstance;
import dagger.Component;
import net.cowpi.ServiceManager;
import net.cowpi.crypto.CryptoModule;
import net.cowpi.oes.config.ConfigModule;
import net.cowpi.oes.config.OesConfig;
import net.cowpi.oes.engine.EngineModule;
import net.cowpi.oes.schedule.ScheduleModule;

import javax.inject.Singleton;

@Singleton
@Component(modules={OesModule.class, ConfigModule.class, CryptoModule.class, ScheduleModule.class, EngineModule.class})
public interface OesComponent {

    @OesServiceManager
    public ServiceManager oesServiceManager();

    @Component.Builder
    public interface Builder {

        @BindsInstance
        Builder config(OesConfig config);
        OesComponent build();
    }
}
