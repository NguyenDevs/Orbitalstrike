package com.NguyenDevs.orbitalstrike.configuration;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private final OrbitalStrike plugin;

    public ConfigManager(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        
        // Add default values if missing
        config.addDefault("settings.force-load-target", true);
        config.addDefault("settings.enabled-worlds", Arrays.asList("world", "world_nether", "world_the_end"));
        
        config.addDefault("payloads.stab.yield", 8.0);
        config.addDefault("payloads.stab.offset", 0.3);
        config.addDefault("payloads.stab.vertical-step", 2);
        
        config.addDefault("payloads.nuke.yield", 8.0);
        config.addDefault("payloads.nuke.height", 60.0);
        config.addDefault("payloads.nuke.rings", 10);
        config.addDefault("payloads.nuke.base-tnt", 20);
        config.addDefault("payloads.nuke.tnt-increase", 3);
        config.addDefault("payloads.nuke.fuse-ticks", 80);
        config.addDefault("payloads.nuke.launch-delay", 10);
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    public boolean isForceLoadTarget() {
        return plugin.getConfig().getBoolean("settings.force-load-target");
    }

    public List<String> getEnabledWorlds() {
        return plugin.getConfig().getStringList("settings.enabled-worlds");
    }
    
    public double getStabYield() {
        return plugin.getConfig().getDouble("payloads.stab.yield");
    }
    
    public double getStabOffset() {
        return plugin.getConfig().getDouble("payloads.stab.offset");
    }
    
    public int getStabVerticalStep() {
        return plugin.getConfig().getInt("payloads.stab.vertical-step");
    }
    
    public double getNukeYield() {
        return plugin.getConfig().getDouble("payloads.nuke.yield");
    }
    
    public double getNukeHeight() {
        return plugin.getConfig().getDouble("payloads.nuke.height");
    }
    
    public int getNukeRings() {
        return plugin.getConfig().getInt("payloads.nuke.rings");
    }
    
    public int getNukeBaseTnt() {
        return plugin.getConfig().getInt("payloads.nuke.base-tnt");
    }
    
    public int getNukeTntIncrease() {
        return plugin.getConfig().getInt("payloads.nuke.tnt-increase");
    }
    
    public int getNukeFuseTicks() {
        return plugin.getConfig().getInt("payloads.nuke.fuse-ticks");
    }
    
    public int getNukeLaunchDelay() {
        return plugin.getConfig().getInt("payloads.nuke.launch-delay");
    }
}
