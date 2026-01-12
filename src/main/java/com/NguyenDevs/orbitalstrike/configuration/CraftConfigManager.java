package com.NguyenDevs.orbitalstrike.configuration;

import com.NguyenDevs.orbitalstrike.OrbitalStrike;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftConfigManager {
    private final OrbitalStrike plugin;
    private FileConfiguration craftConfig;
    private File craftFile;

    public CraftConfigManager(OrbitalStrike plugin) {
        this.plugin = plugin;
        this.craftFile = new File(plugin.getDataFolder(), "craft.yml");
        loadCraftConfig();
    }

    public void loadCraftConfig() {
        if (!craftFile.exists()) {
            plugin.saveResource("craft.yml", false);
        }
        craftConfig = YamlConfiguration.loadConfiguration(craftFile);
    }

    public List<RecipeConfig> getRecipes() {
        List<RecipeConfig> recipes = new ArrayList<>();
        ConfigurationSection section = craftConfig.getConfigurationSection("recipes");
        
        if (section == null) return recipes;

        for (String key : section.getKeys(false)) {
            ConfigurationSection recipeSection = section.getConfigurationSection(key);
            if (recipeSection == null) continue;

            boolean enabled = recipeSection.getBoolean("enabled", true);
            String cannonName = recipeSection.getString("cannon");
            List<String> shape = recipeSection.getStringList("shape");
            boolean requirePermission = recipeSection.getBoolean("require-permission", false);
            String permission = recipeSection.getString("permission", "orbitalstrike.craft." + key);
            
            Map<Character, Material> ingredients = new HashMap<>();
            ConfigurationSection ingSection = recipeSection.getConfigurationSection("ingredients");
            if (ingSection != null) {
                for (String charKey : ingSection.getKeys(false)) {
                    if (charKey.length() != 1) continue;
                    String matName = ingSection.getString(charKey);
                    try {
                        Material material = Material.valueOf(matName);
                        ingredients.put(charKey.charAt(0), material);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material in craft.yml for recipe " + key + ": " + matName);
                    }
                }
            }

            if (cannonName != null && !shape.isEmpty() && !ingredients.isEmpty()) {
                recipes.add(new RecipeConfig(key, enabled, cannonName, shape, ingredients, requirePermission, permission));
            }
        }
        return recipes;
    }
}
