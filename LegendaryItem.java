package com.aegis.legendary;

import org.bukkit.Material;
import java.util.List;
import java.util.Map;

public class LegendaryItem {
    private final String id;
    private final String displayName;
    private final Material material;
    private final int customModelData;
    private final int cooldownSeconds;
    private final double damage;
    private final double attackSpeed;
    private final List<String> lore;
    private final List<String> shape;
    private final Map<Character, Material> ingredients;

    public LegendaryItem(String id, String displayName, Material material, int customModelData,
                         int cooldownSeconds, double damage, double attackSpeed,
                         List<String> lore, List<String> shape, Map<Character, Material> ingredients) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.customModelData = customModelData;
        this.cooldownSeconds = cooldownSeconds;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.lore = lore;
        this.shape = shape;
        this.ingredients = ingredients;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public int getCustomModelData() { return customModelData; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public double getDamage() { return damage; }
    public double getAttackSpeed() { return attackSpeed; }
    public List<String> getLore() { return lore; }
    public List<String> getShape() { return shape; }
    public Map<Character, Material> getIngredients() { return ingredients; }
}
