package com.aegis.hub;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HubPhoneGui implements InventoryHolder {

    private final AegisPlugin plugin;
    private final Inventory inventory;

    public HubPhoneGui(AegisPlugin plugin, Player player) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 27, Mini.parse("<gradient:#a855f7:#7c3aed><bold>📱 Телефон AEGIS</bold></gradient>"));
        fill(player);
    }

    private void fill(Player player) {
        ItemStack glass = createIcon(Material.PURPLE_STAINED_GLASS_PANE, "<dark_purple> </dark_purple>", List.of());
        for (int i = 0; i < 27; i++) inventory.setItem(i, glass);

        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());

        inventory.setItem(10, createIcon(Material.NETHERITE_SWORD,
                "<gradient:#a855f7:#e879f9><bold>⚔ Королевская Битва</bold></gradient>",
                List.of("<gray>Выбор арены КБ и поиск игры.</gray>", "<#c084fc>Нажмите, чтобы войти!</#c084fc>")));

        inventory.setItem(12, createIcon(Material.IRON_PICKAXE,
                "<#a855f7><bold>🛡 Классы КБ</bold></#a855f7>",
                List.of("<gray>Текущий класс:</gray> <white>" + profile.getSelectedClass().getDisplayName() + "</white>",
                        "<#c084fc>Нажмите для выбора и прокачки</#c084fc>")));

        inventory.setItem(13, createIcon(Material.ENCHANTED_BOOK,
                "<#e879f9><bold>✦ Книга Легендарок</bold></#e879f9>",
                List.of("<gray>Просмотр всех 10 рецептов</gray>", "<gray>легендарного оружия AEGIS.</gray>")));

        inventory.setItem(14, createIcon(Material.AMETHYST_CLUSTER,
                "<gradient:#a855f7:#e879f9><bold>💎 Магазин (/donate)</bold></gradient>",
                List.of("<gray>Ваш баланс:</gray> <#e879f9>" + profile.getAmethysts() + " аметистов</#e879f9>",
                        "<#c084fc>Привилегии, титулы, кейсы</#c084fc>")));

        inventory.setItem(16, createIcon(Material.CARVED_PUMPKIN,
                "<#38bdf8><bold>❄ Косметика и Кейсы</bold></#38bdf8>",
                List.of("<gray>Коллекция шапок и очков</gray>", "<gray>из Зимнего кейса.</gray>")));
    }

    private ItemStack createIcon(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Mini.parse(name));
        if (!lore.isEmpty()) meta.lore(Mini.parseList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) { player.openInventory(inventory); }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }
}
