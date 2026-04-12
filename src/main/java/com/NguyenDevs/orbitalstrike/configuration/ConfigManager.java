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
        config.addDefault("settings.disabled-worlds", Arrays.asList("example", "example_nether", "example_the_end"));
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






        config.addDefault("payloads.emp.radius", 12.0);
        config.addDefault("payloads.emp.pulses", 5);
        config.addDefault("payloads.emp.pulse-delay", 20);
        config.addDefault("payloads.emp.pulse-speed", 2.0);
        config.addDefault("payloads.emp.blindness-duration", 60);
        config.addDefault("payloads.emp.weakness-duration", 400);
        config.addDefault("payloads.emp.nausea-duration", 100);
        config.addDefault("payloads.emp.slowness-duration", 100);
        config.addDefault("payloads.emp.slowness-amplifier", 1);



        
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






    public double getEmpRadius() {
        return plugin.getConfig().getDouble("payloads.emp.radius");
    }

    public int getEmpPulses() {
        return plugin.getConfig().getInt("payloads.emp.pulses", 5);
    }

    public int getEmpPulseDelay() {
        return plugin.getConfig().getInt("payloads.emp.pulse-delay", 10);
    }

    public double getEmpPulseSpeed() {
        return plugin.getConfig().getDouble("payloads.emp.pulse-speed", 2.0);
    }

    public int getEmpBlindnessDuration() {
        return plugin.getConfig().getInt("payloads.emp.blindness-duration", 60);
    }

    public int getEmpWeaknessDuration() {
        return plugin.getConfig().getInt("payloads.emp.weakness-duration", 400);
    }

    public int getEmpNauseaDuration() {
        return plugin.getConfig().getInt("payloads.emp.nausea-duration", 100);
    }

    public int getEmpSlownessDuration() {
        return plugin.getConfig().getInt("payloads.emp.slowness-duration", 100);
    }

    public int getEmpSlownessAmplifier() {
        return plugin.getConfig().getInt("payloads.emp.slowness-amplifier", 1);
    }

}

