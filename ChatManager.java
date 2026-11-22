package com.aegis.chat;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatManager {

    private final AegisPlugin plugin;
    private final ChatFilter filter;

    public ChatManager(AegisPlugin plugin) {
        this.plugin = plugin;
        this.filter = new ChatFilter(plugin);
    }

    public ChatFilter getFilter() { return filter; }

    public void processChat(Player sender, String rawMessage) {
        if (!sender.hasPermission("aegis.admin") && filter.isBlocked(rawMessage)) {
            sender.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("chat.filtered")));
            return;
        }

        PlayerProfile profile = plugin.getDatabaseManager().getProfile(sender.getUniqueId());
        Component badge = plugin.getStatusManager().formatBadge(profile);
        Component suffix = plugin.getTitleService().getFormattedSuffix(profile);

        boolean inHub = plugin.getHubService().isInHub(sender);
        if (inHub) {
            Component formatted = Mini.parse(plugin.getConfigManager().getRawMessage("chat.hub-format"),
                    Placeholder.component("badge", badge),
                    Placeholder.unparsed("player", sender.getName()),
                    Placeholder.component("suffix", suffix),
                    Placeholder.unparsed("message", rawMessage));
            for (Player p : sender.getWorld().getPlayers()) {
                p.sendMessage(formatted);
            }
            return;
        }

        boolean isGlobal = rawMessage.startsWith("!");
        String cleanMsg = isGlobal ? rawMessage.substring(1).trim() : rawMessage;
        if (cleanMsg.isEmpty()) return;

        String formatKey = isGlobal ? "chat.global-format" : "chat.local-format";
        Component formatted = Mini.parse(plugin.getConfigManager().getRawMessage(formatKey),
                Placeholder.component("badge", badge),
                Placeholder.unparsed("player", sender.getName()),
                Placeholder.component("suffix", suffix),
                Placeholder.unparsed("message", cleanMsg));

        if (isGlobal) {
            for (Player p : sender.getWorld().getPlayers()) {
                p.sendMessage(formatted);
            }
        } else {
            double radius = plugin.getConfigManager().main().getDouble("chat.local-radius", 100.0);
            double radiusSq = radius * radius;
            for (Player p : sender.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(sender.getLocation()) <= radiusSq) {
                    p.sendMessage(formatted);
                }
            }
        }
    }

    public void handleCheckCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aegis.check")) {
            sender.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(Mini.parse("<red>Использование: /check <ник></red>"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Mini.parse("<red>Игрок не в сети.</red>"));
            return;
        }
        String discord = plugin.getConfigManager().main().getString("server.discord", "https://discord.gg/aegis");
        Component alert = Mini.parse(plugin.getConfigManager().getRawMessage("chat.check-alert"),
                Placeholder.unparsed("moderator", sender.getName()),
                Placeholder.unparsed("discord", discord));

        target.sendMessage(alert);
        sender.sendMessage(Mini.parse("<#c084fc>Уведомление о проверке отправлено игроку <white>" + target.getName() + "</white>.</#c084fc>"));
    }
}
