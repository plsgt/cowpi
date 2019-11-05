package net.cowpi.server.engine;

import net.cowpi.protobuf.MessageProto.CowpiMessage;

import java.util.Map;

public interface MessageBroker {

    public void sendMessage(long userId, CowpiMessage message);

    void sendMessages(Map<Long, CowpiMessage> messages);
}
