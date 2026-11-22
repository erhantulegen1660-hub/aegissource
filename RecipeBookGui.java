package com.aegis.legendary;

import com.aegis.AegisPlugin;
import com.aegis.util.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookGui implements InventoryHolder {

    private final AegisPlugin plugin;
    private final Inventory inventory;
    private final boolean detailView;

    public RecipeBookGui(AegisPlugin plugin) {
        this(plugin, false);
    }

    private RecipeBookGui(AegisPlugin plugin, boolean detailView) {
        this.plugin = plugin;
        this.detailView = detailView;
        this.inventory = Bukkit.createInventory(this, 36, Mini.parse("<gradient:#a855f7:#e879f9><bold>✦ Книга Легендарок</bold></gradient>"));
    }

    public void openList(Player player, int page) {
        ItemStack border = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.displayName(Mini.parse("<dark_purple> </dark_purple>"));
        border.setItemMeta(bm);
        for (int i = 0; i < 36; i++) inventory.setItem(i, border);

        List<LegendaryItem> all = new ArrayList<>(plugin.getLegendaryRecipeRegistry().getAll());
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 20, 21, 22};
        for (int i = 0; i < Math.min(all.size(), slots.length); i++) {
            ItemStack icon = plugin.getLegendaryRecipeRegistry().createItemStack(all.get(i));
            ItemMeta meta = icon.getItemMeta();
            List<net.kyori.adventure.text.Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Mini.parse(""));
            lore.add(Mini.parse("<#c084fc>▶ Нажмите, чтобы открыть рецепт 3×3</#c084fc>"));
            meta.lore(lore);
            icon.setItemMeta(meta);
            inventory.setItem(slots[i], icon);
        }
        player.openInventory(inventory);
    }

    public void openRecipeDetail(Player player, LegendaryItem item) {
        RecipeBookGui detail = new RecipeBookGui(plugin, true);
        ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgm = bg.getItemMeta();
        bgm.displayName(Mini.parse("<gray> </gray>"));
        bg.setItemMeta(bgm);
        for (int i = 0; i < 36; i++) detail.inventory.setItem(i, bg);

        int[] gridSlots = {2, 3, 4, 11, 12, 13, 20, 21, 22};
        List<String> shape = item.getShape();
        for (int row = 0; row < 3; row++) {
            String line = row < shape.size() ? shape.get(row) : "   ";
            for (int col = 0; col < 3; col++) {
                char c = col < line.length() ? line.charAt(col) : ' ';
                Material mat = item.getIngredients().get(c);
                detail.inventory.setItem(gridSlots[row * 3 + col], mat != null ? new ItemStack(mat) : null);
            }
        }

        detail.inventory.setItem(15, plugin.getLegendaryRecipeRegistry().createItemStack(item));

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Mini.parse("<#a855f7>← Назад к списку легендарок</#a855f7>"));
        back.setItemMeta(backMeta);
        detail.inventory.setItem(31, back);

        player.openInventory(detail.inventory);
    }

    public boolean isDetailView() { return detailView; }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }
}
