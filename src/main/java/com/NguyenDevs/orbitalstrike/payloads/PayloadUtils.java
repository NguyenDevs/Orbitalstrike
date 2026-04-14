package com.NguyenDevs.orbitalstrike.payloads;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.models.Cannon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Level;

public class PayloadUtils {

    public static double getDoubleParameter(Cannon cannon, String key, double defaultValue) {
        Object val = cannon.getParameter(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return defaultValue;
    }

    public static float getFloatParameter(Cannon cannon, String key, float defaultValue) {
        Object val = cannon.getParameter(key);
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        return defaultValue;
    }

    public static int getIntParameter(Cannon cannon, String key, int defaultValue) {
        Object val = cannon.getParameter(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return defaultValue;
    }

    public static String getStringParameter(Cannon cannon, String key, String defaultValue) {
        Object val = cannon.getParameter(key);
        if (val instanceof String) {
            return (String) val;
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static java.util.List<String> getStringListParameter(Cannon cannon, String key, java.util.List<String> defaultValue) {
        Object val = cannon.getParameter(key);
        if (val instanceof java.util.List) {
            return (java.util.List<String>) val;
        }
        return defaultValue;
    }

    public static boolean getBooleanParameter(Cannon cannon, String key, boolean defaultValue) {
        Object val = cannon.getParameter(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return defaultValue;
    }

    public static TNTPrimed spawnTNTAt(OrbitalStrike plugin, World world, Location loc, float yield, int fuse, boolean invulnerable, NamespacedKey key) {
        try {
            if (fuse == 0 && loc.getBlock().isLiquid()) return null;

            TNTPrimed tnt = (TNTPrimed) world.spawnEntity(loc, EntityType.PRIMED_TNT);
            tnt.setFuseTicks(fuse);
            tnt.setYield(yield);
            tnt.setIsIncendiary(false);
            if (invulnerable) {
                tnt.setInvulnerable(true);
            }

            tnt.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

            return tnt;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to spawn TNT", e);
            return null;
        }
    }

    public static Location findGroundLevel(World world, Location start) {
        Location ground = start.clone();
        if (ground.getY() > world.getMaxHeight()) ground.setY(world.getMaxHeight() - 1);

        while (ground.getY() > world.getMinHeight() && ground.getBlock().getType().isAir()) {
            ground.subtract(0, 1, 0);
        }
        return ground.add(0, 1, 0);
    }
}
