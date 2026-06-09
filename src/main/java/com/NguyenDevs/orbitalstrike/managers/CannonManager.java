package com.NguyenDevs.orbitalstrike.managers;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.models.PayloadType;
import com.NguyenDevs.orbitalstrike.models.Cannon;
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
    private final Map<String, Long> lastUsedMap;
    private final File cannonsFile;
    private FileConfiguration cannonsConfig;

    public CannonManager(OrbitalStrike plugin) {
        this.plugin = plugin;
        this.cannons = new HashMap<>();
        this.lastUsedMap = new HashMap<>();
        this.cannonsFile = new File(plugin.getDataFolder(), "cannons.yml");
        if (!cannonsFile.exists()) {
            plugin.saveResource("cannons.yml", false);
        }
        loadCannons();
    }


    public void createCannon(String name, PayloadType payloadType) {
        Cannon cannon = new Cannon(name, payloadType);

        if (payloadType == PayloadType.STAB) {
            cannon.setParameter("yield", 8.0);
            cannon.setParameter("offset", 0.3);
            cannon.setParameter("vertical-step", 2);

        } else if (payloadType == PayloadType.NUKE) {
            cannon.setParameter("yield", 8.0);
            cannon.setParameter("height", 60.0);
            cannon.setParameter("rings", 10);
            cannon.setParameter("base-tnt", 20);
            cannon.setParameter("tnt-increase", 3);
            cannon.setParameter("fuse-ticks", 80);
            cannon.setParameter("launch-delay", 10);

        } else if (payloadType == PayloadType.RECURSION) {
            cannon.setParameter("yield", 10.0);
            cannon.setParameter("height", 111.0);
            cannon.setParameter("level", 3);
            cannon.setParameter("amount", 5);
            cannon.setParameter("velocity", 0.8);
            cannon.setParameter("split-fuse-ticks", 20);
            cannon.setParameter("last-fuse-ticks", 60);
        } else if (payloadType == PayloadType.EMP) {
            cannon.setParameter("radius", 15.0);
            cannon.setParameter("pulses", 3);
            cannon.setParameter("pulse-delay", 60);
            cannon.setParameter("pulse-speed", 2.5);
            cannon.setParameter("destroy-drop-items", false);
            cannon.setParameter("effects", Arrays.asList(
                    "BLINDNESS:0:60", "WEAKNESS:1:400", "CONFUSION:4:100", "SLOW:1:100"
            ));
            cannon.setParameter("destroyed-blocks", Arrays.asList(
                    "REDSTONE", "REDSTONE_BLOCK", "PISTON", "STICKY_PISTON", "REPEATER", 
                    "COMPARATOR", "DROPPER", "DISPENSER", "CRAFTER", "OBSERVER", 
                    "RAIL", "ACTIVATOR_RAIL", "DETECTOR_RAIL", "POWERED_RAIL", 
                    "DAYLIGHT_DETECTOR", "LEVER"
            ));
        }

        cannon.setItemMaterial(Material.FISHING_ROD);
        cannon.setDurabilityEnabled(true);
        cannon.setMaxDurability(1);
        cannon.setCooldown(-1);

        cannons.put(name.toLowerCase(), cannon);
        saveCannons();
        
        if (plugin.getTrailManager() != null) {
            plugin.getTrailManager().checkAndGenerateDefaults();
        }
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

    public long getLastUsed(String cannonName) {
        return lastUsedMap.getOrDefault(cannonName.toLowerCase(), 0L);
    }

    public void setLastUsed(String cannonName, long time) {
        lastUsedMap.put(cannonName.toLowerCase(), time);
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

            String payloadStr = section.getString(key + ".payload.type", "STAB");
            PayloadType payloadType;
            try {
                payloadType = PayloadType.valueOf(payloadStr);
            } catch (IllegalArgumentException e) {
                payloadType = PayloadType.STAB;
            }

            if (name != null) {
                Cannon cannon = new Cannon(name, payloadType);

                ConfigurationSection itemSection = section.getConfigurationSection(key + ".item");
                if (itemSection != null) {
                    String matName = itemSection.getString("material");
                    if (matName != null) {
                        try {
                            cannon.setItemMaterial(Material.valueOf(matName));
                        } catch (Exception e) {
                            cannon.setItemMaterial(Material.FISHING_ROD);
                        }
                    }
                    cannon.setDurabilityEnabled(itemSection.getBoolean("durability", true));
                    cannon.setMaxDurability(itemSection.getInt("max-durability", 1));
                }
                
                cannon.setCooldown(section.getInt(key + ".cooldown", -1));

                ConfigurationSection settingsSection = section.getConfigurationSection(key + ".payload.settings");
                if (settingsSection != null) {
                    for (String settingKey : settingsSection.getKeys(false)) {
                        cannon.setParameter(settingKey, settingsSection.get(settingKey));
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

            ConfigurationSection itemSection = section.createSection(key + ".item");
            itemSection.set("material", cannon.getItemMaterial().name());
            itemSection.set("durability", cannon.isDurabilityEnabled());
            itemSection.set("max-durability", cannon.getMaxDurability());
            
            section.set(key + ".cooldown", cannon.getCooldown());

            ConfigurationSection payloadSection = section.createSection(key + ".payload");
            payloadSection.set("type", cannon.getPayloadType().name());
            
            ConfigurationSection payloadSettings = payloadSection.createSection("settings");
            for (Map.Entry<String, Object> entry : cannon.getParameters().entrySet()) {
                payloadSettings.set(entry.getKey(), entry.getValue());
            }
        }

        try {
            cannonsConfig.save(cannonsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save cannons.yml!", e);
        }
    }
}
