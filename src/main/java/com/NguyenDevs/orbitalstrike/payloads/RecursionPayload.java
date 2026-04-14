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
        float yield = PayloadUtils.getFloatParameter(cannon, "yield", (float) plugin.getConfigManager().getRecursionYield());
        double height = PayloadUtils.getDoubleParameter(cannon, "height", plugin.getConfigManager().getRecursionHeight());
        int level = PayloadUtils.getIntParameter(cannon, "level", plugin.getConfigManager().getRecursionLevel());
        int amount = PayloadUtils.getIntParameter(cannon, "amount", plugin.getConfigManager().getRecursionAmount());
        double velocity = PayloadUtils.getDoubleParameter(cannon, "velocity", plugin.getConfigManager().getRecursionVelocity());
        int splitFuseTicks = PayloadUtils.getIntParameter(cannon, "split-fuse-ticks", plugin.getConfigManager().getRecursionSplitFuseTicks());
        int lastFuseTicks = PayloadUtils.getIntParameter(cannon, "last-fuse-ticks", plugin.getConfigManager().getRecursionLastFuseTicks());

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
