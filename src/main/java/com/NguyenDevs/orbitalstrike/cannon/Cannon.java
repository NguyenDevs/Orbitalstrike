package com.NguyenDevs.orbitalstrike.cannon;

import com.NguyenDevs.orbitalstrike.utils.PayloadType;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class Cannon {
    private final String name;
    private PayloadType payloadType;
    private final Map<String, Object> parameters;

    private Material itemMaterial;
    private boolean durabilityEnabled;
    private int maxDurability;
    private int cooldown;

    public Cannon(String name) {
        this(name, PayloadType.STAB);
    }

    public Cannon(String name, PayloadType payloadType) {
        this.name = name;
        this.payloadType = payloadType;
        this.parameters = new HashMap<>();

        this.itemMaterial = Material.FISHING_ROD;
        this.durabilityEnabled = true;
        this.maxDurability = 1;
        this.cooldown = -1;
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

    public Material getItemMaterial() {
        return itemMaterial;
    }

    public void setItemMaterial(Material itemMaterial) {
        this.itemMaterial = itemMaterial;
    }

    public boolean isDurabilityEnabled() {
        return durabilityEnabled;
    }

    public void setDurabilityEnabled(boolean durabilityEnabled) {
        this.durabilityEnabled = durabilityEnabled;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public void setMaxDurability(int maxDurability) {
        this.maxDurability = maxDurability;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
}
