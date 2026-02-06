package org.mik.sellguiplus.manager;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.model.SellItem;

public class SellItemManager {

    private final JavaPlugin plugin;
    private final Map<String, SellItem> items = new LinkedHashMap<>();
    private final Map<String, Double> defaultPrices = new HashMap<>();
    private final Map<Material, SellItem> byMaterial = new HashMap<>();

    public SellItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadItems() {
        items.clear();
        byMaterial.clear();
        loadDefaultPrices();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("items");
        if (section == null) {
            section = config.createSection("items");
        }
        boolean changed = false;
        for (Material material : Material.values()) {
            if (!material.isItem() || material == Material.AIR) {
                continue;
            }
            String key = material.name();
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection == null) {
                itemSection = section.createSection(key);
                itemSection.set("enabled", false);
                itemSection.set("price", 0.0D);
                itemSection.set("material", key);
                itemSection.set("display-name", key);
                changed = true;
            }
            String displayName = itemSection.getString("display-name", key);
            String skullTexture = itemSection.getString("skull-texture", "");
            double price = itemSection.getDouble("price", 0.0D);
            boolean enabled = itemSection.getBoolean("enabled", false);
            int adminSlot = itemSection.getInt("admin-slot", -1);
            int adminPage = itemSection.getInt("admin-page", -1);
            SellItem item = new SellItem(key, material, displayName, skullTexture, price, enabled, adminSlot, adminPage);
            items.put(key, item);
            byMaterial.put(material, item);
        }
        if (changed) {
            plugin.saveConfig();
        }
    }

    private void loadDefaultPrices() {
        defaultPrices.clear();
        if (plugin.getResource("config.yml") == null) {
            return;
        }
        InputStreamReader reader = new InputStreamReader(plugin.getResource("config.yml"), StandardCharsets.UTF_8);
        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
        ConfigurationSection section = defaultConfig.getConfigurationSection("items");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            double price = section.getDouble(key + ".price", 0.0D);
            defaultPrices.put(key, price);
        }
    }

    public Map<String, SellItem> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public SellItem getItem(String key) {
        return items.get(key);
    }

    public SellItem getByMaterial(Material material) {
        return byMaterial.get(material);
    }

    public void setEnabled(String key, boolean enabled) {
        SellItem item = items.get(key);
        if (item == null) {
            return;
        }
        item.setEnabled(enabled);
        plugin.getConfig().set("items." + key + ".enabled", enabled);
        plugin.saveConfig();
    }

    public void setPrice(String key, double price) {
        SellItem item = items.get(key);
        if (item == null) {
            return;
        }
        item.setPrice(price);
        plugin.getConfig().set("items." + key + ".price", price);
        plugin.saveConfig();
    }

    public void setAdminSlot(String key, int adminSlot, int adminPage) {
        SellItem item = items.get(key);
        if (item == null) {
            return;
        }
        item.setAdminSlot(adminSlot);
        item.setAdminPage(adminPage);
        plugin.getConfig().set("items." + key + ".admin-slot", adminSlot);
        plugin.getConfig().set("items." + key + ".admin-page", adminPage);
        plugin.saveConfig();
    }

    public void resetPrice(String key) {
        if (!defaultPrices.containsKey(key)) {
            setPrice(key, 0.0D);
            return;
        }
        setPrice(key, defaultPrices.get(key));
    }

    public boolean isBlacklisted(Material material) {
        for (String entry : plugin.getConfig().getStringList("economy.item-blacklist")) {
            if (entry.equalsIgnoreCase(material.name())) {
                return true;
            }
        }
        return false;
    }

    public boolean isWorldBlocked(String worldName) {
        for (String entry : plugin.getConfig().getStringList("economy.world-blacklist")) {
            if (entry.equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }

    private void ensureItems() {
        // Replaced by loadItems() full material scan.
    }
}
