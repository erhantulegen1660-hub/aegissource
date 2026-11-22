package com.aegis.classes;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClassService implements Listener, InventoryHolder {

    private final AegisPlugin plugin;
    public static final NamespacedKey SMELTER_PICK_KEY = new NamespacedKey("aegis", "smelter_pickaxe");

    public ClassService(AegisPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openClassMenu(Player player) {
        Inventory inv = Bukkit.createInventory(this, 27, Mini.parse("<gradient:#a855f7:#7c3aed><bold>🛡 Классы Королевской Битвы</bold></gradient>"));
        ItemStack pane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.displayName(Mini.parse("<dark_purple> </dark_purple>"));
        pane.setItemMeta(pm);
        for (int i = 0; i < 27; i++) inv.setItem(i, pane);

        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        int[] slots = {10, 11, 12, 14, 15, 16};
        GameClass[] classes = GameClass.values();

        for (int i = 0; i < classes.length; i++) {
            GameClass gc = classes[i];
            int lvl = profile.getClassLevel(gc);
            boolean selected = profile.getSelectedClass() == gc;

            ItemStack item = new ItemStack(gc.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Mini.parse((selected ? "<green>✔ " : "<#a855f7>") + "<bold>" + gc.getDisplayName() + "</bold> <gray>[Ур. " + lvl + "/5]</gray>"));
            List<String> lore = new ArrayList<>();
            for (String line : gc.getDescription()) lore.add("<gray>" + line + "</gray>");
            lore.add("");
            lore.add(selected ? "<green>Выбран активным классом</green>" : "<#c084fc>ЛКМ — выбрать класс</#c084fc>");
            if (lvl < 5) {
                int cost = plugin.getConfigManager().getConfig("classes.yml").getInt("upgrade-costs." + (lvl + 1), 150 * lvl);
                lore.add("<#e879f9>ПКМ — улучшить до ур. " + (lvl + 1) + " (" + cost + " аметистов)</#e879f9>");
            } else {
                lore.add("<gold>Максимальный уровень!</gold>");
            }
            meta.lore(Mini.parseList(lore));
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ClassService)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player p)) return;

        int[] slots = {10, 11, 12, 14, 15, 16};
        GameClass[] classes = GameClass.values();
        for (int i = 0; i < slots.length; i++) {
            if (event.getRawSlot() == slots[i]) {
                GameClass chosen = classes[i];
                PlayerProfile profile = plugin.getDatabaseManager().getProfile(p.getUniqueId());
                if (event.isRightClick()) {
                    int lvl = profile.getClassLevel(chosen);
                    if (lvl >= 5) return;
                    int cost = plugin.getConfigManager().getConfig("classes.yml").getInt("upgrade-costs." + (lvl + 1), 150 * lvl);
                    if (profile.getAmethysts() < cost) {
                        p.sendMessage(Mini.parse("<red>Недостаточно аметистов! Нужно: " + cost + "</red>"));
                        return;
                    }
                    profile.addAmethysts(-cost);
                    profile.getClassLevels().put(chosen, lvl + 1);
                    plugin.getDatabaseManager().saveProfile(profile);
                    p.sendMessage(Mini.parse("<#c084fc>Класс " + chosen.getDisplayName() + " улучшен до уровня " + (lvl + 1) + "!</#c084fc>"));
                } else {
                    profile.setSelectedClass(chosen);
                    plugin.getDatabaseManager().saveProfile(profile);
                    p.sendMessage(Mini.parse("<#c084fc>Вы выбрали класс: <white>" + chosen.getDisplayName() + "</white></#c084fc>"));
                }
                openClassMenu(p);
                return;
            }
        }
    }

    public void applyMatchKit(Player player) {
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        GameClass gc = profile.getSelectedClass();
        int lvl = profile.getClassLevel(gc);

        switch (gc) {
            case MINER -> {
                ItemStack pick = new ItemStack(lvl >= 3 ? Material.DIAMOND_PICKAXE : Material.IRON_PICKAXE);
                ItemMeta meta = pick.getItemMeta();
                meta.displayName(Mini.parse("<#a855f7><bold>Кирка Шахтёра</bold></#a855f7>"));
                meta.getPersistentDataContainer().set(SMELTER_PICK_KEY, PersistentDataType.BYTE, (byte) 1);
                pick.setItemMeta(meta);
                player.getInventory().addItem(pick);
            }
            case WARRIOR -> {
                player.getInventory().addItem(new ItemStack(lvl >= 3 ? Material.IRON_SWORD : Material.STONE_SWORD));
                player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            }
            case ARCHER -> {
                player.getInventory().addItem(new ItemStack(Material.BOW));
                player.getInventory().addItem(new ItemStack(Material.ARROW, 16 + lvl * 8));
            }
            case ALCHEMIST -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 180, 0));
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
            }
            case SCOUT -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, lvl >= 4 ? 1 : 0));
            case BLACKSMITH -> {
                player.getInventory().addItem(new ItemStack(Material.ANVIL));
                player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 4 + lvl * 2));
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return Bukkit.createInventory(this, 27);
    }
}
