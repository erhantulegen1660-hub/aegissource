package com.aegis.config;

import com.aegis.AegisPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final AegisPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final List<String> fileNames = List.of(
            "config.yml", "messages.yml", "tab.yml", "cases.yml",
            "legendaries.yml", "classes.yml", "teams.yml",
            "npcs.yml", "tops.yml", "hud.yml"
    );

    public ConfigManager(AegisPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        configs.clear();
        for (String name : fileNames) {
            File file = new File(plugin.getDataFolder(), name);
            if (!file.exists()) {
                plugin.saveResource(name, false);
            }
            configs.put(name, YamlConfiguration.loadConfiguration(file));
        }
    }

    public FileConfiguration getConfig(String fileName) {
        return configs.getOrDefault(fileName, plugin.getConfig());
    }

    public FileConfiguration main() {
        return getConfig("config.yml");
    }

    public String getMessage(String path) {
        FileConfiguration msg = getConfig("messages.yml");
        String prefix = msg.getString("prefix", "");
        String raw = msg.getString(path, "<red>Missing message: " + path + "</red>");
        return prefix + raw;
    }

    public String getRawMessage(String path) {
        return getConfig("messages.yml").getString(path, "");
    }
}
