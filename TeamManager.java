package com.aegis.team;

import com.aegis.AegisPlugin;
import com.aegis.util.Mini;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamManager {

    private final AegisPlugin plugin;
    private final Map<UUID, Team> playerTeams = new HashMap<>();
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public TeamManager(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public Team getTeam(UUID playerUuid) {
        return playerTeams.get(playerUuid);
    }

    public void handleCommand(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(Mini.parse("<#a855f7>/team invite <ник></#a855f7> | <#a855f7>/team accept</#a855f7> | <#a855f7>/team decline</#a855f7> | <#a855f7>/team leave</#a855f7>"));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "invite" -> {
                if (args.length < 2) return;
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || target.equals(player)) {
                    player.sendMessage(Mini.parse("<red>Игрок не найден.</red>"));
                    return;
                }
                Team team = playerTeams.computeIfAbsent(player.getUniqueId(), Team::new);
                if (!team.getLeader().equals(player.getUniqueId())) {
                    player.sendMessage(Mini.parse("<red>Только лидер команды может приглашать игроков.</red>"));
                    return;
                }
                if (team.getMembers().size() >= 4) {
                    player.sendMessage(Mini.parse("<red>В команде максимум 4 игрока.</red>"));
                    return;
                }
                pendingInvites.put(target.getUniqueId(), player.getUniqueId());
                player.sendMessage(Mini.parse("<#c084fc>Приглашение отправлено игроку " + target.getName() + ".</#c084fc>"));
                target.sendMessage(Mini.parse("<#a855f7>Игрок " + player.getName() + " пригласил вас в команду КБ! Введите /team accept</#a855f7>"));
            }
            case "accept" -> {
                UUID leaderId = pendingInvites.remove(player.getUniqueId());
                if (leaderId == null) {
                    player.sendMessage(Mini.parse("<red>У вас нет активных приглашений.</red>"));
                    return;
                }
                Team team = playerTeams.get(leaderId);
                if (team == null || team.getMembers().size() >= 4) {
                    player.sendMessage(Mini.parse("<red>Команда недоступна или заполнена.</red>"));
                    return;
                }
                leaveTeam(player);
                team.getMembers().add(player.getUniqueId());
                playerTeams.put(player.getUniqueId(), team);
                player.sendMessage(Mini.parse("<#c084fc>Вы вступили в команду!</#c084fc>"));
            }
            case "decline" -> {
                pendingInvites.remove(player.getUniqueId());
                player.sendMessage(Mini.parse("<gray>Приглашение отклонено.</gray>"));
            }
            case "leave" -> {
                leaveTeam(player);
                player.sendMessage(Mini.parse("<gray>Вы покинули команду.</gray>"));
            }
        }
    }

    public void leaveTeam(Player player) {
        Team team = playerTeams.remove(player.getUniqueId());
        if (team == null) return;
        team.getMembers().remove(player.getUniqueId());
        if (team.getMembers().isEmpty()) return;
        if (team.getLeader().equals(player.getUniqueId())) {
            team.setLeader(team.getMembers().iterator().next());
        }
    }
}
