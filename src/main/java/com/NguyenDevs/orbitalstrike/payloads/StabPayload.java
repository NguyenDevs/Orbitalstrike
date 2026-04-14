package com.NguyenDevs.orbitalstrike.payloads;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.models.Cannon;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class StabPayload implements IPayload {
    private final OrbitalStrike plugin;

    public StabPayload(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(World world, Location target, Cannon cannon) {
        Location ground = PayloadUtils.findGroundLevel(world, target);

        float yield = PayloadUtils.getFloatParameter(cannon, "yield", 8.0f);
        double offset = PayloadUtils.getDoubleParameter(cannon, "offset", 0.3);
        int verticalStep = PayloadUtils.getIntParameter(cannon, "vertical-step", 2);

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
                PayloadUtils.spawnTNTAt(plugin, world, loc.clone().add(offset, 0, offset), yield, 0, false, plugin.getPayloadManager().getOrbitalStrikeKey(), cannon.getName());
                PayloadUtils.spawnTNTAt(plugin, world, loc.clone().subtract(offset, 0, offset), yield, 0, false, plugin.getPayloadManager().getOrbitalStrikeKey(), cannon.getName());
                currentY -= verticalStep;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
