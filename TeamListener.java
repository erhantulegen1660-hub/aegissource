package com.aegis.team;

import com.aegis.AegisPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TeamListener implements Listener {

    private final AegisPlugin plugin;
    private final TeamManager teamManager;

    public TeamListener(AegisPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onFriendlyFire(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        Team t1 = teamManager.getTeam(victim.getUniqueId());
        Team t2 = teamManager.getTeam(attacker.getUniqueId());
        if (t1 != null && t1.equals(t2)) {
            event.setCancelled(true);
        }
    }
}
