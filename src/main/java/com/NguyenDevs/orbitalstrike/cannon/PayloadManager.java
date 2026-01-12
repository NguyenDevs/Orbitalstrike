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

    public PayloadManager(OrbitalStrike plugin) {
        this.plugin = plugin;
        this.activeStrikes = new ArrayList<>();
        this.orbitalStrikeKey = new NamespacedKey(plugin, "orbital_strike_tnt");
    }

    public NamespacedKey getOrbitalStrikeKey() {
        return orbitalStrikeKey;
    }

    public void initiateStrike(Cannon cannon, StrikeData data, Location target) {
        if (!plugin.getConfigManager().getEnabledWorlds().contains(target.getWorld().getName())) {
            return;
        }

        plugin.getLogger().info("Initiating strike at " + target.toString());
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
            spawnStab(world, target);
        } else if (type == PayloadType.NUKE) {
            spawnNuke(world, target);
        }
    }

    private void spawnStab(World world, Location center) {
        Location ground = findGroundLevel(world, center);
        float yield = (float) plugin.getConfigManager().getStabYield();
        double offset = plugin.getConfigManager().getStabOffset();
        int verticalStep = plugin.getConfigManager().getStabVerticalStep();

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

    private void spawnNuke(World world, Location center) {
        int rings = plugin.getConfigManager().getNukeRings();
        double height = plugin.getConfigManager().getNukeHeight();
        float yield = (float) plugin.getConfigManager().getNukeYield();
        int baseTnt = plugin.getConfigManager().getNukeBaseTnt();
        int increase = plugin.getConfigManager().getNukeTntIncrease();
        int initialFuse = plugin.getConfigManager().getNukeFuseTicks();
        int launchDelay = plugin.getConfigManager().getNukeLaunchDelay();

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
