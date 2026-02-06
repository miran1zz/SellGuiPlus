package org.mik.sellguiplus.economy;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyManager {

    private final JavaPlugin plugin;
    private EconomyProvider provider;
    private final List<MultiplierEntry> multipliers = new ArrayList<>();

    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        VaultEconomyProvider vault = new VaultEconomyProvider(plugin);
        vault.setup();
        provider = vault.isReady() ? vault : null;

        loadMultipliers();
    }

    private void loadMultipliers() {
        multipliers.clear();
        List<?> list = plugin.getConfig().getMapList("multipliers");
        if (list == null) {
            return;
        }
        for (Object obj : list) {
            if (!(obj instanceof java.util.Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) obj;
            String permission = map.getOrDefault("permission", "").toString();
            double value;
            try {
                value = Double.parseDouble(map.getOrDefault("value", "1.0").toString());
            } catch (NumberFormatException ex) {
                value = 1.0D;
            }
            if (!permission.isEmpty()) {
                multipliers.add(new MultiplierEntry(permission, value));
            }
        }
    }

    public boolean isReady() {
        return provider != null && provider.isReady();
    }

    public String getCurrencySymbol() {
        if (provider == null) {
            return plugin.getConfig().getString("currency.symbol", "$");
        }
        return plugin.getConfig().getString("currency.symbol", "$");
    }

    public void deposit(Player player, double amount) {
        if (provider != null) {
            provider.deposit(player, amount);
        }
    }

    public double getMultiplier(Player player) {
        double max = 1.0D;
        for (MultiplierEntry entry : multipliers) {
            if (player.hasPermission(entry.permission) && entry.value > max) {
                max = entry.value;
            }
        }
        return max;
    }

    public String getProviderName() {
        return provider == null ? "None" : provider.getName();
    }

    private static class MultiplierEntry {
        private final String permission;
        private final double value;

        private MultiplierEntry(String permission, double value) {
            this.permission = permission;
            this.value = value;
        }
    }
}
