package com.NguyenDevs.orbitalstrike.configuration;

import org.bukkit.Material;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecipeConfig {
    private final String key;
    private final boolean enabled;
    private final String cannonName;
    private final List<String> shape;
    private final Map<Character, Material> ingredients;
    private final boolean requirePermission;
    private final String permission;

    public RecipeConfig(String key, boolean enabled, String cannonName, List<String> shape, Map<Character, Material> ingredients, boolean requirePermission, String permission) {
        this.key = key;
        this.enabled = enabled;
        this.cannonName = cannonName;
        this.shape = shape;
        this.ingredients = ingredients;
        this.requirePermission = requirePermission;
        this.permission = permission;
    }

    public String getKey() {
        return key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCannonName() {
        return cannonName;
    }

    public List<String> getShape() {
        return shape;
    }

    public Map<Character, Material> getIngredients() {
        return ingredients;
    }

    public boolean isRequirePermission() {
        return requirePermission;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeConfig that = (RecipeConfig) o;
        return enabled == that.enabled &&
                requirePermission == that.requirePermission &&
                Objects.equals(key, that.key) &&
                Objects.equals(cannonName, that.cannonName) &&
                Objects.equals(shape, that.shape) &&
                Objects.equals(ingredients, that.ingredients) &&
                Objects.equals(permission, that.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, enabled, cannonName, shape, ingredients, requirePermission, permission);
    }
}
