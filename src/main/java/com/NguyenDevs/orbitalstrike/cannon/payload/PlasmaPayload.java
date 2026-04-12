package com.NguyenDevs.orbitalstrike.cannon.payload;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.Cannon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class PlasmaPayload implements IPayload {
    private final OrbitalStrike plugin;

    public PlasmaPayload(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(World world, Location target, Cannon cannon) {
        int duration = PayloadUtils.getIntParameter(cannon, "duration", plugin.getConfigManager().getPlasmaDuration());
        double damage = PayloadUtils.getDoubleParameter(cannon, "damage", plugin.getConfigManager().getPlasmaDamage());
        double radius = PayloadUtils.getDoubleParameter(cannon, "radius", plugin.getConfigManager().getPlasmaRadius());
        boolean melting = plugin.getConfigManager().isPlasmaMeltingEnabled();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration) {
                    this.cancel();
                    return;
                }

                for (double y = 0; y < 100; y += 2) {
                    world.spawnParticle(Particle.END_ROD, target.clone().add(0, y, 0), 1, 0.1, 0.1, 0.1, 0.05);
                }

                if (ticks % 5 == 0) {
                    world.getNearbyEntities(target, radius, 100, radius).forEach(entity -> {
                        if (entity instanceof LivingEntity) {
                            ((LivingEntity) entity).damage(damage);
                        }
                    });

                    if (melting) {
                        for (int x = (int) -radius; x <= radius; x++) {
                            for (int z = (int) -radius; z <= radius; z++) {
                                Location loc = target.clone().add(x, 0, z);
                                Material type = loc.getBlock().getType();
                                if (type == Material.STONE || type == Material.COBBLESTONE || type == Material.GRASS_BLOCK || type == Material.DIRT) {
                                    loc.getBlock().setType(Material.LAVA);
                                } else if (type == Material.SAND) {
                                    loc.getBlock().setType(Material.GLASS);
                                }
                            }
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
