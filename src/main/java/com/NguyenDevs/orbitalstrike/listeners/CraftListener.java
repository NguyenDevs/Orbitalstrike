package com.NguyenDevs.orbitalstrike.listeners;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.cannon.CannonRecipeManager;
import com.NguyenDevs.orbitalstrike.configuration.RecipeConfig;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Level;

public class CraftListener implements Listener {
    private final OrbitalStrike plugin;

    public CraftListener(OrbitalStrike plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getRecipe().getResult();
        
        if (result == null || !result.hasItemMeta()) return;
        
        ItemMeta meta = result.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        if (container.has(CannonRecipeManager.CANNON_KEY, PersistentDataType.STRING)) {
            String cannonName = container.get(CannonRecipeManager.CANNON_KEY, PersistentDataType.STRING);

            for (RecipeConfig config : plugin.getCannonRecipeManager().getCurrentRecipes()) {
                if (config.getCannonName().equals(cannonName)) {
                    if (config.isRequirePermission() && !player.hasPermission(config.getPermission())) {
                        event.setCancelled(true);
                        playErrorSound(player);
                        player.sendMessage(plugin.getMessageManager().getMessage("no-permission-craft"));
                    }
                    break;
                }
            }
        }
    }

    private void playErrorSound(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            try {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error playing error sound for player: " + player.getName(), e);
            }
        }
    }
}
