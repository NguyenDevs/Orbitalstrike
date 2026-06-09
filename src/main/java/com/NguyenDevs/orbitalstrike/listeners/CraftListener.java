package com.NguyenDevs.orbitalstrike.listeners;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.managers.CannonRecipeManager;
import com.NguyenDevs.orbitalstrike.models.RecipeConfig;
import com.NguyenDevs.orbitalstrike.utils.SoundUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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

    private void playErrorSound(Player player) {
        SoundUtils.playErrorSound(player, plugin.getLogger());
    }
}
