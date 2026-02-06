package org.mik.sellguiplus.util;

import java.text.DecimalFormat;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.Sellguiplus;

public final class TextUtil {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,###.##");

    private TextUtil() {
    }

    public static String color(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void sendMessage(CommandSender sender, JavaPlugin plugin, String path, String... placeholders) {
        String message = getMessage(plugin, path);
        if (message.isEmpty()) {
            return;
        }
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        sender.sendMessage(color(message));
    }

    public static String getMessage(JavaPlugin plugin, String path) {
        FileConfiguration messages = getMessages(plugin);
        String key = path.startsWith("messages.") ? path.substring("messages.".length()) : path;
        String prefix = messages.getString("prefix", "");
        String raw = messages.getString(key, "");
        if (raw.contains("{prefix}")) {
            return raw.replace("{prefix}", prefix);
        }
        return prefix + raw;
    }

    public static String formatNumber(double value) {
        return FORMAT.format(value);
    }

    private static FileConfiguration getMessages(JavaPlugin plugin) {
        if (plugin instanceof Sellguiplus) {
            return ((Sellguiplus) plugin).getConfigManager().getMessages();
        }
        return plugin.getConfig();
    }
}
