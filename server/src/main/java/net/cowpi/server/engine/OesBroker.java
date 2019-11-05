package net.cowpi.server.engine;

import net.cowpi.protobuf.OesProto.OesContainer;

import java.util.Map;

public interface OesBroker {

    public void sendMessage(Map<Long, OesContainer> messages);
}
