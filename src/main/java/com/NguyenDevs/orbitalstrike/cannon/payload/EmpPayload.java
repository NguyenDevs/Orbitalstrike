package com.NguyenDevs.orbitalstrike.cannon.payload;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.Cannon;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.persistence.PersistentDataType;

public class EmpPayload implements IPayload {
    private final OrbitalStrike plugin;

    public EmpPayload(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(World world, Location target, Cannon cannon) {
        double height = 60.0;
        Location spawnLoc = target.clone().add(0, height, 0);

        TNTPrimed tnt = PayloadUtils.spawnTNTAt(plugin, world, spawnLoc, 0, 80, true, plugin.getPayloadManager().getEmpTntKey());
        if (tnt != null) {
            double radius = PayloadUtils.getDoubleParameter(cannon, "radius", plugin.getConfigManager().getEmpRadius());
            int duration = PayloadUtils.getIntParameter(cannon, "duration", plugin.getConfigManager().getEmpDuration());
            int pulses = PayloadUtils.getIntParameter(cannon, "pulses", plugin.getConfigManager().getEmpPulses());
            int delay = PayloadUtils.getIntParameter(cannon, "pulse-delay", plugin.getConfigManager().getEmpPulseDelay());
            double speed = PayloadUtils.getDoubleParameter(cannon, "pulse-speed", plugin.getConfigManager().getEmpPulseSpeed());

            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpTntKey(), PersistentDataType.BYTE, (byte) 1);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpRadiusKey(), PersistentDataType.DOUBLE, radius);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpDurationKey(), PersistentDataType.INTEGER, duration);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpPulsesKey(), PersistentDataType.INTEGER, pulses);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpPulseDelayKey(), PersistentDataType.INTEGER, delay);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpPulseSpeedKey(), PersistentDataType.DOUBLE, speed);


        }
    }
}
