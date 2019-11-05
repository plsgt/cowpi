package net.cowpi.it.server.engine;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.cowpi.server.db.DatabaseFile;
import net.cowpi.server.engine.MessageBroker;
import net.cowpi.server.engine.OesBroker;

@Module
public abstract class CowpiEngineModule {

    @Provides
    public static String databaseFile(){
        return ":memory:";
    }

    @Binds
    public abstract MessageBroker messageBroker(FakeMessageBroker fakeMessageBroker);

    @Binds
    public abstract OesBroker mirrorBroker(FakeMirrorBroker fakeMirrorBroker);

}
