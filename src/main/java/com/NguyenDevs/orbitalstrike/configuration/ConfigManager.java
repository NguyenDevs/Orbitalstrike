package com.NguyenDevs.orbitalstrike.configuration;

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
        config.addDefault("settings.enabled-worlds", Arrays.asList("world", "world_nether", "world_the_end"));
        config.addDefault("settings.logs", true);

        config.addDefault("items.material", "FISHING_ROD");
        config.addDefault("items.durability", true);
        config.addDefault("items.max-durability", 1);
        config.addDefault("items.cooldown", -1);
        
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

        config.addDefault("payloads.recursion.yield", 10.0);
        config.addDefault("payloads.recursion.height", 111.0);
        config.addDefault("payloads.recursion.level", 3);
        config.addDefault("payloads.recursion.amount", 5);
        config.addDefault("payloads.recursion.velocity", 0.8);
        config.addDefault("payloads.recursion.split-fuse-ticks", 20);
        config.addDefault("payloads.recursion.last-fuse-ticks", 60);

        config.addDefault("payloads.plasma.duration", 100);
        config.addDefault("payloads.plasma.damage", 4.0);
        config.addDefault("payloads.plasma.radius", 3.0);
        config.addDefault("payloads.plasma.melting", true);

        config.addDefault("payloads.singularity.duration", 60);
        config.addDefault("payloads.singularity.pull-force", 0.5);
        config.addDefault("payloads.singularity.radius", 10.0);
        config.addDefault("payloads.singularity.yield", 12.0);

        config.addDefault("payloads.cluster.split-height", 40.0);
        config.addDefault("payloads.cluster.amount", 15);
        config.addDefault("payloads.cluster.yield", 6.0);
        config.addDefault("payloads.cluster.scatter", 1.2);

        config.addDefault("payloads.emp.radius", 12.0);
        config.addDefault("payloads.emp.duration", 200);

        
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    public boolean isForceLoadTarget() {
        return plugin.getConfig().getBoolean("settings.force-load-target");
    }

    public List<String> getEnabledWorlds() {
        return plugin.getConfig().getStringList("settings.enabled-worlds");
    }

    public boolean isLogsEnabled() {
        return plugin.getConfig().getBoolean("settings.logs", true);
    }

    public Material getDefaultItemMaterial() {
        String matName = plugin.getConfig().getString("items.material", "FISHING_ROD");
        try {
            return Material.valueOf(matName);
        } catch (IllegalArgumentException e) {
            return Material.FISHING_ROD;
        }
    }

    public boolean isDefaultItemDurabilityEnabled() {
        return plugin.getConfig().getBoolean("items.durability", true);
    }

    public int getDefaultItemMaxDurability() {
        return plugin.getConfig().getInt("items.max-durability", 1);
    }

    public int getDefaultItemCooldown() {
        return plugin.getConfig().getInt("items.cooldown", -1);
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



    public double getRecursionYield() {
        return plugin.getConfig().getDouble("payloads.recursion.yield");
    }

    public double getRecursionHeight() {
        return plugin.getConfig().getDouble("payloads.recursion.height");
    }

    public int getRecursionLevel() {
        return plugin.getConfig().getInt("payloads.recursion.level");
    }

    public int getRecursionAmount() {
        return plugin.getConfig().getInt("payloads.recursion.amount");
    }

    public double getRecursionVelocity() {
        return plugin.getConfig().getDouble("payloads.recursion.velocity");
    }

    public int getRecursionSplitFuseTicks() {
        return plugin.getConfig().getInt("payloads.recursion.split-fuse-ticks");
    }

    public int getRecursionLastFuseTicks(){
        return plugin.getConfig().getInt("payloads.recursion.last-fuse-ticks");
    }

    public int getPlasmaDuration() {
        return plugin.getConfig().getInt("payloads.plasma.duration");
    }

    public double getPlasmaDamage() {
        return plugin.getConfig().getDouble("payloads.plasma.damage");
    }

    public double getPlasmaRadius() {
        return plugin.getConfig().getDouble("payloads.plasma.radius");
    }

    public boolean isPlasmaMeltingEnabled() {
        return plugin.getConfig().getBoolean("payloads.plasma.melting");
    }

    public int getSingularityDuration() {
        return plugin.getConfig().getInt("payloads.singularity.duration");
    }

    public double getSingularityPullForce() {
        return plugin.getConfig().getDouble("payloads.singularity.pull-force");
    }

    public double getSingularityRadius() {
        return plugin.getConfig().getDouble("payloads.singularity.radius");
    }

    public double getSingularityYield() {
        return plugin.getConfig().getDouble("payloads.singularity.yield");
    }

    public double getClusterSplitHeight() {
        return plugin.getConfig().getDouble("payloads.cluster.split-height");
    }

    public int getClusterAmount() {
        return plugin.getConfig().getInt("payloads.cluster.amount");
    }

    public double getClusterYield() {
        return plugin.getConfig().getDouble("payloads.cluster.yield");
    }

    public double getClusterScatter() {
        return plugin.getConfig().getDouble("payloads.cluster.scatter");
    }

    public double getEmpRadius() {
        return plugin.getConfig().getDouble("payloads.emp.radius");
    }

    public int getEmpDuration() {
        return plugin.getConfig().getInt("payloads.emp.duration");
    }
}

