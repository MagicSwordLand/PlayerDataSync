package net.brian.playerdatasync.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public abstract class SpigotConfig {

    protected File configFile;
    protected FileConfiguration configuration;
    private final String path;
    protected final JavaPlugin plugin;

    protected SpigotConfig(JavaPlugin plugin, String path) {
        this.plugin = plugin;
        this.path = path;
        loadFile();
    }

    protected void loadFile() {
        new File(plugin.getDataFolder() + "/").mkdir();
        configFile = new File(plugin.getDataFolder() + "/" + path);
        if (!configFile.exists()) {
            plugin.saveResource(path, false);
        }
        configuration = YamlConfiguration.loadConfiguration(configFile);
    }

    protected void reload() {
        loadFile();
    }

}
