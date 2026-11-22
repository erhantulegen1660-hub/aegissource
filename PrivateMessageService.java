package com.aegis.chat;

import com.aegis.AegisPlugin;
import com.aegis.util.Mini;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrivateMessageService {

    private final AegisPlugin plugin;
    private final Map<UUID, UUID> lastReply = new HashMap<>();

    public PrivateMessageService(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleTell(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 2) {
            player.sendMessage(Mini.parse("<red>Использование: /tell <ник> <сообщение></red>"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(Mini.parse("<red>Игрок не найден или не в сети.</red>"));
            return;
        }
        String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        sendPm(player, target, text);
    }

    public void handleReply(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 1) {
            player.sendMessage(Mini.parse("<red>Использование: /reply <сообщение></red>"));
            return;
        }
        UUID targetId = lastReply.get(player.getUniqueId());
        Player target = targetId != null ? Bukkit.getPlayer(targetId) : null;
        if (target == null) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("chat.no-reply")));
            return;
        }
        sendPm(player, target, String.join(" ", args));
    }

    private void sendPm(Player sender, Player target, String text) {
        if (!sender.hasPermission("aegis.admin") && plugin.getChatManager().getFilter().isBlocked(text)) {
            sender.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("chat.filtered")));
            return;
        }
        lastReply.put(sender.getUniqueId(), target.getUniqueId());
        lastReply.put(target.getUniqueId(), sender.getUniqueId());

        sender.sendMessage(Mini.parse(plugin.getConfigManager().getRawMessage("chat.tell-sent"),
                Placeholder.unparsed("target", target.getName()),
                Placeholder.unparsed("message", text)));
        target.sendMessage(Mini.parse(plugin.getConfigManager().getRawMessage("chat.tell-received"),
                Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("message", text)));
    }
}
