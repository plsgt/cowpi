package net.cowpi.server.oes;

public class OesLoginEvent {

    private final Long oesId;

    public OesLoginEvent(Long oesId) {
        this.oesId = oesId;
    }

    public Long getOesId() {
        return oesId;
    }
}
