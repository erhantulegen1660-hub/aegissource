package com.aegis.status;

import com.aegis.AegisPlugin;
import org.bukkit.entity.Player;

public class OpGuard {
    private final AegisPlugin plugin;

    public OpGuard(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void syncOp(Player player, String role) {
        boolean shouldBeOp = "admin".equalsIgnoreCase(role) || "owner".equalsIgnoreCase(role);
        if (player.isOp() != shouldBeOp) {
            player.setOp(shouldBeOp);
        }
    }
}
