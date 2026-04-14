package com.NguyenDevs.orbitalstrike.models;

public class StrikeData {
    private final PayloadType payloadType;

    public StrikeData(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    public PayloadType getPayloadType() {
        return payloadType;
    }
}
