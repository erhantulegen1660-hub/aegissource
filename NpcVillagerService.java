package com.aegis.hub;

import com.aegis.AegisPlugin;
import com.aegis.util.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class NpcVillagerService implements Listener {

    private final AegisPlugin plugin;
    private final NamespacedKey npcKey = new NamespacedKey("aegis", "kb_npc");

    public NpcVillagerService(AegisPlugin plugin) {
        this.plugin = plugin;
        spawnNpc();
    }

    public void spawnNpc() {
        FileConfiguration cfg = plugin.getConfigManager().getConfig("npcs.yml");
        World world = Bukkit.getWorld(cfg.getString("battle-royale-npc.world", "world_hub"));
        if (world == null) return;

        world.getEntitiesByClass(Villager.class).forEach(v -> {
            if (v.getPersistentDataContainer().has(npcKey, PersistentDataType.BYTE)) v.remove();
        });

        Location loc = new Location(world,
                cfg.getDouble("battle-royale-npc.x", 100.5),
                cfg.getDouble("battle-royale-npc.y", 64.0),
                cfg.getDouble("battle-royale-npc.z", 108.5),
                (float) cfg.getDouble("battle-royale-npc.yaw", 180.0),
                (float) cfg.getDouble("battle-royale-npc.pitch", 0.0));

        Villager npc = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
        npc.setAI(false);
        npc.setInvulnerable(true);
        npc.setSilent(true);
        npc.setProfession(Villager.Profession.WEAPONSMITH);
        npc.customName(Mini.parse(cfg.getString("battle-royale-npc.name", "<#a855f7>Королевская Битва</#a855f7>")));
        npc.setCustomNameVisible(true);
        npc.getPersistentDataContainer().set(npcKey, PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler
    public void onNpcClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager v
                && v.getPersistentDataContainer().has(npcKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            plugin.getMatchManager().openLobbySelector(event.getPlayer());
        }
    }

    @EventHandler
    public void onNpcDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Villager v
                && v.getPersistentDataContainer().has(npcKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }
}
