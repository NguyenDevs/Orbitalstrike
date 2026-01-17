package com.NguyenDevs.orbitalstrike.cannon;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.utils.PayloadType;
import com.NguyenDevs.orbitalstrike.utils.StrikeData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PayloadManager {
    private final OrbitalStrike plugin;
    private final List<ActiveStrike> activeStrikes;
    private final NamespacedKey orbitalStrikeKey;
    private final NamespacedKey recursionLevelKey;
    private final NamespacedKey recursionAmountKey;
    private final NamespacedKey recursionYieldKey;
    private final NamespacedKey recursionVelocityKey;
    private final NamespacedKey recursionSplitFuseKey;
    private final NamespacedKey recursionLastFuseKey;

    public PayloadManager(OrbitalStrike plugin) {
        this.plugin = plugin;
        this.activeStrikes = new ArrayList<>();
        this.orbitalStrikeKey = new NamespacedKey(plugin, "orbital_strike_tnt");
        this.recursionLevelKey = new NamespacedKey(plugin, "recursion_level");
        this.recursionAmountKey = new NamespacedKey(plugin, "recursion_amount");
        this.recursionYieldKey = new NamespacedKey(plugin, "recursion_yield");
        this.recursionVelocityKey = new NamespacedKey(plugin, "recursion_velocity");
        this.recursionSplitFuseKey = new NamespacedKey(plugin, "recursion_split_fuse");
        this.recursionLastFuseKey = new NamespacedKey(plugin, "recursion_last_fuse");
    }

    public NamespacedKey getOrbitalStrikeKey() {
        return orbitalStrikeKey;
    }

    public NamespacedKey getRecursionLevelKey() {
        return recursionLevelKey;
    }

    public NamespacedKey getRecursionAmountKey() {
        return recursionAmountKey;
    }

    public NamespacedKey getRecursionYieldKey() {
        return recursionYieldKey;
    }

    public NamespacedKey getRecursionVelocityKey() {
        return recursionVelocityKey;
    }

    public NamespacedKey getRecursionSplitFuseKey() {
        return recursionSplitFuseKey;
    }

    public NamespacedKey getRecursionLastFuseKey() {
        return recursionLastFuseKey;
    }

    public void initiateStrike(Cannon cannon, StrikeData data, Location target) {
        if (!plugin.getConfigManager().getEnabledWorlds().contains(target.getWorld().getName())) {
            return;
        }

        if (plugin.getConfigManager().isLogsEnabled()) {
            plugin.getLogger().info("Initiating strike at " + target.toString());
        }

        ActiveStrike strike = new ActiveStrike(cannon, data, target);
        activeStrikes.add(strike);

        new BukkitRunnable() {
            @Override
            public void run() {
                executeStrike(strike);
                activeStrikes.remove(strike);
            }
        }.runTaskLater(plugin, 20L);
    }

    private void executeStrike(ActiveStrike strike) {
        Location target = strike.getTarget();
        World world = target.getWorld();
        if (world == null) {
            return;
        }

        if (plugin.getConfigManager().isForceLoadTarget() && !target.getChunk().isLoaded()) {
            target.getChunk().load();
        }

        PayloadType type = strike.getData().getPayloadType();

        if (type == PayloadType.STAB) {
            spawnStab(world, target, strike.getCannon());
        } else if (type == PayloadType.NUKE) {
            spawnNuke(world, target, strike.getCannon());
        } else if (type == PayloadType.RECURSION) {
            spawnRecursion(world, target, strike.getCannon());
        }
    }

    private void spawnStab(World world, Location center, Cannon cannon) {
        Location ground = findGroundLevel(world, center);

        float yield = getFloatParameter(cannon, "yield", (float) plugin.getConfigManager().getStabYield());
        double offset = getDoubleParameter(cannon, "offset", plugin.getConfigManager().getStabOffset());
        int verticalStep = getIntParameter(cannon, "vertical-step", plugin.getConfigManager().getStabVerticalStep());

        int y = (int) ground.getY();
        int minY = world.getMinHeight();

        new BukkitRunnable() {
            int currentY = y;

            @Override
            public void run() {
                if (currentY < minY) {
                    this.cancel();
                    return;
                }

                Location loc = new Location(world, ground.getX(), currentY, ground.getZ());
                spawnTNTAt(world, loc.clone().add(offset, 0, offset), yield, 0, false);
                spawnTNTAt(world, loc.clone().subtract(offset, 0, offset), yield, 0, false);
                currentY -= verticalStep;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnNuke(World world, Location center, Cannon cannon) {
        int rings = getIntParameter(cannon, "rings", plugin.getConfigManager().getNukeRings());
        double height = getDoubleParameter(cannon, "height", plugin.getConfigManager().getNukeHeight());
        float yield = getFloatParameter(cannon, "yield", (float) plugin.getConfigManager().getNukeYield());
        int baseTnt = getIntParameter(cannon, "base-tnt", plugin.getConfigManager().getNukeBaseTnt());
        int increase = getIntParameter(cannon, "tnt-increase", plugin.getConfigManager().getNukeTntIncrease());
        int initialFuse = getIntParameter(cannon, "fuse-ticks", plugin.getConfigManager().getNukeFuseTicks());
        int launchDelay = getIntParameter(cannon, "launch-delay", plugin.getConfigManager().getNukeLaunchDelay());

        Location spawnCenter = center.clone().add(0, height, 0);
        if (spawnCenter.getY() > world.getMaxHeight()) {
            spawnCenter.setY(world.getMaxHeight() - 10);
        }

        List<TntLaunchData> launchDataList = new ArrayList<>();

        TNTPrimed centerTnt = spawnTNTAt(world, spawnCenter.clone(), yield, initialFuse, false);
        if (centerTnt != null) {
            centerTnt.setGravity(false);
            centerTnt.setVelocity(new Vector(0, 0, 0));
            launchDataList.add(new TntLaunchData(centerTnt, center.getX(), center.getZ()));
        }

        for (int ring = 1; ring <= rings; ring++) {
            double radius = ring * 4.0;
            int tntCount = baseTnt + ring * increase;
            double angleStep = 360.0 / tntCount;

            for (int i = 0; i < tntCount; i++) {
                double angle = i * angleStep;
                double targetX = center.getX() + radius * Math.cos(Math.toRadians(angle));
                double targetZ = center.getZ() + radius * Math.sin(Math.toRadians(angle));

                TNTPrimed tnt = spawnTNTAt(world, spawnCenter.clone(), yield, initialFuse, false);
                if (tnt != null) {
                    tnt.setGravity(false);
                    tnt.setVelocity(new Vector(0, 0, 0));
                    launchDataList.add(new TntLaunchData(tnt, targetX, targetZ));
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (TntLaunchData data : launchDataList) {
                    if (!data.tnt.isDead()) {
                        double deltaX = data.targetX - spawnCenter.getX();
                        double deltaZ = data.targetZ - spawnCenter.getZ();
                        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                        Vector velocity;
                        if (distance > 0) {
                            double speed = distance / 25.0;
                            velocity = new Vector(
                                    deltaX / distance * speed,
                                    -1.2,
                                    deltaZ / distance * speed
                            );
                        } else {
                            velocity = new Vector(0, -1.2, 0);
                        }

                        data.tnt.setVelocity(velocity);
                        data.tnt.setGravity(true);
                    }
                }
            }
        }.runTaskLater(plugin, (long) launchDelay);
    }

    private void spawnRecursion(World world, Location center, Cannon cannon) {
        float yield = getFloatParameter(cannon, "yield", (float) plugin.getConfigManager().getRecursionYield());
        double height = getDoubleParameter(cannon, "height", plugin.getConfigManager().getRecursionHeight());
        int level = getIntParameter(cannon, "level", plugin.getConfigManager().getRecursionLevel());
        int amount = getIntParameter(cannon, "amount", plugin.getConfigManager().getRecursionAmount());
        double velocity = getDoubleParameter(cannon, "velocity", plugin.getConfigManager().getRecursionVelocity());
        int splitFuseTicks = getIntParameter(cannon, "split-fuse-ticks", plugin.getConfigManager().getRecursionSplitFuseTicks());
        int lastFuseTicks = getIntParameter(cannon, "last-fuse-ticks", plugin.getConfigManager().getRecursionLastFuseTicks());

        Location spawnLocation = center.clone().add(0, height, 0);

        TNTPrimed tnt = spawnTNTAt(world, spawnLocation, yield, 80, false);
        if (tnt != null) {
            tnt.getPersistentDataContainer().set(recursionLevelKey, PersistentDataType.INTEGER, level);
            tnt.getPersistentDataContainer().set(recursionAmountKey, PersistentDataType.INTEGER, amount);
            tnt.getPersistentDataContainer().set(recursionYieldKey, PersistentDataType.FLOAT, yield);
            tnt.getPersistentDataContainer().set(recursionVelocityKey, PersistentDataType.DOUBLE, velocity);
            tnt.getPersistentDataContainer().set(recursionSplitFuseKey, PersistentDataType.INTEGER, splitFuseTicks);
            tnt.getPersistentDataContainer().set(recursionLastFuseKey, PersistentDataType.INTEGER, lastFuseTicks);
        }
    }

    public void handleRecursionExplosion(TNTPrimed explodedTnt) {
        if (!explodedTnt.getPersistentDataContainer().has(recursionLevelKey, PersistentDataType.INTEGER)) {
            return;
        }

        int level = explodedTnt.getPersistentDataContainer().get(recursionLevelKey, PersistentDataType.INTEGER);
        if (level <= 0) {
            return;
        }

        int amount = explodedTnt.getPersistentDataContainer().get(recursionAmountKey, PersistentDataType.INTEGER);
        float yield = explodedTnt.getPersistentDataContainer().get(recursionYieldKey, PersistentDataType.FLOAT);

        double velocityMult = 1.0;
        if (explodedTnt.getPersistentDataContainer().has(recursionVelocityKey, PersistentDataType.DOUBLE)) {
            velocityMult = explodedTnt.getPersistentDataContainer().get(recursionVelocityKey, PersistentDataType.DOUBLE);
        }

        int splitFuseTicks = 60;
        if (explodedTnt.getPersistentDataContainer().has(recursionSplitFuseKey, PersistentDataType.INTEGER)) {
            splitFuseTicks = explodedTnt.getPersistentDataContainer().get(recursionSplitFuseKey, PersistentDataType.INTEGER);
        }

        int lastFuseTicks = 60;
        if (explodedTnt.getPersistentDataContainer().has(recursionLastFuseKey, PersistentDataType.INTEGER)) {
            lastFuseTicks = explodedTnt.getPersistentDataContainer().get(recursionLastFuseKey, PersistentDataType.INTEGER);
        }

        Location center = explodedTnt.getLocation();
        World world = center.getWorld();

        double angleStep = 360.0 / amount;
        int fuseToUse = (level - 1 == 0) ? lastFuseTicks : splitFuseTicks;

        for (int i = 0; i < amount; i++) {
            double angle = i * angleStep;
            double radians = Math.toRadians(angle);
            double x = Math.cos(radians);
            double z = Math.sin(radians);
            Vector velocity = new Vector(x, 0.5, z).normalize().multiply(velocityMult);

            TNTPrimed newTnt = spawnTNTAt(world, center.clone().add(0, 0.5, 0), yield, fuseToUse, false);
            if (newTnt != null) {
                newTnt.setVelocity(velocity);
                newTnt.getPersistentDataContainer().set(recursionLevelKey, PersistentDataType.INTEGER, level - 1);
                newTnt.getPersistentDataContainer().set(recursionAmountKey, PersistentDataType.INTEGER, amount);
                newTnt.getPersistentDataContainer().set(recursionYieldKey, PersistentDataType.FLOAT, yield);
                newTnt.getPersistentDataContainer().set(recursionVelocityKey, PersistentDataType.DOUBLE, velocityMult);
                newTnt.getPersistentDataContainer().set(recursionSplitFuseKey, PersistentDataType.INTEGER, splitFuseTicks);
                newTnt.getPersistentDataContainer().set(recursionLastFuseKey, PersistentDataType.INTEGER, lastFuseTicks);
            }
        }
    }

    private double getDoubleParameter(Cannon cannon, String key, double defaultValue) {
        Object val = cannon.getParameter(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return defaultValue;
    }

    private float getFloatParameter(Cannon cannon, String key, float defaultValue) {
        Object val = cannon.getParameter(key);
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        return defaultValue;
    }

    private int getIntParameter(Cannon cannon, String key, int defaultValue) {
        Object val = cannon.getParameter(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return defaultValue;
    }

    private static class TntLaunchData {
        final TNTPrimed tnt;
        final double targetX;
        final double targetZ;

        TntLaunchData(TNTPrimed tnt, double targetX, double targetZ) {
            this.tnt = tnt;
            this.targetX = targetX;
            this.targetZ = targetZ;
        }
    }

    private TNTPrimed spawnTNTAt(World world, Location loc, float yield, int fuse, boolean invulnerable) {
        try {
            if (fuse == 0 && loc.getBlock().isLiquid()) return null;

            TNTPrimed tnt = (TNTPrimed) world.spawnEntity(loc, EntityType.PRIMED_TNT);
            tnt.setFuseTicks(fuse);
            tnt.setYield(yield);
            tnt.setIsIncendiary(false);
            if (invulnerable) {
                tnt.setInvulnerable(true);
            }

            tnt.getPersistentDataContainer().set(orbitalStrikeKey, PersistentDataType.BYTE, (byte) 1);

            return tnt;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to spawn TNT", e);
            return null;
        }
    }

    private Location findGroundLevel(World world, Location start) {
        Location ground = start.clone();
        if (ground.getY() > world.getMaxHeight()) ground.setY(world.getMaxHeight() - 1);

        while (ground.getY() > world.getMinHeight() && ground.getBlock().getType().isAir()) {
            ground.subtract(0, 1, 0);
        }
        return ground.add(0, 1, 0);
    }

    public void clearAll() {
        activeStrikes.clear();
    }
}