package com.NguyenDevs.orbitalstrike.payloads;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.models.Cannon;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class EmpPayload implements IPayload {
    private final OrbitalStrike plugin;

    public EmpPayload(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(World world, Location target, Cannon cannon) {
        double height = PayloadUtils.getDoubleParameter(cannon, "height", 60.0);
        Location spawnLoc = target.clone().add(0, height, 0);

        TNTPrimed tnt = PayloadUtils.spawnTNTAt(plugin, world, spawnLoc, 0, 80, true, plugin.getPayloadManager().getEmpTntKey(), cannon.getName());
        if (tnt != null) {
            double radius = PayloadUtils.getDoubleParameter(cannon, "radius", 15.0);
            int pulses = PayloadUtils.getIntParameter(cannon, "pulses", 3);
            int delay = PayloadUtils.getIntParameter(cannon, "pulse-delay", 60);
            double speed = PayloadUtils.getDoubleParameter(cannon, "pulse-speed", 2.5);
            boolean dropItems = PayloadUtils.getBooleanParameter(cannon, "destroy-drop-items", false);
            
            List<String> effects = PayloadUtils.getStringListParameter(cannon, "effects", Arrays.asList(
                    "BLINDNESS:0:60", "WEAKNESS:1:400", "CONFUSION:4:100", "SLOW:1:100"
            ));
            List<String> destroyedBlocks = PayloadUtils.getStringListParameter(cannon, "destroyed-blocks", Arrays.asList(
                    "REDSTONE", "REDSTONE_BLOCK", "PISTON", "STICKY_PISTON", "REPEATER", 
                    "COMPARATOR", "DROPPER", "DISPENSER", "CRAFTER", "OBSERVER", 
                    "RAIL", "ACTIVATOR_RAIL", "DETECTOR_RAIL", "POWERED_RAIL", 
                    "DAYLIGHT_DETECTOR", "LEVER"
            ));

            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpTntKey(), PersistentDataType.BYTE, (byte) 1);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpRadiusKey(), PersistentDataType.DOUBLE, radius);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpPulsesKey(), PersistentDataType.INTEGER, pulses);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpPulseDelayKey(), PersistentDataType.INTEGER, delay);
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpPulseSpeedKey(), PersistentDataType.DOUBLE, speed);
            
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpEffectsKey(), PersistentDataType.STRING, String.join(",", effects));
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpDestroyedBlocksKey(), PersistentDataType.STRING, String.join(",", destroyedBlocks));
            tnt.getPersistentDataContainer().set(plugin.getPayloadManager().getEmpDropItemsKey(), PersistentDataType.BYTE, dropItems ? (byte) 1 : (byte) 0);
        }
    }
}
