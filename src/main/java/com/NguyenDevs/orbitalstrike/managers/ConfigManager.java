package com.NguyenDevs.orbitalstrike.managers;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class ConfigManager {
    private final OrbitalStrike plugin;

    public ConfigManager(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public boolean isForceLoadTarget() {
        return plugin.getConfig().getBoolean("settings.force-load-target", true);
    }

    public List<String> getDisabledWorlds() {
        return plugin.getConfig().getStringList("settings.disabled-worlds");
    }

    public boolean isLogsEnabled() {
        return plugin.getConfig().getBoolean("settings.log-strikes", false);
    }
    
    public int getMaxTntPerStrike() {
        int val = plugin.getConfig().getInt("settings.max-tnt-per-strike", -1);
        return val == -1 ? Integer.MAX_VALUE : Math.max(1, Math.min(5000, val));
    }
}

