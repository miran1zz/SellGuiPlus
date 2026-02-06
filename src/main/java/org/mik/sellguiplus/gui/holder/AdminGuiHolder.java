package org.mik.sellguiplus.gui.holder;

import java.util.Map;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.mik.sellguiplus.model.SellItem;

public class AdminGuiHolder implements InventoryHolder {

    private final Map<Integer, SellItem> itemSlots;
    private final int page;

    public AdminGuiHolder(Map<Integer, SellItem> itemSlots, int page) {
        this.itemSlots = itemSlots;
        this.page = page;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public Map<Integer, SellItem> getItemSlots() {
        return itemSlots;
    }

    public int getPage() {
        return page;
    }
}
