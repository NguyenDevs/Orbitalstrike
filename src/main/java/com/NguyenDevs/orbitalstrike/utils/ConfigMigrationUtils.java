package com.NguyenDevs.orbitalstrike.utils;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            if (!section.isConfigurationSection(key + ".item") && !section.isList(key + ".item")) {
                 if (!section.contains(key + ".item")) {
                     migrationNeeded = true;
                     break;
                 }
            }
            
            if (!section.isList(key + ".item")) {
                migrationNeeded = true;
                break;
            }
        }
        
        if (migrationNeeded) {
            plugin.getLogger().info("Migrating cannons.yml to new format...");
            FileConfiguration newConfig = new YamlConfiguration();
            ConfigurationSection newCannonsSection = newConfig.createSection("cannons");
            
            for (String key : section.getKeys(false)) {
                ConfigurationSection oldCannon = section.getConfigurationSection(key);
                String name = oldCannon.getString("name");
                String payloadType = oldCannon.getString("payload", "STAB");
                
                // Create new section
                ConfigurationSection newCannon = newCannonsSection.createSection(key);
                newCannon.set("name", name);
                
                // Item section
                List<Map<String, Object>> itemList = new ArrayList<>();
                itemList.add(Map.of("material", plugin.getConfigManager().getDefaultItemMaterial().name()));
                itemList.add(Map.of("durability", plugin.getConfigManager().isDefaultItemDurabilityEnabled()));
                itemList.add(Map.of("max-durability", plugin.getConfigManager().getDefaultItemMaxDurability()));
                newCannon.set("item", itemList);
                
                newCannon.set("cooldown", plugin.getConfigManager().getDefaultItemCooldown());
                
                // Payload section
                List<Map<String, Object>> payloadSettings = new ArrayList<>();
                for (String paramKey : oldCannon.getKeys(false)) {
                    if (paramKey.equals("name") || paramKey.equals("payload")) continue;
                    payloadSettings.add(Map.of(paramKey, oldCannon.get(paramKey)));
                }
                
                ConfigurationSection payloadSection = newCannon.createSection("payload");
                payloadSection.set("type", payloadType);
                payloadSection.set("settings", payloadSettings);
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
