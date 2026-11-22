package com.aegis.hub;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import org.bukkit.entity.Player;

public class FlyService {
    private final AegisPlugin plugin;

    public FlyService(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void toggleFly(Player player) {
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        if (!plugin.getStatusManager().isVipOrHigher(profile) && !player.hasPermission("aegis.fly")) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }
        if (!plugin.getHubService().isInHub(player)) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("hub.only-in-hub")));
            return;
        }
        boolean next = !player.getAllowFlight();
        player.setAllowFlight(next);
        player.setFlying(next);
        player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage(next ? "hub.fly-enabled" : "hub.fly-disabled")));
    }
}
