package com.NguyenDevs.orbitalstrike.payloads;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.models.Cannon;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.persistence.PersistentDataType;

public class RecursionPayload implements IPayload {
    private final OrbitalStrike plugin;

    public RecursionPayload(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(World world, Location target, Cannon cannon) {
        float yield = PayloadUtils.getFloatParameter(cannon, "yield", 10.0f);
        double height = PayloadUtils.getDoubleParameter(cannon, "height", 111.0);
        int level = PayloadUtils.getIntParameter(cannon, "level", 3);
        int amount = PayloadUtils.getIntParameter(cannon, "amount", 5);
        double velocity = PayloadUtils.getDoubleParameter(cannon, "velocity", 0.8);
        int splitFuseTicks = PayloadUtils.getIntParameter(cannon, "split-fuse-ticks", 20);
        int lastFuseTicks = PayloadUtils.getIntParameter(cannon, "last-fuse-ticks", 60);

        Location spawnLocation = target.clone().add(0, height, 0);

        TNTPrimed tnt = PayloadUtils.spawnTNTAt(plugin, world, spawnLocation, yield, 80, false, plugin.getPayloadManager().getOrbitalStrikeKey());
        if (tnt != null) {
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getRecursionLevelKey(), PersistentDataType.INTEGER, level);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getRecursionAmountKey(), PersistentDataType.INTEGER, amount);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getRecursionYieldKey(), PersistentDataType.FLOAT, yield);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getRecursionVelocityKey(), PersistentDataType.DOUBLE, velocity);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getRecursionSplitFuseKey(), PersistentDataType.INTEGER, splitFuseTicks);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getRecursionLastFuseKey(), PersistentDataType.INTEGER, lastFuseTicks);
        }
    }
}
