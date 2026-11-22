package com.aegis.match;

import com.aegis.AegisPlugin;
import com.aegis.classes.ClassService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SmelterPickaxeListener implements Listener {

    private final AegisPlugin plugin;
    private final MatchManager matchManager;

    public SmelterPickaxeListener(AegisPlugin plugin, MatchManager matchManager) {
        this.plugin = plugin;
        this.matchManager = matchManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onOreBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        MatchArena arena = matchManager.getArenaByPlayer(p);
        if (arena == null || !arena.isSmelterUnlocked()) return;

        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!tool.hasItemMeta() || !tool.getItemMeta().getPersistentDataContainer().has(ClassService.SMELTER_PICK_KEY, PersistentDataType.BYTE)) {
            return;
        }

        Block b = event.getBlock();
        Material smelted = switch (b.getType()) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            default -> null;
        };

        if (smelted != null) {
            event.setDropItems(false);
            b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(smelted, 1));
        }
    }
}
