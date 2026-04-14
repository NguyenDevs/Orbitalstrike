package com.NguyenDevs.orbitalstrike.models;

import org.bukkit.Particle;
import java.util.List;

public class TrailConfig {
    private final List<Particle> particles;
    private final List<TrailPosition> positions;
    private final List<TrailEffect> effects;

    public TrailConfig(List<Particle> particles, List<TrailPosition> positions, List<TrailEffect> effects) {
        this.particles = particles;
        this.positions = positions;
        this.effects = effects;
    }

    public List<Particle> getParticles() { return particles; }
    public List<TrailPosition> getPositions() { return positions; }
    public List<TrailEffect> getEffects() { return effects; }
}
