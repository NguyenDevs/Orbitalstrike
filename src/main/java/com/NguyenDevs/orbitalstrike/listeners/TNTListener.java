package com.NguyenDevs.orbitalstrike.listeners;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
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
            int duration = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpDurationKey(), PersistentDataType.INTEGER, 200);
            
            plugin.getPayloadManager().triggerEmpShockwave(event.getLocation(), radius, duration);
        }

    }
}