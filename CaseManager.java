package com.aegis.casepkg;

import com.aegis.AegisPlugin;
import com.aegis.cosmetic.CosmeticItem;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CaseManager implements Listener, InventoryHolder {

    private final AegisPlugin plugin;

    public CaseManager(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void openCaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(this, 27, Mini.parse("<gradient:#a855f7:#38bdf8><bold>❄ Зимний кейс</bold></gradient>"));
        ItemStack pane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.displayName(Mini.parse("<dark_purple> </dark_purple>"));
        pane.setItemMeta(pm);
        for (int i = 0; i < 27; i++) inv.setItem(i, pane);

        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        boolean free = !profile.isFreeCaseClaimed();

        ItemStack chest = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = chest.getItemMeta();
        meta.displayName(Mini.parse("<gradient:#a855f7:#e879f9><bold>Открыть Зимний кейс</bold></gradient>"));
        meta.lore(Mini.parseList(List.of(
                free ? "<green>Первый кейс — БЕСПЛАТНО!</green>" : "<gray>Стоимость:</gray> <#e879f9>90 аметистов</#e879f9>",
                "<gray>Ваш баланс:</gray> <white>" + profile.getAmethysts() + " аметистов</white>",
                "",
                "<#c084fc>Нажмите, чтобы крутить рулетку!</#c084fc>"
        )));
        chest.setItemMeta(meta);
        inv.setItem(13, chest);

        player.openInventory(inv);
    }

    @EventHandler
    public void onCaseClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CaseManager)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player p)) return;

        if (event.getRawSlot() == 13) {
            PlayerProfile profile = plugin.getDatabaseManager().getProfile(p.getUniqueId());
            if (!profile.isFreeCaseClaimed()) {
                profile.setFreeCaseClaimed(true);
            } else {
                if (profile.getAmethysts() < 90) {
                    p.sendMessage(Mini.parse("<red>Недостаточно аметистов (нужно 90)!</red>"));
                    return;
                }
                profile.addAmethysts(-90);
            }
            plugin.getDatabaseManager().saveProfile(profile);
            spinRoulette(p);
        }
    }

    private void spinRoulette(Player player) {
        Inventory roulette = Bukkit.createInventory(this, 27, Mini.parse("<gradient:#a855f7:#38bdf8><bold>Открытие кейса...</bold></gradient>"));
        List<CosmeticItem> pool = new ArrayList<>(plugin.getCosmeticManager().getAllCosmetics());

        final int[] step = {0};
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline()) {
                task.cancel();
                return;
            }
            for (int slot = 10; slot <= 16; slot++) {
                CosmeticItem rand = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
                ItemStack icon = new ItemStack(Material.CARVED_PUMPKIN);
                ItemMeta meta = icon.getItemMeta();
                meta.displayName(Mini.parse(rand.displayName()));
                meta.setCustomModelData(rand.customModelData());
                icon.setItemMeta(meta);
                roulette.setItem(slot, icon);
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.4f);

            if (step[0] == 0) player.openInventory(roulette);
            step[0]++;

            if (step[0] >= 18) {
                task.cancel();
                CosmeticItem won = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
                PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
                profile.getUnlockedCosmetics().add(won.id());
                profile.setActiveCosmeticId(won.id());
                plugin.getDatabaseManager().saveProfile(profile);
                plugin.getCosmeticManager().applyCosmeticHelmet(player);

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                player.sendMessage(Mini.parse("<gradient:#a855f7:#e879f9>✦ Вы выиграли косметику: " + won.displayName() + "!</gradient>"));
                player.closeInventory();
            }
        }, 1L, 3L);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return Bukkit.createInventory(this, 27);
    }
}
