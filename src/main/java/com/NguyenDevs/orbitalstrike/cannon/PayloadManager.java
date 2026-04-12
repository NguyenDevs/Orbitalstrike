package com.NguyenDevs.orbitalstrike.cannon;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.payload.*;
import com.NguyenDevs.orbitalstrike.utils.PayloadType;
import com.NguyenDevs.orbitalstrike.utils.StrikeData;
import org.bukkit.*;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PayloadManager {
    private final OrbitalStrike plugin;
    private final List<ActiveStrike> activeStrikes;
    private final Map<PayloadType, IPayload> payloads;
    
    private final NamespacedKey orbitalStrikeKey;
    private final NamespacedKey recursionLevelKey;
    private final NamespacedKey recursionAmountKey;
    private final NamespacedKey recursionYieldKey;
    private final NamespacedKey recursionVelocityKey;
    private final NamespacedKey recursionSplitFuseKey;
    private final NamespacedKey recursionLastFuseKey;

    public PayloadManager(OrbitalStrike plugin) {
        this.plugin = plugin;
        this.activeStrikes = new ArrayList<>();
        this.payloads = new EnumMap<>(PayloadType.class);
        
        this.orbitalStrikeKey = new NamespacedKey(plugin, "orbital_strike_tnt");
        this.recursionLevelKey = new NamespacedKey(plugin, "recursion_level");
        this.recursionAmountKey = new NamespacedKey(plugin, "recursion_amount");
        this.recursionYieldKey = new NamespacedKey(plugin, "recursion_yield");
        this.recursionVelocityKey = new NamespacedKey(plugin, "recursion_velocity");
        this.recursionSplitFuseKey = new NamespacedKey(plugin, "recursion_split_fuse");
        this.recursionLastFuseKey = new NamespacedKey(plugin, "recursion_last_fuse");

        registerPayloads();
    }

    private void registerPayloads() {
        payloads.put(PayloadType.STAB, new StabPayload(plugin));
        payloads.put(PayloadType.NUKE, new NukePayload(plugin));
        payloads.put(PayloadType.RECURSION, new RecursionPayload(plugin));
        payloads.put(PayloadType.PLASMA, new PlasmaPayload(plugin));
        payloads.put(PayloadType.SINGULARITY, new SingularityPayload(plugin));
        payloads.put(PayloadType.CLUSTER, new ClusterPayload(plugin));
        payloads.put(PayloadType.EMP, new EmpPayload(plugin));
    }

    public NamespacedKey getOrbitalStrikeKey() {
        return orbitalStrikeKey;
    }

    public NamespacedKey getRecursionLevelKey() {
        return recursionLevelKey;
    }

    public NamespacedKey getRecursionAmountKey() {
        return recursionAmountKey;
    }

    public NamespacedKey getRecursionYieldKey() {
        return recursionYieldKey;
    }

    public NamespacedKey getRecursionVelocityKey() {
        return recursionVelocityKey;
    }

    public NamespacedKey getRecursionSplitFuseKey() {
        return recursionSplitFuseKey;
    }

    public NamespacedKey getRecursionLastFuseKey() {
        return recursionLastFuseKey;
    }

    public void initiateStrike(Cannon cannon, StrikeData data, Location target) {
        if (!plugin.getConfigManager().getEnabledWorlds().contains(target.getWorld().getName())) {
            return;
        }

        if (plugin.getConfigManager().isLogsEnabled()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bOrbital&3Strike&9Cannon&8]")
                    + " " + plugin.getMessageManager().getMessage("log",
                    "%target%",  target.toString()
            ));
        }

        ActiveStrike strike = new ActiveStrike(cannon, data, target);
        activeStrikes.add(strike);

        new BukkitRunnable() {
            @Override
            public void run() {
                executeStrike(strike);
                activeStrikes.remove(strike);
            }
        }.runTaskLater(plugin, 20L);
    }

    private void executeStrike(ActiveStrike strike) {
        Location target = strike.getTarget();
        World world = target.getWorld();
        if (world == null) return;

        if (plugin.getConfigManager().isForceLoadTarget() && !target.getChunk().isLoaded()) {
            target.getChunk().load();
        }

        IPayload payload = payloads.get(strike.getData().getPayloadType());
        if (payload != null) {
            payload.execute(world, target, strike.getCannon());
        }
    }

    public void handleRecursionExplosion(TNTPrimed explodedTnt) {
        if (!explodedTnt.getPersistentDataContainer().has(recursionLevelKey, PersistentDataType.INTEGER)) return;

        int level = explodedTnt.getPersistentDataContainer().get(recursionLevelKey, PersistentDataType.INTEGER);
        if (level <= 0) return;

        int amount = explodedTnt.getPersistentDataContainer().get(recursionAmountKey, PersistentDataType.INTEGER);
        float yield = explodedTnt.getPersistentDataContainer().get(recursionYieldKey, PersistentDataType.FLOAT);
        double velocityMult = explodedTnt.getPersistentDataContainer().getOrDefault(recursionVelocityKey, PersistentDataType.DOUBLE, 1.0);
        int splitFuseTicks = explodedTnt.getPersistentDataContainer().getOrDefault(recursionSplitFuseKey, PersistentDataType.INTEGER, 60);
        int lastFuseTicks = explodedTnt.getPersistentDataContainer().getOrDefault(recursionLastFuseKey, PersistentDataType.INTEGER, 60);

        Location center = explodedTnt.getLocation();
        World world = center.getWorld();

        double angleStep = 360.0 / amount;
        int fuseToUse = (level - 1 == 0) ? lastFuseTicks : splitFuseTicks;

        for (int i = 0; i < amount; i++) {
            double angle = i * angleStep;
            double radians = Math.toRadians(angle);
            Vector velocity = new Vector(Math.cos(radians), 0.5, Math.sin(radians)).normalize().multiply(velocityMult);

            TNTPrimed newTnt = PayloadUtils.spawnTNTAt(plugin, world, center.clone().add(0, 0.5, 0), yield, fuseToUse, false, orbitalStrikeKey);
            if (newTnt != null) {
                newTnt.setVelocity(velocity);
                newTnt.getPersistentDataContainer().set(recursionLevelKey, PersistentDataType.INTEGER, level - 1);
                newTnt.getPersistentDataContainer().set(recursionAmountKey, PersistentDataType.INTEGER, amount);
                newTnt.getPersistentDataContainer().set(recursionYieldKey, PersistentDataType.FLOAT, yield);
                newTnt.getPersistentDataContainer().set(recursionVelocityKey, PersistentDataType.DOUBLE, velocityMult);
                newTnt.getPersistentDataContainer().set(recursionSplitFuseKey, PersistentDataType.INTEGER, splitFuseTicks);
                newTnt.getPersistentDataContainer().set(recursionLastFuseKey, PersistentDataType.INTEGER, lastFuseTicks);
            }
        }
    }

    public void clearAll() {
        activeStrikes.clear();
    }
}