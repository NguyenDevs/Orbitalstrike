package com.NguyenDevs.orbitalstrike.managers;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.payloads.*;
import com.NguyenDevs.orbitalstrike.models.PayloadType;
import com.NguyenDevs.orbitalstrike.models.StrikeData;
import com.NguyenDevs.orbitalstrike.models.Cannon;
import com.NguyenDevs.orbitalstrike.models.ActiveStrike;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;

public class PayloadManager {
    private final OrbitalStrike plugin;
    private final List<ActiveStrike> activeStrikes;
    private final Map<PayloadType, IPayload> payloads;

    private final NamespacedKey orbitalStrikeKey;
    private final NamespacedKey sourceCannonKey;
    private final NamespacedKey recursionLevelKey;
    private final NamespacedKey recursionAmountKey;
    private final NamespacedKey recursionYieldKey;
    private final NamespacedKey recursionVelocityKey;
    private final NamespacedKey recursionSplitFuseKey;
    private final NamespacedKey recursionLastFuseKey;
    private final NamespacedKey empTntKey;
    private final NamespacedKey empRadiusKey;
    private final NamespacedKey empPulsesKey;
    private final NamespacedKey empPulseDelayKey;
    private final NamespacedKey empPulseSpeedKey;
    private final NamespacedKey empEffectsKey;
    private final NamespacedKey empDestroyedBlocksKey;
    private final NamespacedKey empDropItemsKey;

    public PayloadManager(OrbitalStrike plugin) {
        this.plugin = plugin;
        this.activeStrikes = new ArrayList<>();
        this.payloads = new EnumMap<>(PayloadType.class);

        this.orbitalStrikeKey = new NamespacedKey(plugin, "orbital_strike_tnt");
        this.sourceCannonKey = new NamespacedKey(plugin, "source_cannon");
        this.recursionLevelKey = new NamespacedKey(plugin, "recursion_level");
        this.recursionAmountKey = new NamespacedKey(plugin, "recursion_amount");
        this.recursionYieldKey = new NamespacedKey(plugin, "recursion_yield");
        this.recursionVelocityKey = new NamespacedKey(plugin, "recursion_velocity");
        this.recursionSplitFuseKey = new NamespacedKey(plugin, "recursion_split_fuse");
        this.recursionLastFuseKey = new NamespacedKey(plugin, "recursion_last_fuse");
        this.empTntKey = new NamespacedKey(plugin, "emp_tnt");
        this.empRadiusKey = new NamespacedKey(plugin, "emp_radius");
        this.empPulsesKey = new NamespacedKey(plugin, "emp_pulses");
        this.empPulseDelayKey = new NamespacedKey(plugin, "emp_pulse_delay");
        this.empPulseSpeedKey = new NamespacedKey(plugin, "emp_pulse_speed");
        this.empEffectsKey = new NamespacedKey(plugin, "emp_effects");
        this.empDestroyedBlocksKey = new NamespacedKey(plugin, "emp_destroyed_blocks");
        this.empDropItemsKey = new NamespacedKey(plugin, "emp_drop_items");

        registerPayloads();
    }

    private void registerPayloads() {
        payloads.put(PayloadType.STAB, new StabPayload(plugin));
        payloads.put(PayloadType.NUKE, new NukePayload(plugin));
        payloads.put(PayloadType.RECURSION, new RecursionPayload(plugin));
        payloads.put(PayloadType.EMP, new EmpPayload(plugin));
    }

    public NamespacedKey getOrbitalStrikeKey() { return orbitalStrikeKey; }
    public NamespacedKey getSourceCannonKey() { return sourceCannonKey; }
    public NamespacedKey getRecursionLevelKey() { return recursionLevelKey; }
    public NamespacedKey getRecursionAmountKey() { return recursionAmountKey; }
    public NamespacedKey getRecursionYieldKey() { return recursionYieldKey; }
    public NamespacedKey getRecursionVelocityKey() { return recursionVelocityKey; }
    public NamespacedKey getRecursionSplitFuseKey() { return recursionSplitFuseKey; }
    public NamespacedKey getRecursionLastFuseKey() { return recursionLastFuseKey; }
    public NamespacedKey getEmpTntKey() { return empTntKey; }
    public NamespacedKey getEmpRadiusKey() { return empRadiusKey; }
    public NamespacedKey getEmpPulsesKey() { return empPulsesKey; }
    public NamespacedKey getEmpPulseDelayKey() { return empPulseDelayKey; }
    public NamespacedKey getEmpPulseSpeedKey() { return empPulseSpeedKey; }
    public NamespacedKey getEmpEffectsKey() { return empEffectsKey; }
    public NamespacedKey getEmpDestroyedBlocksKey() { return empDestroyedBlocksKey; }
    public NamespacedKey getEmpDropItemsKey() { return empDropItemsKey; }

    public void initiateStrike(Cannon cannon, StrikeData data, Location target) {
        if (plugin.getConfigManager().getDisabledWorlds().contains(target.getWorld().getName())) {
            return;
        }

        if (plugin.getConfigManager().isLogsEnabled()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bOrbital&3Strike&9Cannon&8]")
                    + " " + plugin.getMessageManager().getMessage("log", "%target%", target.toString()));
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
        
        String sourceCannon = null;
        if (explodedTnt.getPersistentDataContainer().has(sourceCannonKey, PersistentDataType.STRING)) {
            sourceCannon = explodedTnt.getPersistentDataContainer().get(sourceCannonKey, PersistentDataType.STRING);
        }

        for (int i = 0; i < amount; i++) {
            double angle = i * angleStep;
            double radians = Math.toRadians(angle);
            Vector velocity = new Vector(Math.cos(radians), 0.5, Math.sin(radians)).normalize().multiply(velocityMult);

            TNTPrimed newTnt = PayloadUtils.spawnTNTAt(plugin, world, center.clone().add(0, 0.5, 0), yield, fuseToUse, false, orbitalStrikeKey, sourceCannon);
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

    public void triggerEmpShockwave(Location center, double maxRadius, int pulses, int pulseDelay, double pulseSpeed, 
                                   List<String> effects, List<String> destroyedBlocks, boolean dropItems) {
        World world = center.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            int pulsesLaunched = 0;
            int ticksSinceLastPulse = pulseDelay;

            @Override
            public void run() {
                if (pulsesLaunched >= pulses) {
                    this.cancel();
                    return;
                }
                if (ticksSinceLastPulse >= pulseDelay) {
                    launchSingleWave(center, maxRadius, pulseSpeed, effects, destroyedBlocks, dropItems);
                    pulsesLaunched++;
                    ticksSinceLastPulse = 0;
                }
                ticksSinceLastPulse++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void launchSingleWave(Location center, double maxRadius, double step, 
                                  List<String> effects, List<String> destroyedBlocks, boolean dropItems) {
        World world = center.getWorld();
        if (world == null) return;

        int gatherTicks = 15;
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= gatherTicks) {
                    this.cancel();

                    world.spawnParticle(Particle.EXPLOSION, center, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, center, 60, 0.3, 0.3, 0.3, 0.3);
                    world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.6f);
                    world.playSound(center, Sound.ITEM_TRIDENT_RIPTIDE_1, 1.2f, 1.5f);
                    world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.5f);

                    new BukkitRunnable() {
                        double currentRadius = 0;

                        @Override
                        public void run() {
                            currentRadius += step;
                            if (currentRadius > maxRadius) {
                                this.cancel();
                                return;
                            }
                            spawnSphereShell(world, center, currentRadius);
                            processWaveEffects(center, currentRadius, step, effects, destroyedBlocks, dropItems);
                        }
                    }.runTaskTimer(plugin, 0L, 1L);

                    return;
                }

                double gatherRadius = maxRadius * 0.35 * (1.0 - (double) tick / gatherTicks);
                int particleCount = 10 + tick * 4;
                for (int i = 0; i < particleCount; i++) {
                    double phi = Math.acos(1 - 2.0 * Math.random());
                    double theta = 2 * Math.PI * Math.random();
                    double r = gatherRadius * (0.6 + Math.random() * 0.4);
                    double px = center.getX() + r * Math.sin(phi) * Math.cos(theta);
                    double py = center.getY() + r * Math.cos(phi);
                    double pz = center.getZ() + r * Math.sin(phi) * Math.sin(theta);
                    world.spawnParticle(Particle.DUST, px, py, pz, 1, 0, 0, 0, 0, GATHER_DUST);
                }

                if (tick % 5 == 0) {
                    float pitch = 0.5f + (tick / (float) gatherTicks);
                    world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, pitch);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static final Particle.DustOptions WHITE_DUST = new Particle.DustOptions(Color.fromRGB(255, 255, 255), 1.5f);
    private static final Particle.DustOptions CYAN_DUST = new Particle.DustOptions(Color.fromRGB(100, 220, 255), 1.5f);
    private static final Particle.DustOptions GATHER_DUST = new Particle.DustOptions(Color.fromRGB(100, 220, 255), 1.2f);

    private void spawnSphereShell(World world, Location center, double radius) {
        if (radius <= 0) return;

        int totalPoints = (int) Math.min(500, Math.max(40, Math.PI * radius * radius * 2.5));

        double goldenRatio = (1.0 + Math.sqrt(5)) / 2.0;
        for (int i = 0; i < totalPoints; i++) {
            double theta = 2 * Math.PI * i / goldenRatio;
            double phi   = Math.acos(1.0 - 2.0 * (i + 0.5) / totalPoints);

            double px = center.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double py = center.getY() + radius * Math.cos(phi);
            double pz = center.getZ() + radius * Math.sin(phi) * Math.sin(theta);

            world.spawnParticle(Particle.DUST, px, py, pz, 1, 0, 0, 0, 0,
                    (i % 8 == 0) ? CYAN_DUST : WHITE_DUST);
        }
    }

    private List<PotionEffect> parseEffects(List<String> effects) {
        if (effects == null || effects.isEmpty()) return Collections.emptyList();
        List<PotionEffect> parsed = new ArrayList<>();
        for (String effectStr : effects) {
            String[] parts = effectStr.split(":");
            if (parts.length >= 3) {
                try {
                    PotionEffectType type = PotionEffectType.getByName(parts[0]);
                    if (type != null) {
                        int amp = Integer.parseInt(parts[1]);
                        int dur = Integer.parseInt(parts[2]);
                        parsed.add(new PotionEffect(type, dur, amp));
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Invalid potion effect config: " + effectStr, e);
                }
            }
        }
        return parsed;
    }

    private void processWaveEffects(Location center, double currentRadius, double step, 
                                    List<String> effects, List<String> destroyedBlocks, boolean dropItems) {
        World world = center.getWorld();
        if (world == null) return;

        double innerBound = currentRadius - step;
        double outerBound = currentRadius;
        int scanRadius = (int) Math.ceil(currentRadius) + 1;

        int rSq = scanRadius * scanRadius;
        List<PotionEffect> parsedEffects = parseEffects(effects);

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    double distSq = x * x + y * y + z * z;
                    if (distSq < innerBound * innerBound || distSq > outerBound * outerBound) continue;

                    Block block = center.getBlock().getRelative(x, y, z);
                    Material type = block.getType();
                    String name = type.name();

                    if (destroyedBlocks.contains(name)) {
                        if (dropItems) {
                            block.breakNaturally();
                        } else {
                            block.setType(Material.AIR);
                        }
                    }

                    if (name.contains("COPPER_BULB")) {
                        BlockData data = block.getBlockData();
                        if (data instanceof Lightable lightable && lightable.isLit()) {
                            lightable.setLit(false);
                            block.setBlockData(lightable);
                        }
                    }

                    if (name.contains("GLASS")) {
                        block.setType(Material.AIR);
                        world.playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.2f);
                        world.spawnParticle(Particle.ELECTRIC_SPARK, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
                    }
                }
            }
        }

        world.getNearbyEntities(center, currentRadius + 0.5, currentRadius + 0.5, currentRadius + 0.5).forEach(entity -> {
            double dist = entity.getLocation().distance(center);
            if (dist < innerBound || dist > outerBound) return;

            if (entity instanceof TNTPrimed) {
                Location loc = entity.getLocation();
                loc.getBlock().setType(Material.TNT);
                entity.remove();
                return;
            }

            if (entity instanceof LivingEntity living && !parsedEffects.isEmpty()) {
                for (PotionEffect effect : parsedEffects) {
                    living.addPotionEffect(effect);
                }
            }

            Vector direction = entity.getLocation().toVector().subtract(center.toVector()).normalize();
            direction.setY(0.1);

            double multiplier = 0.15;
            if (entity instanceof org.bukkit.entity.Item || entity instanceof org.bukkit.entity.ExperienceOrb) {
                multiplier = 0.02;
            } else if (!(entity instanceof LivingEntity)) {
                multiplier = 0.08;
            }

            entity.setVelocity(direction.multiply(multiplier));
        });
    }

    public void clearAll() {
        activeStrikes.clear();
    }
}