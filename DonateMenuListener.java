package com.aegis.donate;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class DonateMenuListener implements Listener {

    private final AegisPlugin plugin;
    private final DonateService donateService;

    public DonateMenuListener(AegisPlugin plugin, DonateService donateService) {
        this.plugin = plugin;
        this.donateService = donateService;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DonateService)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player p)) return;

        PlayerProfile profile = plugin.getDatabaseManager().getProfile(p.getUniqueId());
        switch (event.getRawSlot()) {
            case 10 -> {
                if (profile.getAmethysts() < 150) {
                    p.sendMessage(Mini.parse("<red>Недостаточно аметистов (нужно 150)!</red>"));
                    return;
                }
                profile.addAmethysts(-150);
                plugin.getStatusManager().setRoleByName(p.getName(), "vip", 30);
                p.sendMessage(Mini.parse("<#c084fc>Вы приобрели привилегию VIP на 30 дней!</#c084fc>"));
                p.closeInventory();
            }
            case 12 -> {
                if (profile.getAmethysts() < 320) {
                    p.sendMessage(Mini.parse("<red>Недостаточно аметистов (нужно 320)!</red>"));
                    return;
                }
                profile.addAmethysts(-320);
                plugin.getStatusManager().setRoleByName(p.getName(), "vip+", 30);
                p.sendMessage(Mini.parse("<#c084fc>Вы приобрели привилегию VIP+ на 30 дней!</#c084fc>"));
                p.closeInventory();
            }
            case 14 -> {
                if (profile.getAmethysts() < 180) {
                    p.sendMessage(Mini.parse("<red>Недостаточно аметистов (нужно 180)!</red>"));
                    return;
                }
                profile.addAmethysts(-180);
                profile.setHasTitleToken(true);
                plugin.getDatabaseManager().saveProfile(profile);
                p.sendMessage(Mini.parse("<#c084fc>Вы приобрели Токен личного титула! Используйте /suffix</#c084fc>"));
                p.closeInventory();
            }
        }
    }
}
