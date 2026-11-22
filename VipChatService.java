package com.aegis.chat;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VipChatService {

    private final AegisPlugin plugin;

    public VipChatService(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleVipChat(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        if (!plugin.getStatusManager().isVipOrHigher(profile) && !player.hasPermission("aegis.vipchat")) {
            player.sendMessage(Mini.parse("<red>VIP-чат доступен от привилегии VIP и выше.</red>"));
            return;
        }
        if (args.length == 0) {
            player.sendMessage(Mini.parse("<red>Использование: /vc <сообщение></red>"));
            return;
        }
        String text = String.join(" ", args);
        if (!player.hasPermission("aegis.admin") && plugin.getChatManager().getFilter().isBlocked(text)) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("chat.filtered")));
            return;
        }
        Component badge = plugin.getStatusManager().formatBadge(profile);
        Component msg = Mini.parse(plugin.getConfigManager().getRawMessage("chat.vip-format"),
                Placeholder.component("badge", badge),
                Placeholder.unparsed("player", player.getName()),
                Placeholder.unparsed("message", text));

        for (Player online : Bukkit.getOnlinePlayers()) {
            PlayerProfile op = plugin.getDatabaseManager().getProfile(online.getUniqueId());
            if (plugin.getStatusManager().isVipOrHigher(op) || online.hasPermission("aegis.vipchat")) {
                online.sendMessage(msg);
            }
        }
    }
}
