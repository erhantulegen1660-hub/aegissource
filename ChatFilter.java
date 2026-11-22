package com.aegis.chat;

import com.aegis.AegisPlugin;
import java.util.List;
import java.util.Locale;

public class ChatFilter {

    private final AegisPlugin plugin;

    public ChatFilter(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isBlocked(String rawMessage) {
        String normalized = rawMessage.toLowerCase(Locale.ROOT)
                .replace("@", "а")
                .replace("0", "о")
                .replace("3", "е")
                .replace("1", "и")
                .replace("6", "б");

        List<String> words = plugin.getConfigManager().main().getStringList("chat.blocked-words");
        for (String bad : words) {
            if (normalized.contains(bad.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        String compact = normalized.replace(" ", "");
        List<String> domains = plugin.getConfigManager().main().getStringList("chat.blocked-domains");
        for (String domain : domains) {
            if (compact.contains(domain.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
