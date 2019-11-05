package net.cowpi.it.server.engine;

import net.cowpi.protobuf.MessageProto.MessageContainer;
import net.cowpi.server.engine.OesBroker;

import javax.inject.Inject;

public class FakeMirrorBroker implements OesBroker {

    @Inject
    public FakeMirrorBroker(){

    }

    @Override
    public void sendMessage(MessageContainer message) {

    }
}
