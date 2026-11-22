package com.aegis.data;

import com.aegis.AegisPlugin;
import com.aegis.classes.GameClass;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {

    private final AegisPlugin plugin;
    private Connection connection;
    private final Map<UUID, PlayerProfile> cache = new ConcurrentHashMap<>();

    public DatabaseManager(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            FileConfiguration cfg = plugin.getConfigManager().main();
            if (cfg.getBoolean("database.mysql.enabled", false)) {
                String host = cfg.getString("database.mysql.host", "127.0.0.1");
                int port = cfg.getInt("database.mysql.port", 3306);
                String db = cfg.getString("database.mysql.database", "aegis");
                String user = cfg.getString("database.mysql.username", "aegis");
                String pass = cfg.getString("database.mysql.password", "");
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&autoReconnect=true", user, pass);
            } else {
                File dbFile = new File(plugin.getDataFolder(), "aegis.db");
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            }
            createTables();
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка инициализации БД: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(32),
                    role VARCHAR(24),
                    role_expire BIGINT,
                    vipplus_color VARCHAR(24),
                    has_title_token BOOLEAN,
                    custom_suffix TEXT,
                    amethysts INT,
                    kills INT,
                    deaths INT,
                    wins INT,
                    losses INT,
                    crafted_legendaries INT,
                    playtime_sec BIGINT,
                    free_case_claimed BOOLEAN,
                    active_cosmetic VARCHAR(64),
                    unlocked_cosmetics TEXT,
                    selected_class VARCHAR(24),
                    class_levels TEXT
                );
            """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS offline_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username VARCHAR(32),
                    amethysts INT
                );
            """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS tops (
                    period VARCHAR(16),
                    uuid VARCHAR(36),
                    username VARCHAR(32),
                    kills INT,
                    wins INT,
                    legendaries INT,
                    deaths INT,
                    PRIMARY KEY (period, uuid)
                );
            """);
        }
    }

    public PlayerProfile getProfile(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> loadProfile(id, Bukkit.getOfflinePlayer(id).getName()));
    }

    public PlayerProfile loadProfile(UUID uuid, String name) {
        PlayerProfile profile = new PlayerProfile(uuid, name != null ? name : "Unknown");
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                profile.setRole(rs.getString("role"));
                profile.setRoleExpireAt(rs.getLong("role_expire"));
                profile.setVipPlusColor(rs.getString("vipplus_color"));
                profile.setHasTitleToken(rs.getBoolean("has_title_token"));
                profile.setCustomSuffix(rs.getString("custom_suffix"));
                profile.setAmethysts(rs.getInt("amethysts"));
                profile.setKills(rs.getInt("kills"));
                profile.setDeaths(rs.getInt("deaths"));
                profile.setWins(rs.getInt("wins"));
                profile.setLosses(rs.getInt("losses"));
                profile.setCraftedLegendaries(rs.getInt("crafted_legendaries"));
                profile.setPlayTimeSeconds(rs.getLong("playtime_sec"));
                profile.setFreeCaseClaimed(rs.getBoolean("free_case_claimed"));
                profile.setActiveCosmeticId(rs.getString("active_cosmetic"));
                String cos = rs.getString("unlocked_cosmetics");
                if (cos != null && !cos.isEmpty()) {
                    profile.getUnlockedCosmetics().addAll(Arrays.asList(cos.split(",")));
                }
                try {
                    profile.setSelectedClass(GameClass.valueOf(rs.getString("selected_class")));
                } catch (Exception ignored) {}
                String lvls = rs.getString("class_levels");
                if (lvls != null && !lvls.isEmpty()) {
                    for (String pair : lvls.split(";")) {
                        String[] kv = pair.split(":");
                        if (kv.length == 2) {
                            profile.getClassLevels().put(GameClass.valueOf(kv[0]), Integer.parseInt(kv[1]));
                        }
                    }
                }
            } else {
                saveProfile(profile);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Ошибка загрузки профиля " + uuid + ": " + e.getMessage());
        }
        cache.put(uuid, profile);
        return profile;
    }

    public void saveProfile(PlayerProfile p) {
        StringBuilder lvls = new StringBuilder();
        p.getClassLevels().forEach((k, v) -> lvls.append(k.name()).append(":").append(v).append(";"));
        String cos = String.join(",", p.getUnlockedCosmetics());

        String sql = """
            REPLACE INTO players (uuid, username, role, role_expire, vipplus_color, has_title_token,
            custom_suffix, amethysts, kills, deaths, wins, losses, crafted_legendaries, playtime_sec,
            free_case_claimed, active_cosmetic, unlocked_cosmetics, selected_class, class_levels)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getUuid().toString());
            ps.setString(2, p.getUsername());
            ps.setString(3, p.getRole());
            ps.setLong(4, p.getRoleExpireAt());
            ps.setString(5, p.getVipPlusColor());
            ps.setBoolean(6, p.isHasTitleToken());
            ps.setString(7, p.getCustomSuffix());
            ps.setInt(8, p.getAmethysts());
            ps.setInt(9, p.getKills());
            ps.setInt(10, p.getDeaths());
            ps.setInt(11, p.getWins());
            ps.setInt(12, p.getLosses());
            ps.setInt(13, p.getCraftedLegendaries());
            ps.setLong(14, p.getPlayTimeSeconds());
            ps.setBoolean(15, p.isFreeCaseClaimed());
            ps.setString(16, p.getActiveCosmeticId());
            ps.setString(17, cos);
            ps.setString(18, p.getSelectedClass().name());
            ps.setString(19, lvls.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Ошибка сохранения профиля: " + e.getMessage());
        }
    }

    public void queueOfflineAmethysts(String username, int amount) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO offline_queue (username, amethysts) VALUES (?, ?)")) {
            ps.setString(1, username.toLowerCase());
            ps.setInt(2, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Ошибка добавления в оффлайн-очередь: " + e.getMessage());
        }
    }

    public int claimOfflineAmethysts(String username) {
        int total = 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT SUM(amethysts) FROM offline_queue WHERE username = ?")) {
            ps.setString(1, username.toLowerCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getInt(1);
        } catch (SQLException ignored) {}
        if (total > 0) {
            try (PreparedStatement del = connection.prepareStatement("DELETE FROM offline_queue WHERE username = ?")) {
                del.setString(1, username.toLowerCase());
                del.executeUpdate();
            } catch (SQLException ignored) {}
        }
        return total;
    }

    public Connection getConnection() { return connection; }

    public void close() {
        cache.values().forEach(this::saveProfile);
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
    }
}
