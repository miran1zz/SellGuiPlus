package org.mik.sellguiplus.economy;

import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultEconomyProvider implements EconomyProvider {

    private final JavaPlugin plugin;
    private Economy economy;

    public VaultEconomyProvider(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
    }

    @Override
    public boolean isReady() {
        return economy != null;
    }

    @Override
    public String getName() {
        return "Vault";
    }

    @Override
    public String getCurrencySymbol() {
        if (economy == null) {
            return "$";
        }
        return economy.currencyNamePlural();
    }

    @Override
    public void deposit(Player player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    @Override
    public double getBalance(UUID uuid) {
        if (economy == null) {
            return 0.0D;
        }
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            return economy.getBalance(player);
        }
        return 0.0D;
    }
}
