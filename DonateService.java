package com.aegis.donate;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DonateService implements InventoryHolder {

    private final AegisPlugin plugin;

    public DonateService(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(this, 27, Mini.parse("<gradient:#a855f7:#e879f9><bold>💎 Магазин AEGIS</bold></gradient>"));
        ItemStack pane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.displayName(Mini.parse("<dark_purple> </dark_purple>"));
        pane.setItemMeta(pm);
        for (int i = 0; i < 27; i++) inv.setItem(i, pane);

        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());

        inv.setItem(10, icon(Material.AMETHYST_SHARD, "<#a855f7><bold>Привилегия VIP (30 дней)</bold></#a855f7>",
                List.of("<gray>Полёт в хабе, VIP-чат, приоритет входа.</gray>", "<#e879f9>Цена: 150 аметистов</#e879f9>")));

        inv.setItem(12, icon(Material.AMETHYST_CLUSTER, "<#e879f9><bold>Привилегия VIP+ (30 дней)</bold></#e879f9>",
                List.of("<gray>Все права VIP + выбор цвета плашки и /size.</gray>", "<#e879f9>Цена: 320 аметистов</#e879f9>")));

        inv.setItem(14, icon(Material.NAME_TAG, "<gradient:#a855f7:#e879f9><bold>Токен личного титула</bold></gradient>",
                List.of("<gray>Бессрочный доступ к /suffix с градиентами.</gray>", "<#e879f9>Цена: 180 аметистов</#e879f9>")));

        inv.setItem(16, icon(Material.EMERALD, "<green><bold>Ваш баланс: " + profile.getAmethysts() + " ✦</bold></green>",
                List.of("<gray>Пополнение аметистов на сайте:</gray>", "<#c084fc>https://aegis.gamepvp.ru</#c084fc>")));

        player.openInventory(inv);
    }

    private ItemStack icon(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Mini.parse(name));
        meta.lore(Mini.parseList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void processAmethystCommand(String action, String username, int amount) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(username);
        if (!op.hasPlayedBefore() && !op.isOnline()) {
            if ("give".equalsIgnoreCase(action)) {
                plugin.getDatabaseManager().queueOfflineAmethysts(username, amount);
            }
            return;
        }
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(op.getUniqueId());
        switch (action.toLowerCase()) {
            case "give" -> profile.addAmethysts(amount);
            case "take" -> profile.addAmethysts(-amount);
            case "set" -> profile.setAmethysts(amount);
        }
        plugin.getDatabaseManager().saveProfile(profile);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return Bukkit.createInventory(this, 27);
    }
}
