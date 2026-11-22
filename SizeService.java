package com.aegis.hub;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class SizeService {
    private final AegisPlugin plugin;

    public SizeService(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleSizeCommand(Player player, String[] args) {
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        if (!plugin.getStatusManager().isStaffOrVipPlus(profile) && !player.hasPermission("aegis.size")) {
            player.sendMessage(Mini.parse("<red>Команда /size доступна от привилегии VIP+.</red>"));
            return;
        }
        if (!plugin.getHubService().isInHub(player)) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("hub.only-in-hub")));
            return;
        }
        if (args.length < 1) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("hub.size-invalid")));
            return;
        }
        try {
            double scale = Double.parseDouble(args[0]);
            if (scale < 0.5 || scale > 2.0) {
                player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("hub.size-invalid")));
                return;
            }
            AttributeInstance attr = player.getAttribute(Attribute.SCALE);
            if (attr != null) attr.setBaseValue(scale);
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("hub.size-changed"),
                    Placeholder.unparsed("size", String.valueOf(scale))));
        } catch (NumberFormatException e) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("hub.size-invalid")));
        }
    }
}
