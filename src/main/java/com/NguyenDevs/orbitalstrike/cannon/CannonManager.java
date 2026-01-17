package com.NguyenDevs.orbitalstrike.cannon;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.utils.PayloadType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
        
        // Load default parameters from config based on payload type
        if (payloadType == PayloadType.STAB) {
            cannon.setParameter("yield", plugin.getConfigManager().getStabYield());
            cannon.setParameter("offset", plugin.getConfigManager().getStabOffset());
            cannon.setParameter("vertical-step", plugin.getConfigManager().getStabVerticalStep());
        } else if (payloadType == PayloadType.NUKE) {
            cannon.setParameter("yield", plugin.getConfigManager().getNukeYield());
            cannon.setParameter("height", plugin.getConfigManager().getNukeHeight());
            cannon.setParameter("rings", plugin.getConfigManager().getNukeRings());
            cannon.setParameter("base-tnt", plugin.getConfigManager().getNukeBaseTnt());
            cannon.setParameter("tnt-increase", plugin.getConfigManager().getNukeTntIncrease());
            cannon.setParameter("fuse-ticks", plugin.getConfigManager().getNukeFuseTicks());
            cannon.setParameter("launch-delay", plugin.getConfigManager().getNukeLaunchDelay());
        }
        
        // Load default item settings
        cannon.setItemMaterial(plugin.getConfigManager().getDefaultItemMaterial());
        cannon.setDurabilityEnabled(plugin.getConfigManager().isDefaultItemDurabilityEnabled());
        cannon.setMaxDurability(plugin.getConfigManager().getDefaultItemMaxDurability());
        cannon.setCooldown(plugin.getConfigManager().getDefaultItemCooldown());

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
            
            // Load payload type from new structure
            String payloadStr = section.getString(key + ".payload.type", "STAB");
            PayloadType payloadType;
            try {
                payloadType = PayloadType.valueOf(payloadStr);
            } catch (IllegalArgumentException e) {
                payloadType = PayloadType.STAB;
            }

            if (name != null) {
                Cannon cannon = new Cannon(name, payloadType);
                
                // Load item settings
                List<Map<?, ?>> itemList = section.getMapList(key + ".item");
                for (Map<?, ?> itemProp : itemList) {
                    if (itemProp.containsKey("material")) {
                        try {
                            cannon.setItemMaterial(Material.valueOf((String) itemProp.get("material")));
                        } catch (Exception e) {
                            cannon.setItemMaterial(Material.FISHING_ROD);
                        }
                    }
                    if (itemProp.containsKey("durability")) {
                        cannon.setDurabilityEnabled((Boolean) itemProp.get("durability"));
                    }
                    if (itemProp.containsKey("max-durability")) {
                        cannon.setMaxDurability((Integer) itemProp.get("max-durability"));
                    }
                }
                
                cannon.setCooldown(section.getInt(key + ".cooldown", -1));
                
                // Load payload parameters
                List<Map<?, ?>> payloadSettings = section.getMapList(key + ".payload.settings");
                for (Map<?, ?> setting : payloadSettings) {
                    for (Map.Entry<?, ?> entry : setting.entrySet()) {
                        cannon.setParameter((String) entry.getKey(), entry.getValue());
                    }
                }
                
                cannons.put(name.toLowerCase(), cannon);
            }
        }
    }

    public void saveCannons() {
        cannonsConfig = new YamlConfiguration();
        ConfigurationSection section = cannonsConfig.createSection("cannons");

        for (Cannon cannon : cannons.values()) {
            String key = cannon.getName().toLowerCase();
            section.set(key + ".name", cannon.getName());
            
            // Save item settings
            List<Map<String, Object>> itemList = new ArrayList<>();
            itemList.add(Map.of("material", cannon.getItemMaterial().name()));
            itemList.add(Map.of("durability", cannon.isDurabilityEnabled()));
            itemList.add(Map.of("max-durability", cannon.getMaxDurability()));
            section.set(key + ".item", itemList);
            
            section.set(key + ".cooldown", cannon.getCooldown());
            
            // Save payload settings
            ConfigurationSection payloadSection = section.createSection(key + ".payload");
            payloadSection.set("type", cannon.getPayloadType().name());
            
            List<Map<String, Object>> payloadSettings = new ArrayList<>();
            for (Map.Entry<String, Object> entry : cannon.getParameters().entrySet()) {
                payloadSettings.add(Map.of(entry.getKey(), entry.getValue()));
            }
            payloadSection.set("settings", payloadSettings);
        }

        try {
            cannonsConfig.save(cannonsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save cannons.yml!", e);
        }
    }
}
