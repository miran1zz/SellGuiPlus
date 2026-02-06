package org.mik.sellguiplus.config;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private File messagesFile;
    private File menusFile;
    private FileConfiguration messages;
    private FileConfiguration menus;
    private FileConfiguration defaultConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        loadMessages();
        loadMenus();
        loadDefaultConfig();
    }

    public void saveAll() {
        plugin.saveConfig();
        saveMessages();
        saveMenus();
    }

    public void reloadAll() {
        plugin.reloadConfig();
        loadMessages();
        loadMenus();
        loadDefaultConfig();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void loadMenus() {
        menusFile = new File(plugin.getDataFolder(), "menus.yml");
        if (!menusFile.exists()) {
            plugin.saveResource("menus.yml", false);
        }
        menus = YamlConfiguration.loadConfiguration(menusFile);
    }

    private void loadDefaultConfig() {
        if (plugin.getResource("config.yml") == null) {
            defaultConfig = new YamlConfiguration();
            return;
        }
        InputStreamReader reader = new InputStreamReader(plugin.getResource("config.yml"), StandardCharsets.UTF_8);
        defaultConfig = YamlConfiguration.loadConfiguration(reader);
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getMenus() {
        return menus;
    }

    public FileConfiguration getDefaultConfig() {
        return defaultConfig;
    }

    public void saveMessages() {
        try {
            messages.save(messagesFile);
        } catch (Exception ignored) {
        }
    }

    public void saveMenus() {
        try {
            menus.save(menusFile);
        } catch (Exception ignored) {
        }
    }
}
