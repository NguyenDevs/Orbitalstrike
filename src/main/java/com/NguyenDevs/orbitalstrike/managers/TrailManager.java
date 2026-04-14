package com.NguyenDevs.orbitalstrike.managers;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.models.TrailConfig;
import com.NguyenDevs.orbitalstrike.models.TrailLayer;
import com.NguyenDevs.orbitalstrike.utils.MathEvaluator;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;

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
                
                List<Map<String, Object>> defaultLayers = new ArrayList<>();
                Map<String, Object> layer = new HashMap<>();
                layer.put("particle", "FLAME");
                layer.put("formula-x", "0");
                layer.put("formula-y", "0.5");
                layer.put("formula-z", "0");
                layer.put("spread", "0.1, 0.1, 0.1");
                layer.put("speed", 0.05);
                layer.put("count", 3);
                defaultLayers.add(layer);
                
                sec.set("layers", defaultLayers);
                changed = true;
            }
        }

        if (changed) {
            config.options().header("=========================================================\n" +
                    "ORBITAL STRIKE - TRAILS CONFIGURATION\n" +
                    "=========================================================\n" +
                    "System supports creating multiple particle layers per cannon.\n" +
                    "- particle: The name of the particle (E.g.: FLAME, LAVA, ELECTRIC_SPARK).\n" +
                    "  * Supports REDSTONE coloring: REDSTONE:<R>,<G>,<B>:<Size> (E.g.: REDSTONE:255,0,0:1.5)\n" +
                    "- formula-x, formula-y, formula-z: Spawn coordinates evaluated via Math expressions.\n" +
                    "  * 't' is the variable representing Time (Ticks). Each tick 't' increments by 1.\n" +
                    "  * Supported functions: sin(t), cos(t), tan(t), sqrt(t), abs(t), +, -, *, /, %\n" +
                    "  * 0.5 is the exact center of the TNT block.\n" +
                    "- spread: The spread potential of the particle in XYZ format. E.g.: \"0.2, 0.2, 0.2\"\n" +
                    "- speed: The movement speed of the particle.\n" +
                    "- count: The amount of particles spawned per tick.\n" +
                    "=========================================================");
            config.options().copyHeader(true);
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save trails.yml", e);
            }
        }

        loadTrails();
    }

    @SuppressWarnings("unchecked")
    private void loadTrails() {
        trails.clear();
        if (!file.exists()) return;
        config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            ConfigurationSection sec = config.getConfigurationSection(key);
            if (sec == null || !sec.contains("layers")) continue;

            List<TrailLayer> parsedLayers = new ArrayList<>();
            List<Map<String, Object>> layersData = (List<Map<String, Object>>) sec.getList("layers");
            if (layersData == null) continue;

            for (Map<String, Object> map : layersData) {
                String particleStr = String.valueOf(map.getOrDefault("particle", "FLAME")).toUpperCase();
                
                Particle particleType;
                Color color = null;
                float size = 1.0f;
                
                String[] parts = particleStr.split(":");
                try {
                    particleType = Particle.valueOf(parts[0]);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid particle type in trails.yml: " + parts[0]);
                    continue;
                }
                
                if (parts.length >= 2 && particleType == Particle.REDSTONE) {
                    String[] rgb = parts[1].split(",");
                    if (rgb.length >= 3) {
                        try {
                            int r = Integer.parseInt(rgb[0]);
                            int g = Integer.parseInt(rgb[1]);
                            int b = Integer.parseInt(rgb[2]);
                            color = Color.fromRGB(r, g, b);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                
                if (parts.length >= 3 && particleType == Particle.REDSTONE) {
                    try {
                        size = Float.parseFloat(parts[2]);
                    } catch (NumberFormatException ignored) {}
                }

                String formX = String.valueOf(map.getOrDefault("formula-x", "0"));
                String formY = String.valueOf(map.getOrDefault("formula-y", "0"));
                String formZ = String.valueOf(map.getOrDefault("formula-z", "0"));

                MathEvaluator.Expression ex = MathEvaluator.parse(formX);
                MathEvaluator.Expression ey = MathEvaluator.parse(formY);
                MathEvaluator.Expression ez = MathEvaluator.parse(formZ);

                String spreadStr = String.valueOf(map.getOrDefault("spread", "0,0,0"));
                String[] sp = spreadStr.split(",");
                double sx = 0, sy = 0, sz = 0;
                if (sp.length >= 3) {
                    try {
                        sx = Double.parseDouble(sp[0].trim());
                        sy = Double.parseDouble(sp[1].trim());
                        sz = Double.parseDouble(sp[2].trim());
                    } catch (NumberFormatException ignored) {}
                }

                double speed = 0;
                try {
                    speed = Double.parseDouble(String.valueOf(map.getOrDefault("speed", "0")));
                } catch (NumberFormatException ignored) {}

                int count = 1;
                try {
                    count = Integer.parseInt(String.valueOf(map.getOrDefault("count", "1")));
                } catch (NumberFormatException ignored) {}

                parsedLayers.add(new TrailLayer(particleType, color, size, ex, ey, ez, sx, sy, sz, speed, count));
            }

            if (!parsedLayers.isEmpty()) {
                trails.put(key.toLowerCase(), new TrailConfig(parsedLayers));
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

                Location center = tnt.getLocation();
                World w = center.getWorld();
                if (w == null) return;

                for (TrailLayer layer : conf.getLayers()) {
                    double ox = layer.getOffsetX().evaluate(tick);
                    double oy = layer.getOffsetY().evaluate(tick);
                    double oz = layer.getOffsetZ().evaluate(tick);
                    
                    Location spawnLoc = center.clone().add(ox, oy, oz);
                    
                    Object data = null;
                    if (layer.getParticle() == Particle.REDSTONE) {
                        data = new Particle.DustOptions(layer.getColor() != null ? layer.getColor() : Color.RED, layer.getSize());
                    }

                    w.spawnParticle(layer.getParticle(), spawnLoc, layer.getCount(), 
                                    layer.getSpreadX(), layer.getSpreadY(), layer.getSpreadZ(), 
                                    layer.getSpeed(), data);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
