package com.aegis.top;

import com.aegis.AegisPlugin;
import com.aegis.util.Mini;
import org.bukkit.command.CommandSender;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TopManager {

    private final AegisPlugin plugin;

    public TopManager(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleCommand(CommandSender sender, String[] args) {
        String category = args.length >= 2 ? args[1].toLowerCase() : "kills";
        String column = switch (category) {
            case "wins", "победы" -> "wins";
            case "legendaries", "легендарки" -> "crafted_legendaries";
            case "deaths", "смерти" -> "deaths";
            default -> "kills";
        };
        String title = switch (column) {
            case "wins" -> "Победам в КБ";
            case "crafted_legendaries" -> "Скрафченным Легендаркам";
            case "deaths" -> "Смертям";
            default -> "Убийствам";
        };

        sender.sendMessage(Mini.parse("<gradient:#a855f7:#e879f9><bold>✦ ТОП-10 ПО " + title.toUpperCase() + "</bold></gradient>"));
        try (PreparedStatement ps = plugin.getDatabaseManager().getConnection()
                .prepareStatement("SELECT username, " + column + " AS val FROM players ORDER BY " + column + " DESC LIMIT 10")) {
            ResultSet rs = ps.executeQuery();
            int place = 1;
            while (rs.next()) {
                sender.sendMessage(Mini.parse("<#c084fc>#" + place + "</#c084fc> <white>" + rs.getString("username") + "</white> <gray>—</gray> <#e879f9>" + rs.getInt("val") + "</#e879f9>"));
                place++;
            }
        } catch (Exception e) {
            sender.sendMessage(Mini.parse("<red>Ошибка загрузки рейтинга.</red>"));
        }
    }

    public void shutdown() {}
}
