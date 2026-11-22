package com.aegis.match;

import com.aegis.AegisPlugin;
import com.aegis.data.PlayerProfile;
import com.aegis.util.Mini;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MatchArena {

    private final AegisPlugin plugin;
    private final String id;
    private World world;
    private MatchPhase phase = MatchPhase.WAITING;
    private final Set<UUID> participants = new HashSet<>();
    private final Set<UUID> alive = new HashSet<>();
    private final Map<UUID, Integer> matchKills = new HashMap<>();
    private final Map<UUID, Integer> gapplesCrafted = new HashMap<>();
    private int timerSeconds;
    private long matchStartMillis;
    private long fallProtectionUntil;
    private BukkitTask tickTask;

    public MatchArena(AegisPlugin plugin, String id) {
        this.plugin = plugin;
        this.id = id;
        createWorld();
        startTicker();
    }

    private void createWorld() {
        WorldCreator wc = new WorldCreator("aegis_" + id);
        wc.environment(World.Environment.NORMAL);
        this.world = wc.createWorld();
        if (world != null) {
            world.getWorldBorder().setCenter(0.5, 0.5);
            world.getWorldBorder().setSize(1000.0);
            buildWaitingCube();
        }
    }

    private void buildWaitingCube() {
        // Куб ожидания 35x35x14 над центром карты (y = 200..213)
        int half = 17;
        int baseY = 200;
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                for (int y = baseY; y <= baseY + 13; y++) {
                    boolean isFloorOrCeil = (y == baseY || y == baseY + 13);
                    boolean isWall = (Math.abs(x) == half || Math.abs(z) == half);
                    if (isFloorOrCeil || isWall) {
                        world.getBlockAt(x, y, z).setType(Material.PURPLE_STAINED_GLASS);
                    } else {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
    }

    private void clearWaitingCube() {
        int half = 17;
        int baseY = 200;
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                for (int y = baseY; y <= baseY + 13; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    public void addPlayer(Player player) {
        if (phase != MatchPhase.WAITING) {
            player.sendMessage(Mini.parse("<red>Матч на этой арене уже идёт!</red>"));
            return;
        }
        if (participants.size() >= 32) {
            player.sendMessage(Mini.parse("<red>Арена заполнена (32/32)!</red>"));
            return;
        }
        participants.add(player.getUniqueId());
        alive.add(player.getUniqueId());
        matchKills.put(player.getUniqueId(), 0);
        gapplesCrafted.put(player.getUniqueId(), 0);

        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(new Location(world, 0.5, 202.0, 0.5));
        broadcast("<#c084fc>" + player.getName() + " присоединился к бою (" + participants.size() + "/32)</#c084fc>");
    }

    public void removePlayer(Player player) {
        participants.remove(player.getUniqueId());
        alive.remove(player.getUniqueId());
        checkWinCondition();
    }

    private void startTicker() {
        this.timerSeconds = 30;
        this.tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        if (phase == MatchPhase.WAITING) {
            int minPlayers = plugin.getConfigManager().main().getInt("match.min-players-to-start", 4);
            if (participants.size() < minPlayers) {
                timerSeconds = 30;
                return;
            }
            timerSeconds--;
            if (timerSeconds <= 0) {
                startMatch();
            }
            return;
        }

        if (phase == MatchPhase.MINING) {
            timerSeconds--;
            if (timerSeconds <= 0) {
                phase = MatchPhase.SHRINKING;
                timerSeconds = 600;
                world.getWorldBorder().setSize(150.0, 600L);
                broadcast(plugin.getConfigManager().getRawMessage("match.phase-shrinking"));
            }
        } else if (phase == MatchPhase.SHRINKING) {
            timerSeconds--;
            if (timerSeconds <= 0) {
                phase = MatchPhase.FINAL;
                timerSeconds = 600;
                world.getWorldBorder().setSize(50.0, 300L);
                broadcast(plugin.getConfigManager().getRawMessage("match.phase-final"));
            }
        } else if (phase == MatchPhase.FINAL) {
            timerSeconds--;
            if (timerSeconds <= 0) {
                endMatch(null);
            }
        }
    }

    private void startMatch() {
        phase = MatchPhase.MINING;
        timerSeconds = 1800;
        matchStartMillis = System.currentTimeMillis();
        fallProtectionUntil = matchStartMillis + 10_000L;
        clearWaitingCube();

        for (UUID uuid : new HashSet<>(alive)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            int rx = ThreadLocalRandom.current().nextInt(-420, 421);
            int rz = ThreadLocalRandom.current().nextInt(-420, 421);
            int ry = world.getHighestBlockYAt(rx, rz) + 2;
            p.teleport(new Location(world, rx + 0.5, ry, rz + 0.5));
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.getInventory().clear();
            plugin.getClassService().applyMatchKit(p);
        }
        broadcast(plugin.getConfigManager().getRawMessage("match.phase-mining"));
    }

    public void handleDeath(Player victim, Player killer) {
        if (!alive.remove(victim.getUniqueId())) return;
        PlayerProfile vp = plugin.getDatabaseManager().getProfile(victim.getUniqueId());
        vp.setDeaths(vp.getDeaths() + 1);
        vp.setLosses(vp.getLosses() + 1);
        plugin.getDatabaseManager().saveProfile(vp);

        if (killer != null && !killer.equals(victim)) {
            matchKills.merge(killer.getUniqueId(), 1, Integer::sum);
            PlayerProfile kp = plugin.getDatabaseManager().getProfile(killer.getUniqueId());
            kp.setKills(kp.getKills() + 1);
            kp.addAmethysts(15);
            plugin.getDatabaseManager().saveProfile(kp);
            killer.sendMessage(Mini.parse("<#e879f9>+15 аметистов за убийство " + victim.getName() + "!</#e879f9>"));
        }

        victim.setGameMode(GameMode.SPECTATOR);
        checkWinCondition();
    }

    private void checkWinCondition() {
        if (phase == MatchPhase.WAITING || phase == MatchPhase.ENDING) return;
        if (alive.size() <= 1) {
            UUID winnerId = alive.isEmpty() ? null : alive.iterator().next();
            endMatch(winnerId);
        }
    }

    private void endMatch(UUID winnerId) {
        phase = MatchPhase.ENDING;
        if (winnerId != null) {
            Player winner = Bukkit.getPlayer(winnerId);
            PlayerProfile wp = plugin.getDatabaseManager().getProfile(winnerId);
            wp.setWins(wp.getWins() + 1);
            wp.addAmethysts(50);
            plugin.getDatabaseManager().saveProfile(wp);
            if (winner != null) {
                broadcast("<gradient:#a855f7:#e879f9><bold>🏆 Победитель матча: " + winner.getName() + " (+50 аметистов)!</bold></gradient>");
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID uuid : new HashSet<>(participants)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) plugin.getHubService().sendToHub(p);
            }
            participants.clear();
            alive.clear();
            matchKills.clear();
            gapplesCrafted.clear();
            world.getWorldBorder().setSize(1000.0);
            buildWaitingCube();
            phase = MatchPhase.WAITING;
            timerSeconds = 30;
        }, 100L);
    }

    public boolean isPvpEnabled() {
        return phase == MatchPhase.SHRINKING || phase == MatchPhase.FINAL;
    }

    public boolean hasFallProtection() {
        return System.currentTimeMillis() < fallProtectionUntil;
    }

    public boolean isSmelterUnlocked() {
        return phase != MatchPhase.WAITING && (System.currentTimeMillis() - matchStartMillis) >= 120_000L;
    }

    public boolean canCraftGapple(UUID uuid) {
        int count = gapplesCrafted.getOrDefault(uuid, 0);
        if (count >= 2) return false;
        gapplesCrafted.put(uuid, count + 1);
        return true;
    }

    public void broadcast(String miniMsg) {
        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(Mini.parse(miniMsg));
        }
    }

    public void shutdown() {
        if (tickTask != null) tickTask.cancel();
    }

    public String getId() { return id; }
    public World getWorld() { return world; }
    public MatchPhase getPhase() { return phase; }
    public Set<UUID> getParticipants() { return participants; }
    public Set<UUID> getAlive() { return alive; }
    public int getTimerSeconds() { return timerSeconds; }
}
