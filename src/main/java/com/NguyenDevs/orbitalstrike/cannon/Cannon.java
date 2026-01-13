package com.NguyenDevs.orbitalstrike.cannon;

import com.NguyenDevs.orbitalstrike.utils.PayloadType;

import java.util.HashMap;
import java.util.Map;

public class Cannon {
    private final String name;
    private PayloadType payloadType;
    private final Map<String, Object> parameters;

    public Cannon(String name) {
        this(name, PayloadType.STAB);
    }

    public Cannon(String name, PayloadType payloadType) {
        this.name = name;
        this.payloadType = payloadType;
        this.parameters = new HashMap<>();
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

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }
}
