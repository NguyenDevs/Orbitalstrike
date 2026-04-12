package com.NguyenDevs.orbitalstrike.cannon.payload;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.Cannon;
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

        float yield = PayloadUtils.getFloatParameter(cannon, "yield", (float) plugin.getConfigManager().getStabYield());
        double offset = PayloadUtils.getDoubleParameter(cannon, "offset", plugin.getConfigManager().getStabOffset());
        int verticalStep = PayloadUtils.getIntParameter(cannon, "vertical-step", plugin.getConfigManager().getStabVerticalStep());

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
                PayloadUtils.spawnTNTAt(plugin, world, loc.clone().add(offset, 0, offset), yield, 0, false, plugin.getPayloadManager().getOrbitalStrikeKey());
                PayloadUtils.spawnTNTAt(plugin, world, loc.clone().subtract(offset, 0, offset), yield, 0, false, plugin.getPayloadManager().getOrbitalStrikeKey());
                currentY -= verticalStep;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
