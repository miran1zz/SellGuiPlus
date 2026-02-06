package org.mik.sellguiplus.util;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class SoundUtil {

    private SoundUtil() {
    }

    public static Sound soundFromConfig(FileConfiguration config, String path, Sound fallback) {
        String raw = config.getString(path, fallback.name());
        try {
            return Sound.valueOf(raw.toUpperCase());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public static void play(Player player, Sound sound) {
        if (sound == null) {
            return;
        }
        player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }

    public static void spawnParticles(Player player, FileConfiguration config) {
        String type = config.getString("effects.particles.type", "VILLAGER_HAPPY");
        int count = config.getInt("effects.particles.count", 10);
        double offset = config.getDouble("effects.particles.offset", 0.4D);
        Particle particle;
        try {
            particle = Particle.valueOf(type.toUpperCase());
        } catch (Exception ignored) {
            particle = Particle.VILLAGER_HAPPY;
        }
        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), count, offset, offset, offset, 0.01D);
    }
}
