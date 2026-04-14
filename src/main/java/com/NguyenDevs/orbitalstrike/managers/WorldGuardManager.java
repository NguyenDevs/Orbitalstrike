package com.NguyenDevs.orbitalstrike.managers;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class WorldGuardManager {

    private final OrbitalStrike plugin;
    private boolean worldGuardEnabled = false;
    private WorldGuardHook hook;

    public WorldGuardManager(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    public static void safeRegisterFlag() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            try {


                new WorldGuardHook().registerFlag();
            } catch (Throwable t) {

                Bukkit.getLogger().log(Level.WARNING, "[OrbitalStrike] Failed to register WorldGuard flag: " + t.getMessage());
            }
        }
    }

    public void init() {
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                this.hook = new WorldGuardHook();
                this.worldGuardEnabled = true;
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bOrbital&3Strike&9Cannon&8] &aWorldGuard detected — &3osc-enable&a flag is active."));
            } catch (Throwable t) {
                this.worldGuardEnabled = false;
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bOrbital&3Strike&9Cannon&8] &cWorldGuard detected but hook failed to load."));
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bOrbital&3Strike&9Cannon&8] &cWorldGuard not found — &3osc-enable &aflag check will be skipped."));
        }
    }

    public boolean isAllowed(Player player, Location target) {
        if (!worldGuardEnabled || hook == null) {
            return true;
        }

        try {
            return hook.isAllowed(player, target);
        } catch (Throwable t) {
            plugin.getLogger().warning("WorldGuard flag check failed: " + t.getMessage());
            return true;
        }
    }

    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }
}
