package com.NguyenDevs.orbitalstrike.cannon.payload;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.Cannon;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import java.util.Random;

public class ClusterPayload implements IPayload {
    private final OrbitalStrike plugin;

    public ClusterPayload(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(World world, Location target, Cannon cannon) {
        double splitHeight = PayloadUtils.getDoubleParameter(cannon, "split-height", plugin.getConfigManager().getClusterSplitHeight());
        int amount = PayloadUtils.getIntParameter(cannon, "amount", plugin.getConfigManager().getClusterAmount());
        float yield = (float) PayloadUtils.getDoubleParameter(cannon, "yield", plugin.getConfigManager().getClusterYield());
        double scatter = PayloadUtils.getDoubleParameter(cannon, "scatter", plugin.getConfigManager().getClusterScatter());

        Location spawnLoc = target.clone().add(0, splitHeight, 0);
        Random random = new Random();

        for (int i = 0; i < amount; i++) {
            TNTPrimed tnt = PayloadUtils.spawnTNTAt(plugin, world, spawnLoc, yield, 40 + random.nextInt(40), false, plugin.getPayloadManager().getOrbitalStrikeKey());
            if (tnt != null) {
                Vector velocity = new Vector(
                        (random.nextDouble() - 0.5) * scatter,
                        -0.5,
                        (random.nextDouble() - 0.5) * scatter
                );
                tnt.setVelocity(velocity);
            }
        }
    }
}
