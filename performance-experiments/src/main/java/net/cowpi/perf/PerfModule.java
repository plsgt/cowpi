package net.cowpi.perf;

import dagger.Module;
import dagger.Provides;
import net.cowpi.perf.oes.OesComponent;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.server.engine.MessageBroker;
import net.cowpi.server.engine.OesBroker;

import java.util.Map;

@Module(subcomponents = {OesComponent.class})
public abstract class PerfModule {

}
