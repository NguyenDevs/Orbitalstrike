package com.NguyenDevs.orbitalstrike.cannon.payload;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.Cannon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SingularityPayload implements IPayload {
    private final OrbitalStrike plugin;

    public SingularityPayload(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(World world, Location target, Cannon cannon) {
        int duration = PayloadUtils.getIntParameter(cannon, "duration", plugin.getConfigManager().getSingularityDuration());
        double pullForce = PayloadUtils.getDoubleParameter(cannon, "pull-force", plugin.getConfigManager().getSingularityPullForce());
        double radius = PayloadUtils.getDoubleParameter(cannon, "radius", plugin.getConfigManager().getSingularityRadius());
        float yield = (float) PayloadUtils.getDoubleParameter(cannon, "yield", plugin.getConfigManager().getSingularityYield());

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration) {
                    this.cancel();
                    PayloadUtils.spawnTNTAt(plugin, world, target, yield, 0, false, plugin.getPayloadManager().getOrbitalStrikeKey());
                    return;
                }

                world.spawnParticle(Particle.PORTAL, target, 20, 1, 1, 1, 0.1);
                world.spawnParticle(Particle.REVERSE_PORTAL, target, 10, 0.5, 0.5, 0.5, 0.05);

                world.getNearbyEntities(target, radius, radius, radius).forEach(entity -> {
                    Vector direction = target.toVector().subtract(entity.getLocation().toVector());
                    if (direction.length() > 0.5) {
                        entity.setVelocity(direction.normalize().multiply(pullForce));
                    }
                });
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
