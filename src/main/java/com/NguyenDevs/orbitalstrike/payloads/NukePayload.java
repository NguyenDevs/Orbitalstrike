package com.NguyenDevs.orbitalstrike.payloads;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.models.Cannon;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class NukePayload implements IPayload {
    private final OrbitalStrike plugin;

    public NukePayload(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(World world, Location target, Cannon cannon) {
        int rings = PayloadUtils.getIntParameter(cannon, "rings", 10);
        double height = PayloadUtils.getDoubleParameter(cannon, "height", 60.0);
        float yield = PayloadUtils.getFloatParameter(cannon, "yield", 8.0f);
        int baseTnt = PayloadUtils.getIntParameter(cannon, "base-tnt", 20);
        int increase = PayloadUtils.getIntParameter(cannon, "tnt-increase", 3);
        int initialFuse = PayloadUtils.getIntParameter(cannon, "fuse-ticks", 80);
        int launchDelay = PayloadUtils.getIntParameter(cannon, "launch-delay", 10);

        Location spawnCenter = target.clone().add(0, height, 0);
        if (spawnCenter.getY() > world.getMaxHeight()) {
            spawnCenter.setY(world.getMaxHeight() - 10);
        }

        List<TntLaunchData> launchDataList = new ArrayList<>();

        TNTPrimed centerTnt = PayloadUtils.spawnTNTAt(plugin, world, spawnCenter.clone(), yield, initialFuse, false, plugin.getPayloadManager().getOrbitalStrikeKey(), cannon.getName());
        if (centerTnt != null) {
            centerTnt.setGravity(false);
            centerTnt.setVelocity(new Vector(0, 0, 0));
            launchDataList.add(new TntLaunchData(centerTnt, target.getX(), target.getZ()));
        }

        for (int ring = 1; ring <= rings; ring++) {
            double radius = ring * 4.0;
            int tntCount = baseTnt + ring * increase;
            double angleStep = 360.0 / tntCount;

            for (int i = 0; i < tntCount; i++) {
                double angle = i * angleStep;
                double targetX = target.getX() + radius * Math.cos(Math.toRadians(angle));
                double targetZ = target.getZ() + radius * Math.sin(Math.toRadians(angle));

                TNTPrimed tnt = PayloadUtils.spawnTNTAt(plugin, world, spawnCenter.clone(), yield, initialFuse, false, plugin.getPayloadManager().getOrbitalStrikeKey(), cannon.getName());
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
                            velocity = new Vector(deltaX / distance * speed, -1.2, deltaZ / distance * speed);
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
}
