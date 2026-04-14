package com.NguyenDevs.orbitalstrike.models;

import com.NguyenDevs.orbitalstrike.utils.MathEvaluator.Expression;
import org.bukkit.Color;
import org.bukkit.Particle;

public class TrailLayer {
    private final Particle particle;
    private final Color color;
    private final float size;
    private final Expression offsetX;
    private final Expression offsetY;
    private final Expression offsetZ;
    private final double spreadX;
    private final double spreadY;
    private final double spreadZ;
    private final double speed;
    private final int count;

    public TrailLayer(Particle particle, Color color, float size, Expression offsetX, Expression offsetY, Expression offsetZ,
                      double spreadX, double spreadY, double spreadZ, double speed, int count) {
        this.particle = particle;
        this.color = color;
        this.size = size;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.spreadX = spreadX;
        this.spreadY = spreadY;
        this.spreadZ = spreadZ;
        this.speed = speed;
        this.count = count;
    }

    public Particle getParticle() { return particle; }
    public Color getColor() { return color; }
    public float getSize() { return size; }
    public Expression getOffsetX() { return offsetX; }
    public Expression getOffsetY() { return offsetY; }
    public Expression getOffsetZ() { return offsetZ; }
    public double getSpreadX() { return spreadX; }
    public double getSpreadY() { return spreadY; }
    public double getSpreadZ() { return spreadZ; }
    public double getSpeed() { return speed; }
    public int getCount() { return count; }
}
