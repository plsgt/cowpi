package net.cowpi.it.server.engine;

import net.cowpi.protobuf.MessageProto.MessageContainer;
import net.cowpi.server.engine.MessageBroker;

import javax.inject.Inject;

public class FakeMessageBroker implements MessageBroker {

    @Inject
    public FakeMessageBroker(){

    }

    @Override
    public void sendMessage(String user, MessageContainer message) {

    }
}
