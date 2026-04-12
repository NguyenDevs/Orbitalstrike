package com.NguyenDevs.orbitalstrike.cannon.payload;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.Cannon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EmpPayload implements IPayload {
    private final OrbitalStrike plugin;

    public EmpPayload(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(World world, Location target, Cannon cannon) {
        double radius = PayloadUtils.getDoubleParameter(cannon, "radius", plugin.getConfigManager().getEmpRadius());
        int duration = PayloadUtils.getIntParameter(cannon, "duration", plugin.getConfigManager().getEmpDuration());

        world.spawnParticle(Particle.FLASH, target, 5, 2, 2, 2, 0.1);
        world.spawnParticle(Particle.ELECTRIC_SPARK, target, 50, radius / 2, radius / 2, radius / 2, 0.5);

        for (int x = (int) -radius; x <= radius; x++) {
            for (int y = (int) -radius; y <= radius; y++) {
                for (int z = (int) -radius; z <= radius; z++) {
                    Location loc = target.clone().add(x, y, z);
                    if (loc.distance(target) <= radius) {
                        Material type = loc.getBlock().getType();
                        if (type.name().contains("TORCH") || type.name().contains("REDSTONE")) {
                            loc.getBlock().setType(Material.AIR);
                        }
                    }
                }
            }
        }

        world.getNearbyEntities(target, radius, radius, radius).forEach(entity -> {
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 0));
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 2));
            }
        });
    }
}
