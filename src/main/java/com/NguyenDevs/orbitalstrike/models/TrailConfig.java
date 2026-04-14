package com.NguyenDevs.orbitalstrike.models;

import java.util.List;

public class TrailConfig {
    private final List<TrailLayer> layers;

    public TrailConfig(List<TrailLayer> layers) {
        this.layers = layers;
    }

    public List<TrailLayer> getLayers() {
        return layers;
    }
}
