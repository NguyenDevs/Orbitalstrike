package com.NguyenDevs.orbitalstrike.managers;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.managers.CraftConfigManager;
import com.NguyenDevs.orbitalstrike.models.RecipeConfig;
import com.NguyenDevs.orbitalstrike.models.Cannon;
import com.NguyenDevs.orbitalstrike.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CannonRecipeManager {
    private final OrbitalStrike plugin;
    private final CraftConfigManager craftConfigManager;
    private final List<NamespacedKey> registeredRecipes;
    private List<RecipeConfig> currentRecipes;
    public static NamespacedKey CANNON_KEY;
    public static NamespacedKey DURABILITY_KEY;

    public CannonRecipeManager(OrbitalStrike plugin) {
        this.plugin = plugin;
        this.craftConfigManager = new CraftConfigManager(plugin);
        this.registeredRecipes = new ArrayList<>();
        this.currentRecipes = new ArrayList<>();
        CANNON_KEY = new NamespacedKey(plugin, "linked_cannon");
        DURABILITY_KEY = new NamespacedKey(plugin, "durability_uses");
    }

    public List<RecipeConfig> getCurrentRecipes() {
        return currentRecipes;
    }

    public void registerRecipes() {
        craftConfigManager.loadCraftConfig();
        List<RecipeConfig> newRecipes = craftConfigManager.getRecipes();

        if (newRecipes.equals(currentRecipes)) {
            return;
        }

        for (NamespacedKey key : registeredRecipes) {
            plugin.getServer().removeRecipe(key);
        }
        registeredRecipes.clear();

        currentRecipes = newRecipes;
        for (RecipeConfig config : currentRecipes) {
            if (!config.isEnabled()) continue;

            NamespacedKey key = new NamespacedKey(plugin, "cannon_tool_" + config.getKey());
            
            Cannon cannon = plugin.getCannonManager().getCannon(config.getCannonName());
            if (cannon == null) continue;
            
            ItemStack item = new ItemStack(cannon.getItemMaterial());
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                if (cannon.isDurabilityEnabled()) {
                    meta.getPersistentDataContainer().set(DURABILITY_KEY, PersistentDataType.INTEGER, 0);
                    
                    if (meta instanceof Damageable damageable) {
                        int maxVanilla = item.getType().getMaxDurability();
                        int customMax = cannon.getMaxDurability();
                        
                        if (maxVanilla > 0) {
                            int initialDamage = Math.max(0, maxVanilla - customMax);
                            damageable.setDamage(initialDamage);
                        }
                    }
                }
                
                String displayName = plugin.getMessageManager().getMessage("tool.name") + " (" + config.getCannonName() + ")";
                meta.setDisplayName(ColorUtils.colorize(displayName));
                
                List<String> loreConfig = plugin.getMessageManager().getMessageList("tool.lore");
                List<String> finalLore = new ArrayList<>();
                
                String payloadName = cannon.getPayloadType().name();

                for (String line : loreConfig) {
                    finalLore.add(ColorUtils.colorize(line
                        .replace("%cannon%", config.getCannonName())
                        .replace("%payload%", payloadName)
                    ));
                }
                meta.setLore(finalLore);
                
                meta.getPersistentDataContainer().set(CANNON_KEY, PersistentDataType.STRING, config.getCannonName());
                
                item.setItemMeta(meta);
            }

            ShapedRecipe recipe = new ShapedRecipe(key, item);
            recipe.shape(config.getShape().toArray(new String[0]));

            for (Map.Entry<Character, Material> entry : config.getIngredients().entrySet()) {
                recipe.setIngredient(entry.getKey(), entry.getValue());
            }

            plugin.getServer().addRecipe(recipe);
            registeredRecipes.add(key);
        }
    }
}
