package com.NguyenDevs.orbitalstrike.listeners;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.models.Cannon;
import com.NguyenDevs.orbitalstrike.managers.CannonRecipeManager;
import com.NguyenDevs.orbitalstrike.models.StrikeData;
import com.NguyenDevs.orbitalstrike.utils.SoundUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;



public class CannonInteractListener implements Listener {
    private final OrbitalStrike plugin;

    public CannonInteractListener(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.REEL_IN && event.getState() != PlayerFishEvent.State.IN_GROUND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.FISHING_ROD) {
            item = player.getInventory().getItemInOffHand();
        }

        if (item.getType() != Material.FISHING_ROD) return;

        Location target = player.getTargetBlock(null, 1000).getLocation();

        handleCannonUse(player, item, target);
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.FISHING_ROD) return;

        Location target = player.getTargetBlock(null, 1000).getLocation();

        if (target == null) return;

        handleCannonUse(player, item, target);
    }

    private void handleCannonUse(Player player, ItemStack item, Location target) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Cannon cannon = null;

        if (meta.getPersistentDataContainer().has(CannonRecipeManager.CANNON_KEY, PersistentDataType.STRING)) {
            String linkedCannonName = meta.getPersistentDataContainer().get(CannonRecipeManager.CANNON_KEY, PersistentDataType.STRING);
            cannon = plugin.getCannonManager().getCannon(linkedCannonName);

            if (cannon == null) {
                player.sendMessage(plugin.getMessageManager().getMessage("cannon.not-found", "%name%", linkedCannonName));
                playErrorSound(player);
                return;
            }
        } else {
            return;
        }
        
        if (!player.hasPermission("orbitalstrike.use." + cannon.getName()) && !player.hasPermission("orbitalstrike.use.*")) {
            player.sendMessage(plugin.getMessageManager().getMessage("cannon.no-permission"));
            playErrorSound(player);
            return;
        }

        if (plugin.getConfigManager().getDisabledWorlds().contains(player.getWorld().getName())) {
            player.sendMessage(plugin.getMessageManager().getMessage("error.world-disabled"));
            playErrorSound(player);
            return;
        }

        if (!plugin.getWorldGuardManager().isAllowed(player, target)) {
            player.sendMessage(plugin.getMessageManager().getMessage("prefix") + plugin.getMessageManager().getMessage("error.worldguard-denied"));
            playErrorSound(player);
            return;
        }

        if (cannon.getCooldown() > 0) {
            long lastUsed = plugin.getCannonManager().getLastUsed(cannon.getName());
            long timeLeft = (lastUsed + cannon.getCooldown() * 1000L) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.sendMessage(plugin.getMessageManager().getMessage("cannon.cooldown", "%time%", String.valueOf(timeLeft / 1000 + 1)));
                playErrorSound(player);
                return;
            }
        }
        
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);

        boolean shouldConsume = true;
        if (cannon.isDurabilityEnabled()) {
            int currentUses = meta.getPersistentDataContainer().getOrDefault(CannonRecipeManager.DURABILITY_KEY, PersistentDataType.INTEGER, 0);
            currentUses++;

            if (currentUses < cannon.getMaxDurability()) {
                meta.getPersistentDataContainer().set(CannonRecipeManager.DURABILITY_KEY, PersistentDataType.INTEGER, currentUses);

                if (meta instanceof Damageable damageable) {
                    int maxVanilla = item.getType().getMaxDurability();
                    if (maxVanilla > 0) {
                        int initialDamage = Math.max(0, maxVanilla - cannon.getMaxDurability());
                        damageable.setDamage(initialDamage + currentUses);
                    }
                }
                item.setItemMeta(meta);
                shouldConsume = false;
            }
        }

        if (shouldConsume) {
            item.setAmount(item.getAmount() - 1);
        }


        StrikeData strikeData = new StrikeData(cannon.getPayloadType());

        plugin.getPayloadManager().initiateStrike(cannon, strikeData, target);
        plugin.getCannonManager().setLastUsed(cannon.getName(), System.currentTimeMillis());
        player.sendMessage(plugin.getMessageManager().getMessage("cannon.fired",
                "%x%", String.valueOf(target.getBlockX()),
                "%y%", String.valueOf(target.getBlockY()),
                "%z%", String.valueOf(target.getBlockZ())
        ));
    }

    private void playErrorSound(Player player) {
        SoundUtils.playErrorSound(player, plugin.getLogger());
    }
}
