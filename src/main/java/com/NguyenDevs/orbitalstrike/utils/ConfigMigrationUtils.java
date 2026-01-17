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
        
        // Check if migration is needed (check for old format)
        boolean migrationNeeded = false;
        for (String key : section.getKeys(false)) {
            if (!section.isConfigurationSection(key + ".item") && !section.isList(key + ".item")) {
                 // If item is not a section or list (in new format it should be a list of maps, but we check if old format exists)
                 // Actually, the old format didn't have 'item' section at all usually, or it was different.
                 // The prompt implies we are changing TO a format where item is a list of properties.
                 // Let's check if the structure matches the new one.
                 
                 // New format:
                 // item:
                 //   - material: ...
                 //   - durability: ...
                 
                 // If we don't see this structure, we might need to migrate.
                 // However, since the user said "migrate cannons.yml for current version", 
                 // and the previous code didn't seem to have item configuration in cannons.yml at all (it was using defaults),
                 // we should probably just update all entries to include the new structure with default values if missing.
                 
                 if (!section.contains(key + ".item")) {
                     migrationNeeded = true;
                     break;
                 }
            }
            
            // Also check payload format
            // Old: payload: NUKE (string)
            // New: payload: NUKE (key with list of attributes?)
            // Wait, the user example:
            // payload: NUKE
            //   - launch-delay: 10
            //   ...
            // This looks like 'payload' is a key, but the value is 'NUKE' AND it has children? 
            // YAML doesn't support a node having a scalar value AND children directly like that unless it's a tag or something specific.
            // But looking at the user's example:
            /*
            payload: NUKE
              - launch-delay: 10
            */
            // This is invalid YAML if 'payload: NUKE' is a scalar and then it has indented list items.
            // It's likely the user meant:
            /*
            payload:
              type: NUKE
              parameters:
                - launch-delay: 10
            */
            // OR
            /*
            payload: NUKE
            payload-settings:
              - launch-delay: 10
            */
            // OR maybe they just want the parameters to be under the cannon root, but the user example shows indentation.
            
            // Let's look closely at the user request:
            /*
            payload: NUKE
              - launch-delay: 10
              - rings: 3
            */
            // If I write this in YAML:
            // payload: NUKE
            //   - launch-delay: 10
            // This is a syntax error in standard YAML.
            
            // Maybe they meant:
            /*
            payload: 
              type: NUKE
              properties:
                - launch-delay: 10
            */
            
            // However, I must follow the user's request as closely as possible.
            // If the user provided invalid YAML structure, I should probably interpret it as:
            // payload: NUKE
            // parameters: ...
            
            // BUT, looking at the previous code:
            // section.getString(key + ".payload", "STAB");
            // And parameters were siblings of payload.
            
            // The user wants:
            /*
            cannons:
              test:
                name: test
                item:
                  - material: FISHING_ROD
                  - durability: true
                  - max-durability: 5
                cooldown: 1
                payload: NUKE
                  - launch-delay: 10
                  ...
            */
            
            // Since "payload: NUKE" with children is invalid, I will assume they want:
            /*
            payload:
              type: NUKE
              settings:
                 - launch-delay: 10
            */
            // OR maybe they just typed it weirdly and meant that payload parameters are listed under payload.
            // But 'NUKE' is the type.
            
            // Let's assume the user wants to group payload settings under a section, maybe named 'payload-settings' or just 'payload' being a section with 'type' and other keys.
            
            // Wait, if I look at the `item` example:
            /*
            item:
              - material: FISHING_ROD
              - durability: true
            */
            // This is a list of maps.
            
            // If I look at `payload`:
            /*
            payload: NUKE
              - launch-delay: 10
            */
            // I will implement it as:
            /*
            payload:
              type: NUKE
              settings:
                launch-delay: 10
            */
            // OR better yet, to keep it simple and valid YAML:
            /*
            payload:
              type: NUKE
              launch-delay: 10
              rings: 3
            */
            // But the user specifically used dashes `-`.
            
            // Let's try to stick to a valid YAML structure that resembles the user's request.
            // Maybe:
            /*
            payload:
              - type: NUKE
              - launch-delay: 10
            */
            // But 'type' isn't a parameter usually.
            
            // Let's look at the user's text again.
            // "payload: NUKE" followed by indented list.
            // I will assume they want the payload parameters to be in a list under the payload key, and the type to be specified somehow.
            // Maybe:
            /*
            payload:
              type: NUKE
              params:
                - launch-delay: 10
            */
            
            // actually, looking at the `item` section, it is a list of single-entry maps.
            // `payload` section in user example looks like `payload: NUKE` then list.
            // I will implement a migration that converts the old flat structure to a nested structure.
            // And I will use a structure that is valid YAML.
            
            // Proposed structure for payload:
            /*
            payload:
              type: NUKE
              settings:
                - launch-delay: 10
                - rings: 3
            */
            // This seems safest.
            
            // However, the user might be confusing YAML syntax.
            // I will try to support the structure:
            /*
            payload:
              type: NUKE
              launch-delay: 10
            */
            // But the user explicitly wrote dashes.
            
            // Let's look at `item` again.
            /*
            item:
              - material: FISHING_ROD
              - durability: true
            */
            
            // So I will use lists for `item` and `payload` settings.
            // For payload type, I'll put it as a separate key or the first item in the list?
            // User: `payload: NUKE`
            //       `- ...`
            // This is definitely invalid.
            
            // I will assume:
            /*
            payload:
              type: NUKE
              settings:
                - launch-delay: 10
            */
            // Or maybe just:
            /*
            payload:
              - type: NUKE
              - launch-delay: 10
            */
            
            // Let's go with:
            /*
            payload:
              type: NUKE
              properties:
                - launch-delay: 10
            */
            
            // Actually, let's look at how I can parse this.
            // If I write the file, I need to decide on a structure.
            
            // Let's try to interpret "payload: NUKE" as a key "payload" with value "NUKE", and the list as a separate key? No, it's indented.
            
            // I will implement the structure:
            /*
            payload:
              type: NUKE
              settings:
                 - launch-delay: 10
            */
            // And for item:
            /*
            item:
              - material: FISHING_ROD
              - durability: true
            */
            
            // Let's check if I can just use a map for item instead of list of maps, it's cleaner.
            // But user asked for:
            // item:
            //   - material: FISHING_ROD
            // So I must use list.
            
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
                // We need to extract payload parameters from the old flat structure
                List<Map<String, Object>> payloadSettings = new ArrayList<>();
                for (String paramKey : oldCannon.getKeys(false)) {
                    if (paramKey.equals("name") || paramKey.equals("payload")) continue;
                    payloadSettings.add(Map.of(paramKey, oldCannon.get(paramKey)));
                }
                
                // Construct payload object
                // Since we can't do "payload: NUKE" with children, we'll do:
                // payload:
                //   type: NUKE
                //   settings:
                //     - key: value
                
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
