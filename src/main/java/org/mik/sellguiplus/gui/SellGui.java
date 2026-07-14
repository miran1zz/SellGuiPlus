package org.mik.sellguiplus.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mik.sellguiplus.config.ConfigManager;
import org.mik.sellguiplus.gui.holder.SellGuiHolder;
import org.mik.sellguiplus.manager.SellItemManager;
import org.mik.sellguiplus.model.SellItem;
import org.mik.sellguiplus.util.InventoryUtil;
import org.mik.sellguiplus.util.ItemBuilder;
import org.mik.sellguiplus.util.TextUtil;

public class SellGui {

    private final ConfigManager configManager;
    private final SellItemManager sellItemManager;

    public SellGui(ConfigManager configManager, SellItemManager sellItemManager) {
        this.configManager = configManager;
        this.sellItemManager = sellItemManager;
    }

    public void open(Player player) {
        FileConfiguration menus = configManager.getMenus();
        int size = menus.getInt("sell-gui.size", 54);
        String title = TextUtil.color(menus.getString("sell-gui.title", "&8Sell GUI"));

        Set<Integer> borderSlots = InventoryUtil.getBorderSlots(size);
        SellGuiHolder holder = new SellGuiHolder(borderSlots);
        Inventory inventory = Bukkit.createInventory(holder, size, title);

        Material borderMaterial = Material.matchMaterial(menus.getString("sell-gui.border-material", "GRAY_STAINED_GLASS_PANE"));
        String borderName = menus.getString("sell-gui.border-name", " ");
        ItemStack borderItem = ItemBuilder.build(borderMaterial, borderName, null);
        for (int slot : borderSlots) {
            inventory.setItem(slot, borderItem);
        }

        // Add confirm button
        List<String> confirmLore = new ArrayList<>();
        confirmLore.add(TextUtil.color("&7Click to proceed to confirmation"));
        ItemStack confirmButton = ItemBuilder.build(Material.LIME_CONCRETE, TextUtil.color("&a&lCONFIRM SALE"), confirmLore);
        int confirmSlot = menus.getInt("sell-gui.confirm-button-slot", 49);
        inventory.setItem(confirmSlot, confirmButton);

        player.openInventory(inventory);
    }

    public void addItemWithLore(Inventory inventory, ItemStack item, Player player, String currencySymbol) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        SellItem sellItem = sellItemManager.getByMaterial(item.getType());
        if (sellItem == null || !sellItem.isEnabled()) {
            return;
        }

        // Create a copy to avoid modifying the original
        ItemStack display = item.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        }

        if (meta != null) {
            List<String> lore = new ArrayList<>();
            
            // Calculate individual price and total price
            double unitPrice = sellItem.getPrice();
            double multiplier = 1.0D; // Will be set by GuiListener
            double itemTotal = unitPrice * item.getAmount() * multiplier;
            
            lore.add(TextUtil.color("&7Unit Price: &f" + currencySymbol + TextUtil.formatNumber(unitPrice)));
            lore.add(TextUtil.color("&7Stack Total: &a" + currencySymbol + TextUtil.formatNumber(itemTotal)));
            lore.add(TextUtil.color("&7Amount: &f" + item.getAmount()));
            
            meta.setLore(lore);
            display.setItemMeta(meta);
        }
    }
}
