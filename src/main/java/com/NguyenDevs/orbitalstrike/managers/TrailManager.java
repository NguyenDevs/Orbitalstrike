package com.NguyenDevs.orbitalstrike.managers;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.models.Cannon;
import com.NguyenDevs.orbitalstrike.models.TrailConfig;
import com.NguyenDevs.orbitalstrike.models.TrailEffect;
import com.NguyenDevs.orbitalstrike.models.TrailPosition;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TrailManager {
    private final OrbitalStrike plugin;
    private final Map<String, TrailConfig> trails;
    private final File file;
    private FileConfiguration config;

    public TrailManager(OrbitalStrike plugin) {
        this.plugin = plugin;
        this.trails = new HashMap<>();
        this.file = new File(plugin.getDataFolder(), "trails.yml");
        if (!file.exists()) {
            plugin.saveResource("trails.yml", false);
        }
    }

    public void init() {
        checkAndGenerateDefaults();
    }

    public void checkAndGenerateDefaults() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(file);
        boolean changed = false;

        for (String cannonName : plugin.getCannonManager().getCannons().keySet()) {
            if (!config.contains(cannonName)) {
                ConfigurationSection sec = config.createSection(cannonName);
                sec.set("particle", new ArrayList<String>());
                sec.set("position", new ArrayList<String>());
                sec.set("type", new ArrayList<String>());
                changed = true;
            }
        }

        if (changed) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save trails.yml", e);
            }
        }

        loadTrails();
    }

    private void loadTrails() {
        trails.clear();
        config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            ConfigurationSection sec = config.getConfigurationSection(key);
            if (sec == null) continue;

            List<Particle> particles = new ArrayList<>();
            for (String ps : sec.getStringList("particle")) {
                try {
                    particles.add(Particle.valueOf(ps.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }

            List<TrailPosition> positions = new ArrayList<>();
            for (String pos : sec.getStringList("position")) {
                try {
                    positions.add(TrailPosition.valueOf(pos.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }

            List<TrailEffect> effects = new ArrayList<>();
            for (String typ : sec.getStringList("type")) {
                try {
                    effects.add(TrailEffect.valueOf(typ.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }

            if (!particles.isEmpty()) {
                trails.put(key.toLowerCase(), new TrailConfig(particles, positions, effects));
            }
        }
    }

    public TrailConfig getTrail(String cannonName) {
        if (cannonName == null) return null;
        return trails.get(cannonName.toLowerCase());
    }

    public void startTrailTask(TNTPrimed tnt, String cannonName) {
        TrailConfig conf = getTrail(cannonName);
        if (conf == null) return;

        new BukkitRunnable() {
            double tick = 0;
            @Override
            public void run() {
                if (!tnt.isValid() || tnt.isDead()) {
                    this.cancel();
                    return;
                }

                Location loc = tnt.getLocation();
                World w = loc.getWorld();
                if (w == null) return;

                for (Particle p : conf.getParticles()) {
                    List<Location> baseLocs = new ArrayList<>();

                    if (conf.getPositions().isEmpty()) {
                        baseLocs.add(loc.clone().add(0, 0.5, 0));
                    } else {
                        for (TrailPosition pos : conf.getPositions()) {
                            baseLocs.addAll(calculatePositions(loc, pos));
                        }
                    }

                    for (Location baseLoc : baseLocs) {
                        applyEffectsAndSpawn(w, baseLoc, loc.clone().add(0, 0.5, 0), p, conf.getEffects(), tick);
                    }
                }
                tick++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private List<Location> calculatePositions(Location tntLoc, TrailPosition pos) {
        List<Location> locs = new ArrayList<>();
        switch (pos) {
            case UP:
                locs.add(tntLoc.clone().add(0, 1.0, 0));
                break;
            case DOWN:
                locs.add(tntLoc.clone());
                break;
            case CENTER:
                locs.add(tntLoc.clone().add(0, 0.5, 0));
                break;
            case SIDE:
                locs.add(tntLoc.clone().add(0.5, 0.5, 0));
                locs.add(tntLoc.clone().add(-0.5, 0.5, 0));
                locs.add(tntLoc.clone().add(0, 0.5, 0.5));
                locs.add(tntLoc.clone().add(0, 0.5, -0.5));
                break;
        }
        return locs;
    }

    private void applyEffectsAndSpawn(World w, Location spawnLoc, Location center, Particle p, List<TrailEffect> effects, double tick) {
        if (effects.isEmpty() || effects.contains(TrailEffect.DOT) || effects.contains(TrailEffect.LINE)) {
            w.spawnParticle(p, spawnLoc, 1, 0, 0, 0, 0);
        }

        if (effects.contains(TrailEffect.SPREAD)) {
            w.spawnParticle(p, spawnLoc, 5, 0.3, 0.3, 0.3, 0.05);
        }

        if (effects.contains(TrailEffect.ROTATE) || effects.contains(TrailEffect.SPIRAL) || effects.contains(TrailEffect.CIRCLE)) {
            Vector offset = spawnLoc.toVector().subtract(center.toVector());
            double angle = tick * 0.3;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double rx = offset.getX() * cos - offset.getZ() * sin;
            double rz = offset.getX() * sin + offset.getZ() * cos;
            Location rotated = center.clone().add(rx, offset.getY(), rz);
            w.spawnParticle(p, rotated, 1, 0, 0, 0, 0);
        }
    }
}
