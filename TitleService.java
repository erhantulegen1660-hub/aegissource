package com.aegis.status;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class TitleService {

    private final AegisPlugin plugin;

    public TitleService(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleSuffixCommand(Player player, String[] args) {
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        if (args.length == 0) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getRawMessage("status.suffix-help")));
            return;
        }
        if (args[0].equalsIgnoreCase("reset")) {
            profile.setCustomSuffix("");
            plugin.getDatabaseManager().saveProfile(profile);
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("status.suffix-reset")));
            return;
        }
        if (args[0].equalsIgnoreCase("preview") && args.length >= 2) {
            String raw = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            if (!validateSuffix(player, raw)) return;
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("status.suffix-preview"),
                    Placeholder.unparsed("player", player.getName()),
                    Placeholder.parsed("suffix", raw)));
            return;
        }
        if (!profile.isHasTitleToken() && !player.hasPermission("aegis.admin")) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("status.suffix-no-token")));
            return;
        }
        String raw = String.join(" ", args);
        if (!validateSuffix(player, raw)) return;

        profile.setCustomSuffix(raw);
        plugin.getDatabaseManager().saveProfile(profile);
        player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("status.suffix-set"),
                Placeholder.parsed("suffix", raw)));
    }

    private boolean validateSuffix(Player player, String raw) {
        String stripped = Mini.stripTags(raw);
        if (stripped.length() > 16) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("status.suffix-too-long")));
            return false;
        }
        if (plugin.getChatManager().getFilter().isBlocked(stripped)) {
            player.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("chat.filtered")));
            return false;
        }
        return true;
    }

    public Component getFormattedSuffix(PlayerProfile profile) {
        if (profile.getCustomSuffix() == null || profile.getCustomSuffix().isEmpty()) {
            return Component.empty();
        }
        return Component.space().append(Mini.parse(profile.getCustomSuffix()));
    }

    public void grantTitleTokenByName(String playerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(target.getUniqueId());
        profile.setHasTitleToken(true);
        plugin.getDatabaseManager().saveProfile(profile);
    }
}
