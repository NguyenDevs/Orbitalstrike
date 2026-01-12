package com.NguyenDevs.orbitalstrike.cannon;

import com.NguyenDevs.orbitalstrike.utils.PayloadType;

public class Cannon {
    private final String name;
    private PayloadType payloadType;

    public Cannon(String name) {
        this(name, PayloadType.STAB);
    }

    public Cannon(String name, PayloadType payloadType) {
        this.name = name;
        this.payloadType = payloadType;
    }

    public String getName() {
        return name;
    }

    public PayloadType getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }
}
