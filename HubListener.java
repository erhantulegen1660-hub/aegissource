package com.aegis.hub;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.legendary.RecipeBookGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class HubListener implements Listener {

    private final AegisPlugin plugin;
    private final HubService hubService;

    public HubListener(AegisPlugin plugin, HubService hubService) {
        this.plugin = plugin;
        this.hubService = hubService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        PlayerProfile prof = plugin.getDatabaseManager().loadProfile(p.getUniqueId(), p.getName());
        plugin.getStatusManager().getOpGuard().syncOp(p, prof.getRole());
        hubService.sendToHub(p);
        hubService.checkOfflineRewards(p);
    }

    @EventHandler
    public void onPhoneUse(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!hubService.isInHub(p)) return;
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        if (item.getItemMeta().getPersistentDataContainer().has(HubService.PHONE_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            new HubPhoneGui(plugin, p).open(p);
        }
    }

    @EventHandler
    public void onPhoneGuiClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof HubPhoneGui)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player p)) return;
        switch (event.getRawSlot()) {
            case 10 -> plugin.getMatchManager().openLobbySelector(p);
            case 12 -> plugin.getClassService().openClassMenu(p);
            case 13 -> new RecipeBookGui(plugin).openList(p, 0);
            case 14 -> plugin.getDonateService().openMainMenu(p);
            case 16 -> plugin.getCosmeticManager().openWardrobe(p);
        }
    }

    @EventHandler
    public void onHubDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player p && hubService.isInHub(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHubHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player p && hubService.isInHub(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHubBreak(BlockBreakEvent event) {
        if (hubService.isInHub(event.getPlayer()) && !event.getPlayer().hasPermission("aegis.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHubPlace(BlockPlaceEvent event) {
        if (hubService.isInHub(event.getPlayer()) && !event.getPlayer().hasPermission("aegis.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHubDrop(PlayerDropItemEvent event) {
        if (hubService.isInHub(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
