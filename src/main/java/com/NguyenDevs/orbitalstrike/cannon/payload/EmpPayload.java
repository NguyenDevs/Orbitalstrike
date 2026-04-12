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
            int pulses = PayloadUtils.getIntParameter(cannon, "pulses", plugin.getConfigManager().getEmpPulses());
            int delay = PayloadUtils.getIntParameter(cannon, "pulse-delay", plugin.getConfigManager().getEmpPulseDelay());
            double speed = PayloadUtils.getDoubleParameter(cannon, "pulse-speed", plugin.getConfigManager().getEmpPulseSpeed());
            int blindness = PayloadUtils.getIntParameter(cannon, "blindness-duration", plugin.getConfigManager().getEmpBlindnessDuration());
            int weakness = PayloadUtils.getIntParameter(cannon, "weakness-duration", plugin.getConfigManager().getEmpWeaknessDuration());
            int nausea = PayloadUtils.getIntParameter(cannon, "nausea-duration", plugin.getConfigManager().getEmpNauseaDuration());
            int slowness = PayloadUtils.getIntParameter(cannon, "slowness-duration", plugin.getConfigManager().getEmpSlownessDuration());
            int slownessAmp = PayloadUtils.getIntParameter(cannon, "slowness-amplifier", plugin.getConfigManager().getEmpSlownessAmplifier());

            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpTntKey(), PersistentDataType.BYTE, (byte) 1);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpRadiusKey(), PersistentDataType.DOUBLE, radius);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpPulsesKey(), PersistentDataType.INTEGER, pulses);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpPulseDelayKey(), PersistentDataType.INTEGER, delay);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpPulseSpeedKey(), PersistentDataType.DOUBLE, speed);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpBlindnessDurationKey(), PersistentDataType.INTEGER, blindness);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpWeaknessDurationKey(), PersistentDataType.INTEGER, weakness);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpNauseaDurationKey(), PersistentDataType.INTEGER, nausea);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpSlownessDurationKey(), PersistentDataType.INTEGER, slowness);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpSlownessAmplifierKey(), PersistentDataType.INTEGER, slownessAmp);


        }
    }
}
