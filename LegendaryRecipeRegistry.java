package com.aegis.legendary;

import com.aegis.AegisPlugin;
import com.aegis.util.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class LegendaryRecipeRegistry {

    private final AegisPlugin plugin;
    private final Map<String, LegendaryItem> items = new LinkedHashMap<>();
    public static final NamespacedKey LEGENDARY_KEY = new NamespacedKey("aegis", "legendary_id");

    public LegendaryRecipeRegistry(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadRecipes() {
        items.clear();
        FileConfiguration cfg = plugin.getConfigManager().getConfig("legendaries.yml");
        ConfigurationSection section = cfg.getConfigurationSection("items");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(id);
            if (s == null) continue;

            String name = s.getString("name", id);
            Material mat = Material.valueOf(s.getString("material", "NETHERITE_SWORD"));
            int cmd = s.getInt("custom-model-data", 10001);
            int cd = s.getInt("cooldown", 25);
            double dmg = s.getDouble("damage", 11.0);
            double spd = s.getDouble("attack-speed", -2.2);
            List<String> lore = s.getStringList("lore");
            List<String> shape = s.getStringList("shape");

            Map<Character, Material> ingMap = new HashMap<>();
            ConfigurationSection ingSec = s.getConfigurationSection("ingredients");
            if (ingSec != null) {
                for (String key : ingSec.getKeys(false)) {
                    ingMap.put(key.charAt(0), Material.valueOf(ingSec.getString(key)));
                }
            }

            LegendaryItem item = new LegendaryItem(id, name, mat, cmd, cd, dmg, spd, lore, shape, ingMap);
            items.put(id, item);

            NamespacedKey recipeKey = new NamespacedKey(plugin, "leg_" + id);
            Bukkit.removeRecipe(recipeKey);
            ShapedRecipe recipe = new ShapedRecipe(recipeKey, createItemStack(item));
            recipe.shape(shape.toArray(new String[0]));
            ingMap.forEach(recipe::setIngredient);
            Bukkit.addRecipe(recipe);
        }
    }

    public ItemStack createItemStack(LegendaryItem item) {
        ItemStack stack = new ItemStack(item.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Mini.parse(item.getDisplayName()));
        meta.lore(Mini.parseList(item.getLore()));
        meta.setCustomModelData(item.getCustomModelData());
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(new NamespacedKey(plugin, "dmg_" + item.getId()),
                        item.getDamage() - 1.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                new AttributeModifier(new NamespacedKey(plugin, "spd_" + item.getId()),
                        item.getAttackSpeed(), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        meta.getPersistentDataContainer().set(LEGENDARY_KEY, PersistentDataType.STRING, item.getId());
        stack.setItemMeta(meta);
        return stack;
    }

    public Collection<LegendaryItem> getAll() { return items.values(); }
    public LegendaryItem getById(String id) { return items.get(id); }
}
