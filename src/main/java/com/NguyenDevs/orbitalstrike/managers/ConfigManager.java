package com.NguyenDevs.orbitalstrike.managers;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import org.bukkit.Material;
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

        config.addDefault("settings.force-load-target", true);
        config.addDefault("settings.disabled-worlds", Arrays.asList("example", "example_nether", "example_the_end"));
        config.addDefault("settings.logs", true);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    public boolean isForceLoadTarget() {
        return plugin.getConfig().getBoolean("settings.force-load-target");
    }

    public List<String> getDisabledWorlds() {
        return plugin.getConfig().getStringList("settings.disabled-worlds");
    }

    public boolean isLogsEnabled() {
        return plugin.getConfig().getBoolean("settings.logs", true);
    }

}

