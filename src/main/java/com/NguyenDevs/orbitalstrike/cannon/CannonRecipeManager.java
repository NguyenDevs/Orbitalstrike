package com.NguyenDevs.orbitalstrike.cannon;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import com.NguyenDevs.orbitalstrike.configuration.CraftConfigManager;
import com.NguyenDevs.orbitalstrike.configuration.RecipeConfig;
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
    public static final NamespacedKey CANNON_KEY = new NamespacedKey("orbitalstrike", "linked_cannon");
    public static final NamespacedKey DURABILITY_KEY = new NamespacedKey("orbitalstrike", "durability_uses");

    public CannonRecipeManager(OrbitalStrike plugin) {
        this.plugin = plugin;
        this.craftConfigManager = new CraftConfigManager(plugin);
        this.registeredRecipes = new ArrayList<>();
        this.currentRecipes = new ArrayList<>();
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
                // Set initial damage if it's a damageable item and we want it to look used?
                // Actually, if we are using custom durability, we might want it to look full or empty?
                // The previous code set it to maxDurability - 1 (almost broken).
                // But now we have custom durability.
                // If durability is enabled, we start with 0 uses.
                
                if (cannon.isDurabilityEnabled()) {
                    meta.getPersistentDataContainer().set(DURABILITY_KEY, PersistentDataType.INTEGER, 0);
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
