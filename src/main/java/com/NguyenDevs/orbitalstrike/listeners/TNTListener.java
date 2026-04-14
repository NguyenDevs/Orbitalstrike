package com.NguyenDevs.orbitalstrike.listeners;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.managers.PayloadManager;
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
            
            event.setYield(0);
            event.blockList().clear();
            
            double radius = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpRadiusKey(), PersistentDataType.DOUBLE, 12.0);
            int pulses = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpPulsesKey(), PersistentDataType.INTEGER, 5);
            int delay = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpPulseDelayKey(), PersistentDataType.INTEGER, 10);
            double speed = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpPulseSpeedKey(), PersistentDataType.DOUBLE, 2.0);
            String effectsStr = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpEffectsKey(), PersistentDataType.STRING, "");
            String blocksStr = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpDestroyedBlocksKey(), PersistentDataType.STRING, "");
            byte dropItemsByte = tnt.getPersistentDataContainer().getOrDefault(
                    plugin.getPayloadManager().getEmpDropItemsKey(), PersistentDataType.BYTE, (byte) 0);

            java.util.List<String> effects = effectsStr.isEmpty() ? new java.util.ArrayList<>() : java.util.Arrays.asList(effectsStr.split(","));
            java.util.List<String> destroyedBlocks = blocksStr.isEmpty() ? new java.util.ArrayList<>() : java.util.Arrays.asList(blocksStr.split(","));

            plugin.getPayloadManager().triggerEmpShockwave(event.getLocation(), radius, pulses, delay, speed, effects, destroyedBlocks, dropItemsByte != (byte) 0);

        }

    }
}