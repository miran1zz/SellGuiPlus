package org.mik.sellguiplus.gui.holder;

import java.util.Set;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class SellGuiHolder implements InventoryHolder {

    private final Set<Integer> blockedSlots;
    private ItemStack[] pendingItems;
    private double pendingTotal;
    private String currencySymbol;

    public SellGuiHolder(Set<Integer> blockedSlots) {
        this.blockedSlots = blockedSlots;
        this.pendingItems = null;
        this.pendingTotal = 0.0D;
        this.currencySymbol = "$";
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public Set<Integer> getBlockedSlots() {
        return blockedSlots;
    }

    public ItemStack[] getPendingItems() {
        return pendingItems;
    }

    public void setPendingItems(ItemStack[] items) {
        this.pendingItems = items;
    }

    public double getPendingTotal() {
        return pendingTotal;
    }

    public void setPendingTotal(double total) {
        this.pendingTotal = total;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String symbol) {
        this.currencySymbol = symbol;
    }
}
