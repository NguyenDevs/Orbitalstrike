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

           // plugin.getLogger().info("Orbital strike TNT exploded normally");
            return;
        }
    }
}