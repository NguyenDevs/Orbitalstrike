package com.NguyenDevs.orbitalstrike.listeners;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.Cannon;
import com.NguyenDevs.orbitalstrike.cannon.CannonRecipeManager;
import com.NguyenDevs.orbitalstrike.utils.ColorUtils;
import com.NguyenDevs.orbitalstrike.utils.StrikeData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Level;

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

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable)) return;
        Damageable damageable = (Damageable) meta;

        if (item.getType().getMaxDurability() - damageable.getDamage() > 1) {
            return;
        }

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

        if (!plugin.getConfigManager().getEnabledWorlds().contains(player.getWorld().getName())) {
            player.sendMessage(plugin.getMessageManager().getMessage("error.world-disabled"));
            playErrorSound(player);
            return;
        }

        Location target = player.getTargetBlock(null, 500).getLocation();

        item.setAmount(0);
        player.playSound(player.getLocation(), "entity.item.break", 1.0f, 1.0f);

        StrikeData strikeData = new StrikeData(cannon.getPayloadType());

        plugin.getPayloadManager().initiateStrike(cannon, strikeData, target);
        player.sendMessage(plugin.getMessageManager().getMessage("cannon.fired",
                "%x%", String.valueOf(target.getBlockX()),
                "%y%", String.valueOf(target.getBlockY()),
                "%z%", String.valueOf(target.getBlockZ())
        ));
    }

    private void playErrorSound(Player player) {
        if (player != null) {
            try {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error playing error sound for player: " + player.getName(), e);
            }
        }
    }
}