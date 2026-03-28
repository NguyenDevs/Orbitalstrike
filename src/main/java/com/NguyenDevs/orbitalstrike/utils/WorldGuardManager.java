package com.NguyenDevs.orbitalstrike.utils;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardManager {

    public static StateFlag OSB_ENABLE_FLAG;

    private final OrbitalStrike plugin;
    private boolean worldGuardEnabled = false;

    public WorldGuardManager(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    public static void registerFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("osc-enable", false);
            registry.register(flag);
            OSB_ENABLE_FLAG = flag;
        } catch (FlagConflictException e) {
            if (registry.get("osc-enable") instanceof StateFlag existing) {
                OSB_ENABLE_FLAG = existing;
            }
        }
    }

    public void init() {
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardEnabled = true;
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bOrbital&3Strike&9Cannon&8] &aWorldGuard detected — &3osc-enable&a flag is active."));
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bOrbital&3Strike&9Cannon&8] &cWorldGuard not found — &3osc-enable &aflag check will be skipped."));
        }
    }

    public boolean isAllowed(Player player, Location target) {
        if (!worldGuardEnabled || OSB_ENABLE_FLAG == null) {
            return true;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            com.sk89q.worldedit.util.Location wgLocation =
                    BukkitAdapter.adapt(target);

            com.sk89q.worldguard.LocalPlayer wgPlayer =
                    WorldGuardPlugin.inst().wrapPlayer(player);

            StateFlag.State state = query.queryState(wgLocation, wgPlayer, OSB_ENABLE_FLAG);

            return state == StateFlag.State.ALLOW;
        } catch (Exception e) {
            plugin.getLogger().warning("WorldGuard flag check failed: " + e.getMessage());
            return true;
        }
    }

    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }
}
