package com.aegis.cosmetic;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CosmeticManager implements Listener, InventoryHolder {

    private final AegisPlugin plugin;
    private final Map<String, CosmeticItem> items = new LinkedHashMap<>();
    public static final NamespacedKey COSMETIC_KEY = new NamespacedKey("aegis", "cosmetic_id");

    public CosmeticManager(AegisPlugin plugin) {
        this.plugin = plugin;
        registerDefaults();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void registerDefaults() {
        items.put("classic_hat", new CosmeticItem("classic_hat", "<white>Классическая шапка</white>", 20001, "RARE"));
        items.put("winter_hat_1", new CosmeticItem("winter_hat_1", "<#38bdf8>Зимняя шапка I</#38bdf8>", 20002, "RARE"));
        items.put("winter_hat_2", new CosmeticItem("winter_hat_2", "<#38bdf8>Зимняя шапка II</#38bdf8>", 20003, "RARE"));
        items.put("winter_hat_3", new CosmeticItem("winter_hat_3", "<#a855f7>Зимняя шапка III</#a855f7>", 20004, "EPIC"));
        items.put("reindeer_hat", new CosmeticItem("reindeer_hat", "<#e879f9>Рога Оленя</#e879f9>", 20005, "MYTHIC"));
        items.put("winter_glasses_1", new CosmeticItem("winter_glasses_1", "<#38bdf8>Зимние очки I</#38bdf8>", 20006, "RARE"));
        items.put("winter_glasses_2", new CosmeticItem("winter_glasses_2", "<#a855f7>Зимние очки II</#a855f7>", 20007, "EPIC"));
        items.put("winter_glasses_3", new CosmeticItem("winter_glasses_3", "<gold>Легендарные очки</gold>", 20008, "LEGENDARY"));
    }

    public Collection<CosmeticItem> getAllCosmetics() { return items.values(); }
    public CosmeticItem getById(String id) { return items.get(id); }

    public void applyCosmeticHelmet(Player player) {
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        String id = profile.getActiveCosmeticId();
        CosmeticItem cos = items.get(id);
        if (cos == null) return;

        ItemStack hat = new ItemStack(Material.CARVED_PUMPKIN);
        ItemMeta meta = hat.getItemMeta();
        meta.displayName(Mini.parse(cos.displayName()));
        meta.setCustomModelData(cos.customModelData());
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.PROTECTION, 4, true);
        meta.getPersistentDataContainer().set(COSMETIC_KEY, PersistentDataType.STRING, cos.id());
        hat.setItemMeta(meta);

        player.getInventory().setHelmet(hat);
    }

    public void openWardrobe(Player player) {
        Inventory inv = Bukkit.createInventory(this, 27, Mini.parse("<gradient:#a855f7:#38bdf8><bold>❄ Гардероб косметики</bold></gradient>"));
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());

        int slot = 10;
        for (CosmeticItem item : items.values()) {
            boolean unlocked = profile.getUnlockedCosmetics().contains(item.id());
            ItemStack icon = new ItemStack(unlocked ? Material.CARVED_PUMPKIN : Material.BARRIER);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(Mini.parse(item.displayName()));
            if (unlocked) meta.setCustomModelData(item.customModelData());
            meta.lore(Mini.parseList(List.of(
                    "<gray>Редкость:</gray> <#c084fc>" + item.rarity() + "</#c084fc>",
                    unlocked ? "<green>Нажмите, чтобы надеть</green>" : "<red>Выпадает из Зимнего кейса (/case)</red>"
            )));
            icon.setItemMeta(meta);
            inv.setItem(slot++, icon);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onWardrobeClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CosmeticManager)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player p)) return;

        int idx = event.getRawSlot() - 10;
        List<CosmeticItem> list = new ArrayList<>(items.values());
        if (idx >= 0 && idx < list.size()) {
            CosmeticItem chosen = list.get(idx);
            PlayerProfile profile = plugin.getDatabaseManager().getProfile(p.getUniqueId());
            if (!profile.getUnlockedCosmetics().contains(chosen.id())) {
                p.sendMessage(Mini.parse("<red>Эта косметика ещё не открыта!</red>"));
                return;
            }
            profile.setActiveCosmeticId(chosen.id());
            plugin.getDatabaseManager().saveProfile(profile);
            applyCosmeticHelmet(p);
            p.sendMessage(Mini.parse("<#c084fc>Вы надели косметику: " + chosen.displayName() + "</#c084fc>"));
            p.closeInventory();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return Bukkit.createInventory(this, 27);
    }
}
