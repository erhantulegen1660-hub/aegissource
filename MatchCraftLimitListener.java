package com.aegis.match;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.legendary.LegendaryItem;
import com.aegis.util.Mini;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class MatchCraftLimitListener implements Listener {

    private final AegisPlugin plugin;
    private final MatchManager matchManager;

    public MatchCraftLimitListener(AegisPlugin plugin, MatchManager matchManager) {
        this.plugin = plugin;
        this.matchManager = matchManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        ItemStack result = event.getRecipe().getResult();

        MatchArena arena = matchManager.getArenaByPlayer(p);
        if (arena != null && result.getType() == Material.GOLDEN_APPLE) {
            if (!arena.canCraftGapple(p.getUniqueId())) {
                event.setCancelled(true);
                p.sendMessage(Mini.parse(plugin.getConfigManager().getMessage("match.gapple-limit")));
                return;
            }
        }

        LegendaryItem leg = plugin.getLegendaryManager().identify(result);
        if (leg != null) {
            PlayerProfile prof = plugin.getDatabaseManager().getProfile(p.getUniqueId());
            prof.setCraftedLegendaries(prof.getCraftedLegendaries() + 1);
            plugin.getDatabaseManager().saveProfile(prof);
            if (arena != null) {
                arena.broadcast(plugin.getConfigManager().getRawMessage("match.legendary-crafted")
                        .replace("<player>", p.getName())
                        .replace("<item>", leg.getDisplayName()));
            }
        }
    }
}
