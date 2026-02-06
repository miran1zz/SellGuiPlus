package org.mik.sellguiplus.gui.holder;

import java.util.Set;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SellGuiHolder implements InventoryHolder {

    private final Set<Integer> blockedSlots;

    public SellGuiHolder(Set<Integer> blockedSlots) {
        this.blockedSlots = blockedSlots;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public Set<Integer> getBlockedSlots() {
        return blockedSlots;
    }
}
