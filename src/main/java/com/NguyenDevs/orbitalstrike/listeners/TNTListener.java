package com.NguyenDevs.orbitalstrike.listeners;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.PayloadManager;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;

public class TNTListener implements Listener {
    private final OrbitalStrike plugin;

    public TNTListener(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed)) {
            return;
        }

        TNTPrimed tnt = (TNTPrimed) event.getEntity();

        if (tnt.getPersistentDataContainer().has(
                plugin.getPayloadManager().getOrbitalStrikeKey(),
                PersistentDataType.BYTE)) {

            plugin.getPayloadManager().handleRecursionExplosion(tnt);
            return;
        }

        if (tnt.getPersistentDataContainer().has(
                plugin.getPayloadManager().getEmpTntKey(),
                PersistentDataType.BYTE)) {
            
            event.setYield(0); // Extra safety
            event.blockList().clear(); // No block damage
            
            double radius = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpRadiusKey(), PersistentDataType.DOUBLE, 12.0);
            int pulses = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpPulsesKey(), PersistentDataType.INTEGER, 5);
            int delay = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpPulseDelayKey(), PersistentDataType.INTEGER, 10);
            double speed = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpPulseSpeedKey(), PersistentDataType.DOUBLE, 2.0);
            int blindness = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpBlindnessDurationKey(), PersistentDataType.INTEGER, 60);
            int weakness = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpWeaknessDurationKey(), PersistentDataType.INTEGER, 400);
            int nausea = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpNauseaDurationKey(), PersistentDataType.INTEGER, 100);
            int slowness = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpSlownessDurationKey(), PersistentDataType.INTEGER, 100);
            int slownessAmp = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpSlownessAmplifierKey(), PersistentDataType.INTEGER, 1);

            plugin.getPayloadManager().triggerEmpShockwave(event.getLocation(), radius, pulses, delay, speed, blindness, weakness, nausea, slowness, slownessAmp);

        }

    }
}