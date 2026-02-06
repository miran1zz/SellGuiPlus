package org.mik.sellguiplus.economy;

import java.util.UUID;
import org.bukkit.entity.Player;

public interface EconomyProvider {

    boolean isReady();

    String getName();

    String getCurrencySymbol();

    void deposit(Player player, double amount);

    double getBalance(UUID uuid);
}
