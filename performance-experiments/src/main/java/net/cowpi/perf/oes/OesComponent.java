package net.cowpi.perf.oes;

import dagger.BindsInstance;
import dagger.Subcomponent;
import net.cowpi.perf.config.Oes;

@Subcomponent(modules = {OesModule.class})
@OesScope
public interface OesComponent {

    OesExperiment oesExperiment();

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        Builder oes(Oes oes);
        OesComponent build();

    }
}
