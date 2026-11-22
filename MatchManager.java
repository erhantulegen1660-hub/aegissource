package com.aegis.match;

import com.aegis.AegisPlugin;
import com.aegis.util.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MatchManager implements Listener, InventoryHolder {

    private final AegisPlugin plugin;
    private final Map<String, MatchArena> arenas = new LinkedHashMap<>();

    public MatchManager(AegisPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void initLobbies() {
        List<String> ids = plugin.getConfigManager().main().getStringList("match.lobbies");
        for (String id : ids) {
            arenas.put(id, new MatchArena(plugin, id));
        }
    }

    public MatchArena getArenaByPlayer(Player player) {
        for (MatchArena arena : arenas.values()) {
            if (arena.getParticipants().contains(player.getUniqueId())) return arena;
        }
        return null;
    }

    public boolean isInMatch(Player player) {
        return getArenaByPlayer(player) != null;
    }

    public void leaveMatch(Player player) {
        MatchArena arena = getArenaByPlayer(player);
        if (arena != null) arena.removePlayer(player);
    }

    public void openLobbySelector(Player player) {
        Inventory inv = Bukkit.createInventory(this, 27, Mini.parse("<gradient:#a855f7:#e879f9><bold>⚔ Арены Королевской Битвы</bold></gradient>"));
        ItemStack pane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.displayName(Mini.parse("<dark_purple> </dark_purple>"));
        pane.setItemMeta(pm);
        for (int i = 0; i < 27; i++) inv.setItem(i, pane);

        int slot = 11;
        for (MatchArena arena : arenas.values()) {
            ItemStack item = new ItemStack(arena.getPhase() == MatchPhase.WAITING ? Material.AMETHYST_BLOCK : Material.REDSTONE_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Mini.parse("<#a855f7><bold>Арена " + arena.getId().toUpperCase() + "</bold></#a855f7>"));
            meta.lore(Mini.parseList(List.of(
                    "<gray>Статус:</gray> " + arena.getPhase().getFormatted(),
                    "<gray>Игроков:</gray> <white>" + arena.getParticipants().size() + "/32</white>",
                    "",
                    "<#c084fc>Нажмите, чтобы присоединиться!</#c084fc>"
            )));
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot += 2;
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onLobbyClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MatchManager)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player p)) return;

        int slot = 11;
        for (MatchArena arena : arenas.values()) {
            if (event.getRawSlot() == slot) {
                p.closeInventory();
                arena.addPlayer(p);
                return;
            }
            slot += 2;
        }
    }

    public void shutdown() {
        arenas.values().forEach(MatchArena::shutdown);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return Bukkit.createInventory(this, 27);
    }
}
