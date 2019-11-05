package net.cowpi.perf;

import dagger.BindsInstance;
import dagger.Component;
import net.cowpi.crypto.CryptoModule;
import net.cowpi.perf.config.ConfigModule;
import net.cowpi.perf.config.PerfConfig;
import net.cowpi.perf.router.RouterModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ConfigModule.class, RouterModule.class, CryptoModule.class, PerfModule.class})
public interface PerfServerComponent {

    PerfExperiment experiment();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder config(PerfConfig config);
        PerfServerComponent build();
    }

}
