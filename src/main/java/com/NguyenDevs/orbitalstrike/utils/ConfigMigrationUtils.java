package com.NguyenDevs.orbitalstrike.utils;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class ConfigMigrationUtils {

    public static void migrateCannonsYml(OrbitalStrike plugin) {
        File cannonsFile = new File(plugin.getDataFolder(), "cannons.yml");
        if (!cannonsFile.exists()) return;

        FileConfiguration cannonsConfig = YamlConfiguration.loadConfiguration(cannonsFile);
        ConfigurationSection section = cannonsConfig.getConfigurationSection("cannons");
        
        if (section == null) return;
        
        boolean migrationNeeded = false;
        for (String key : section.getKeys(false)) {
            ConfigurationSection payloadSec = section.getConfigurationSection(key + ".payload");
            if (payloadSec == null) {
                migrationNeeded = true;
                break;
            }
            String typeStr = payloadSec.getString("type", "");
            if (typeStr.startsWith("MemorySection[") || payloadSec.isList("settings")) {
                migrationNeeded = true;
                break;
            }
        }
        
        if (migrationNeeded) {
            plugin.getLogger().info("Migrating cannons.yml to new format...");
            
            FileConfiguration defaultConfig = null;
            java.io.InputStream defaultStream = plugin.getResource("cannons.yml");
            if (defaultStream != null) {
                try (InputStreamReader reader = new InputStreamReader(defaultStream, StandardCharsets.UTF_8)) {
                    defaultConfig = YamlConfiguration.loadConfiguration(reader);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Could not load default cannons.yml", e);
                }
            }
            ConfigurationSection defaultCannons = defaultConfig != null ? defaultConfig.getConfigurationSection("cannons") : null;
            
            FileConfiguration newConfig = new YamlConfiguration();
            ConfigurationSection newCannonsSection = newConfig.createSection("cannons");
            
            for (String key : section.getKeys(false)) {
                ConfigurationSection oldCannon = section.getConfigurationSection(key);
                String name = oldCannon.getString("name");

                ConfigurationSection newCannon = newCannonsSection.createSection(key);
                newCannon.set("name", name);

                ConfigurationSection itemSection = newCannon.createSection("item");
                ConfigurationSection payloadSec = oldCannon.getConfigurationSection("payload");

                boolean isCorrupted = false;
                if (payloadSec != null) {
                    String typeStr = payloadSec.getString("type", "");
                    if (typeStr.startsWith("MemorySection[") || payloadSec.isList("settings")) {
                        isCorrupted = true;
                    }
                }

                if (isCorrupted) {
                    ConfigurationSection defCannon = defaultCannons != null ? defaultCannons.getConfigurationSection(key) : null;
                    if (defCannon != null) {
                        ConfigurationSection defItem = defCannon.getConfigurationSection("item");
                        if (defItem != null) {
                            for (String defKey : defItem.getKeys(false)) {
                                itemSection.set(defKey, defItem.get(defKey));
                            }
                        }
                        newCannon.set("cooldown", defCannon.getInt("cooldown", -1));
                        ConfigurationSection defPayload = defCannon.getConfigurationSection("payload");
                        if (defPayload != null) {
                            ConfigurationSection newPayload = newCannon.createSection("payload");
                            newPayload.set("type", defPayload.getString("type", "STAB"));
                            ConfigurationSection defSettings = defPayload.getConfigurationSection("settings");
                            if (defSettings != null) {
                                ConfigurationSection newSettings = newPayload.createSection("settings");
                                for (String sKey : defSettings.getKeys(false)) {
                                    newSettings.set(sKey, defSettings.get(sKey));
                                }
                            } else {
                                newPayload.createSection("settings");
                            }
                        }
                    } else {
                        itemSection.set("material", "FISHING_ROD");
                        itemSection.set("durability", true);
                        itemSection.set("max-durability", 1);
                        newCannon.set("cooldown", -1);
                        ConfigurationSection newPayload = newCannon.createSection("payload");
                        newPayload.set("type", "STAB");
                        newPayload.createSection("settings");
                    }
                } else {
                    itemSection.set("material", oldCannon.getString("material", "FISHING_ROD"));
                    itemSection.set("durability", oldCannon.getBoolean("durability", true));
                    itemSection.set("max-durability", oldCannon.getInt("max-durability", 1));

                    newCannon.set("cooldown", oldCannon.getInt("cooldown", -1));

                    String payloadType = oldCannon.getString("payload", "STAB");
                    ConfigurationSection newPayloadSection = newCannon.createSection("payload");
                    newPayloadSection.set("type", payloadType);

                    ConfigurationSection settingsSection = newPayloadSection.createSection("settings");
                    for (String paramKey : oldCannon.getKeys(false)) {
                        if (paramKey.equals("name") || paramKey.equals("payload") || 
                            paramKey.equals("material") || paramKey.equals("durability") || 
                            paramKey.equals("max-durability") || paramKey.equals("cooldown") ||
                            paramKey.equals("item")) continue;
                        settingsSection.set(paramKey, oldCannon.get(paramKey));
                    }
                }
            }
            
            try {
                newConfig.save(cannonsFile);
                plugin.getLogger().info("cannons.yml migrated successfully.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save migrated cannons.yml", e);
            }
        }
    }
}
