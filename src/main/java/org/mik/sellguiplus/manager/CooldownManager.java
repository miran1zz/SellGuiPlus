package org.mik.sellguiplus.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.java.JavaPlugin;

public class CooldownManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public CooldownManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(UUID uuid) {
        long seconds = plugin.getConfig().getLong("economy.cooldown-seconds", 0L);
        if (seconds <= 0) {
            return false;
        }
        long last = cooldowns.getOrDefault(uuid, 0L);
        long now = System.currentTimeMillis();
        return now - last < seconds * 1000L;
    }

    public long getRemaining(UUID uuid) {
        long seconds = plugin.getConfig().getLong("economy.cooldown-seconds", 0L);
        if (seconds <= 0) {
            return 0L;
        }
        long last = cooldowns.getOrDefault(uuid, 0L);
        long now = System.currentTimeMillis();
        long remaining = (seconds * 1000L) - (now - last);
        return Math.max(0L, remaining / 1000L);
    }

    public void mark(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    public void clear(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
