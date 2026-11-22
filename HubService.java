package com.aegis.hub;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class HubService {

    private final AegisPlugin plugin;
    public static final NamespacedKey PHONE_KEY = new NamespacedKey("aegis", "hub_phone");

    public HubService(AegisPlugin plugin) {
        this.plugin = plugin;
        ensureHubWorld();
    }

    private void ensureHubWorld() {
        String worldName = plugin.getConfigManager().main().getString("server.hub-world", "world_hub");
        if (Bukkit.getWorld(worldName) == null) {
            WorldCreator wc = new WorldCreator(worldName);
            wc.type(WorldType.FLAT);
            World world = wc.createWorld();
            if (world != null) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                world.setTime(18000L);
            }
        }
    }

    public boolean isInHub(Player player) {
        String hubWorld = plugin.getConfigManager().main().getString("server.hub-world", "world_hub");
        return player.getWorld().getName().equalsIgnoreCase(hubWorld);
    }

    public Location getHubSpawn() {
        FileConfiguration cfg = plugin.getConfigManager().main();
        World w = Bukkit.getWorld(cfg.getString("server.hub-world", "world_hub"));
        if (w == null) w = Bukkit.getWorlds().get(0);
        return new Location(w,
                cfg.getDouble("server.hub-spawn.x", 100.5),
                cfg.getDouble("server.hub-spawn.y", 64.0),
                cfg.getDouble("server.hub-spawn.z", 100.5),
                (float) cfg.getDouble("server.hub-spawn.yaw", 0.0),
                (float) cfg.getDouble("server.hub-spawn.pitch", 0.0));
    }

    public void sendToHub(Player player) {
        if (plugin.getMatchManager().isInMatch(player)) {
            plugin.getMatchManager().leaveMatch(player);
        }
        player.teleport(getHubSpawn());
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        player.getInventory().clear();

        giveHubItems(player);
        plugin.getCosmeticManager().applyCosmeticHelmet(player);
        player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("hub.teleported")));
    }

    public void giveHubItems(Player player) {
        ItemStack phone = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = phone.getItemMeta();
        meta.displayName(Mini.parse("<gradient:#a855f7:#e879f9><bold>📱 Телефон AEGIS</bold></gradient>"));
        meta.lore(Mini.parseList(List.of(
                "<gray>Нажмите ПКМ, чтобы открыть</gray>",
                "<gray>главное меню режимов и профиля.</gray>"
        )));
        meta.getPersistentDataContainer().set(PHONE_KEY, PersistentDataType.BYTE, (byte) 1);
        phone.setItemMeta(meta);

        player.getInventory().setItem(0, phone);
    }

    public void checkOfflineRewards(Player player) {
        int offlineAmethysts = plugin.getDatabaseManager().claimOfflineAmethysts(player.getName());
        if (offlineAmethysts > 0) {
            PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
            profile.addAmethysts(offlineAmethysts);
            plugin.getDatabaseManager().saveProfile(profile);
            player.sendMessage(Mini.parse("<gradient:#a855f7:#e879f9>✦ Вам начислено " + offlineAmethysts + " аметистов за покупку на сайте!</gradient>"));
        }
    }
}
