package net.cowpi.oes.schedule;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import net.cowpi.ManagedService;

@Module
public abstract class ScheduleModule {

    @Schedule
    @Binds
    abstract ManagedService serviceManager(ScheduleManagedService scheduleManagedService);

    @Provides
    @IntoSet
    static FixedDelayEvent prekeyEvent(UpdatePrekeysFixedDelayEvent event){
        return event;
    }


}
