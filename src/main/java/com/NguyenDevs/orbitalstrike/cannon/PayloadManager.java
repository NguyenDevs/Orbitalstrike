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
    private final NamespacedKey empTntKey;
    private final NamespacedKey empRadiusKey;
    private final NamespacedKey empDurationKey;
    private final NamespacedKey empPulsesKey;
    private final NamespacedKey empPulseDelayKey;
    private final NamespacedKey empPulseSpeedKey;




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
        this.empTntKey = new NamespacedKey(plugin, "emp_tnt");
        this.empRadiusKey = new NamespacedKey(plugin, "emp_radius");
        this.empDurationKey = new NamespacedKey(plugin, "emp_duration");
        this.empPulsesKey = new NamespacedKey(plugin, "emp_pulses");
        this.empPulseDelayKey = new NamespacedKey(plugin, "emp_pulse_delay");
        this.empPulseSpeedKey = new NamespacedKey(plugin, "emp_pulse_speed");




        registerPayloads();
    }

    private void registerPayloads() {
        payloads.put(PayloadType.STAB, new StabPayload(plugin));
        payloads.put(PayloadType.NUKE, new NukePayload(plugin));
        payloads.put(PayloadType.RECURSION, new RecursionPayload(plugin));
        payloads.put(PayloadType.PLASMA, new PlasmaPayload(plugin));
        payloads.put(PayloadType.SINGULARITY, new SingularityPayload(plugin));
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

    public NamespacedKey getEmpTntKey() {
        return empTntKey;
    }

    public NamespacedKey getEmpRadiusKey() {
        return empRadiusKey;
    }

    public NamespacedKey getEmpDurationKey() {
        return empDurationKey;
    }

    public NamespacedKey getEmpPulsesKey() {
        return empPulsesKey;
    }

    public NamespacedKey getEmpPulseDelayKey() {
        return empPulseDelayKey;
    }

    public NamespacedKey getEmpPulseSpeedKey() {
        return empPulseSpeedKey;
    }




    public void initiateStrike(Cannon cannon, StrikeData data, Location target) {
        if (plugin.getConfigManager().getDisabledWorlds().contains(target.getWorld().getName())) {
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

    public void triggerEmpShockwave(Location center, double maxRadius, int duration, int totalPulses, int pulseDelay, double step) {
        World world = center.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int currentPulse = 0;
            double currentRadius = 0;
            int waitTicks = 0;

            @Override
            public void run() {
                // Handle waiting between pulses
                if (waitTicks > 0) {
                    waitTicks--;
                    return;
                }

                // If a pulse finishes, reset radius and increment pulse count
                if (currentRadius >= maxRadius) {
                    currentRadius = 0;
                    currentPulse++;
                    
                    if (currentPulse >= totalPulses) {
                        this.cancel();
                        return;
                    }
                    
                    // Trigger wait period
                    if (pulseDelay > 0) {
                        waitTicks = pulseDelay;
                        return;
                    }
                }

                if (currentRadius == 0) {
                    // Start of a pulse sounds
                    world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.5f + (currentPulse * 0.2f));
                    world.playSound(center, Sound.ITEM_TRIDENT_RIPTIDE_1, 1.5f, 1.2f);
                }

                currentRadius += step;


                // Single expanding ring of particles
                for (double angle = 0; angle < 360; angle += 8.0 / currentRadius) {
                    double rad = Math.toRadians(angle);
                    double x = currentRadius * Math.cos(rad);
                    double z = currentRadius * Math.sin(rad);
                    world.spawnParticle(Particle.END_ROD, center.clone().add(x, 0.2, z), 1, 0, 0, 0, 0);
                    // Add secondary particle for "bloom" effect
                    if (currentRadius > 5 && angle % 30 == 0) {
                        world.spawnParticle(Particle.ELECTRIC_SPARK, center.clone().add(x, 0.5, z), 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }

                // Block processing at the current wave front
                int boxRadius = (int) Math.ceil(currentRadius);
                for (int x = -boxRadius; x <= boxRadius; x++) {
                    for (int y = -3; y <= 3; y++) {
                        for (int z = -boxRadius; z <= boxRadius; z++) {
                            Location loc = center.clone().add(x, y, z);
                            double dist = loc.distance(center);
                            
                            if (dist >= currentRadius - step && dist <= currentRadius) {
                                org.bukkit.block.Block block = loc.getBlock();
                                Material type = block.getType();
                                
                                if (type.name().contains("REDSTONE") || type.name().contains("TORCH")) {
                                    block.breakNaturally();
                                }
                                
                                if (type.name().contains("GLASS")) {
                                    block.setType(Material.AIR);
                                    world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
                                }
                            }
                        }
                    }
                }

                // Entity processing
                world.getNearbyEntities(center, currentRadius, 10, currentRadius).forEach(entity -> {
                    double dist = entity.getLocation().distance(center);
                    if (dist >= currentRadius - step && dist <= currentRadius) {
                        if (entity instanceof org.bukkit.entity.LivingEntity) {
                            org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
                            living.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, duration, 0));
                            living.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, duration, 2));
                        }
                        
                        // Stronger knockback per pulse to simulate "waves" pushing
                        Vector direction = entity.getLocation().toVector().subtract(center.toVector()).normalize();
                        direction.setY(0.25);
                        entity.setVelocity(direction.multiply(0.8));
                    }
                });
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }


    public void clearAll() {

        activeStrikes.clear();
    }
}