package net.cowpi.perf.router;

import net.cowpi.protobuf.MessageProto.CowpiMessage;

public class RouterMessage {

    private final long userId;
    private final CowpiMessage message;

    public RouterMessage(long userId, CowpiMessage message) {
        this.userId = userId;
        this.message = message;
    }

    public long getUserId() {
        return userId;
    }

    public CowpiMessage getMessage() {
        return message;
    }
}
