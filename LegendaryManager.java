package com.aegis.legendary;

import com.aegis.AegisPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LegendaryManager {

    private final AegisPlugin plugin;
    private final LegendaryRecipeRegistry registry;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public LegendaryManager(AegisPlugin plugin, LegendaryRecipeRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public LegendaryItem identify(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        String id = stack.getItemMeta().getPersistentDataContainer()
                .get(LegendaryRecipeRegistry.LEGENDARY_KEY, PersistentDataType.STRING);
        return id != null ? registry.getById(id) : null;
    }

    public long getRemainingCooldown(UUID playerUuid, String itemId) {
        String key = playerUuid + ":" + itemId;
        long end = cooldowns.getOrDefault(key, 0L);
        long left = end - System.currentTimeMillis();
        return left > 0 ? (left / 1000L) + 1 : 0L;
    }

    public void setCooldown(UUID playerUuid, String itemId, int seconds) {
        cooldowns.put(playerUuid + ":" + itemId, System.currentTimeMillis() + (seconds * 1000L));
    }

    public LegendaryRecipeRegistry getRegistry() { return registry; }
}
