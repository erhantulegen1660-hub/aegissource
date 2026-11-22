package com.aegis.legendary;

import com.aegis.AegisPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RecipeBookListener implements Listener {

    private final AegisPlugin plugin;
    private final LegendaryManager legendaryManager;

    public RecipeBookListener(AegisPlugin plugin, LegendaryManager legendaryManager) {
        this.plugin = plugin;
        this.legendaryManager = legendaryManager;
    }

    @EventHandler
    public void onBookClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RecipeBookGui gui)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player p)) return;

        if (gui.isDetailView()) {
            if (event.getRawSlot() == 31) {
                new RecipeBookGui(plugin).openList(p, 0);
            }
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        LegendaryItem item = legendaryManager.identify(clicked);
        if (item != null) {
            gui.openRecipeDetail(p, item);
        }
    }
}
