package com.NguyenDevs.orbitalstrike.cannon;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.utils.PayloadType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CannonManager {
    private final OrbitalStrike plugin;
    private final Map<String, Cannon> cannons;
    private final Map<UUID, String> selectedCannons;
    private final File cannonsFile;
    private FileConfiguration cannonsConfig;

    public CannonManager(OrbitalStrike plugin) {
        this.plugin = plugin;
        this.cannons = new HashMap<>();
        this.selectedCannons = new HashMap<>();
        this.cannonsFile = new File(plugin.getDataFolder(), "cannons.yml");
        loadCannons();
    }

    public void createCannon(String name, PayloadType payloadType) {
        Cannon cannon = new Cannon(name, payloadType);
        cannons.put(name.toLowerCase(), cannon);
        saveCannons();
    }

    public void createCannon(String name) {
        createCannon(name, PayloadType.STAB);
    }

    public void removeCannon(String name) {
        cannons.remove(name.toLowerCase());
        saveCannons();
    }

    public Cannon getCannon(String name) {
        return cannons.get(name.toLowerCase());
    }

    public Map<String, Cannon> getCannons() {
        return cannons;
    }

    public void selectCannon(UUID playerId, String cannonName) {
        selectedCannons.put(playerId, cannonName.toLowerCase());
    }

    public Cannon getSelectedCannon(UUID playerId) {
        String name = selectedCannons.get(playerId);
        if (name == null) return null;
        return getCannon(name);
    }

    public void loadCannons() {
        if (!cannonsFile.exists()) {
            return;
        }

        cannonsConfig = YamlConfiguration.loadConfiguration(cannonsFile);
        ConfigurationSection section = cannonsConfig.getConfigurationSection("cannons");
        
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".name");
            String payloadStr = section.getString(key + ".payload", "STAB");
            PayloadType payloadType;
            try {
                payloadType = PayloadType.valueOf(payloadStr);
            } catch (IllegalArgumentException e) {
                payloadType = PayloadType.STAB;
            }

            if (name != null) {
                cannons.put(name.toLowerCase(), new Cannon(name, payloadType));
            }
        }
    }

    public void saveCannons() {
        cannonsConfig = new YamlConfiguration();
        ConfigurationSection section = cannonsConfig.createSection("cannons");

        for (Cannon cannon : cannons.values()) {
            String key = cannon.getName().toLowerCase();
            section.set(key + ".name", cannon.getName());
            section.set(key + ".payload", cannon.getPayloadType().name());
        }

        try {
            cannonsConfig.save(cannonsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save cannons.yml!", e);
        }
    }
}
