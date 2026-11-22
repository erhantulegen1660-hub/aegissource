package com.aegis.match;

import com.aegis.AegisPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MatchListener implements Listener {

    private final AegisPlugin plugin;
    private final MatchManager matchManager;

    public MatchListener(AegisPlugin plugin, MatchManager matchManager) {
        this.plugin = plugin;
        this.matchManager = matchManager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        MatchArena arena = matchManager.getArenaByPlayer(p);
        if (arena == null) return;

        if (arena.getPhase() == MatchPhase.WAITING) {
            event.setCancelled(true);
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && arena.hasFallProtection()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        MatchArena arena = matchManager.getArenaByPlayer(victim);
        if (arena != null && !arena.isPvpEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        MatchArena arena = matchManager.getArenaByPlayer(victim);
        if (arena == null) return;

        event.setCancelled(true);
        arena.handleDeath(victim, victim.getKiller());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        matchManager.leaveMatch(event.getPlayer());
    }
}
