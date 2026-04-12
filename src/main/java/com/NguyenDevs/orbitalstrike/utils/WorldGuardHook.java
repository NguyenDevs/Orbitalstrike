package com.NguyenDevs.orbitalstrike.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * This class handles all direct interaction with the WorldGuard API.
 * It is only loaded if WorldGuard is present on the server.
 */
public class WorldGuardHook {

    public static StateFlag OSC_ENABLE_FLAG;

    public void registerFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("osc-enable", false);
            registry.register(flag);
            OSC_ENABLE_FLAG = flag;
        } catch (FlagConflictException e) {
            if (registry.get("osc-enable") instanceof StateFlag existing) {
                OSC_ENABLE_FLAG = existing;
            }
        }
    }

    public boolean isAllowed(Player player, Location target) {
        if (OSC_ENABLE_FLAG == null) {
            return true;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(target);

            // Check if there are any regions covering this location (excluding global)
            ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
            if (set.size() == 0) {
                // Wilderness: Allow by default
                return true;
            }

            com.sk89q.worldguard.LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            // Inside a region: Default to DENY unless the flag is explicitly set to ALLOW
            StateFlag.State state = query.queryState(wgLocation, wgPlayer, OSC_ENABLE_FLAG);

            return state == StateFlag.State.ALLOW;
        } catch (Exception e) {
            return true;
        }
    }
}
