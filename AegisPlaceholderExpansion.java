package com.aegis;

import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class AegisPlaceholderExpansion extends PlaceholderExpansion {

    private final AegisPlugin plugin;

    public AegisPlaceholderExpansion(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "aegis"; }

    @Override
    public @NotNull String getAuthor() { return "AEGIS Team"; }

    @Override
    public @NotNull String getVersion() { return "1.0.0"; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        PlayerProfile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        return switch (params.toLowerCase()) {
            case "amethysts" -> String.valueOf(profile.getAmethysts());
            case "kills" -> String.valueOf(profile.getKills());
            case "deaths" -> String.valueOf(profile.getDeaths());
            case "wins" -> String.valueOf(profile.getWins());
            case "role" -> plugin.getStatusManager().getEffectiveRole(profile);
            case "suffix" -> Mini.stripTags(profile.getCustomSuffix());
            default -> "";
        };
    }
}
