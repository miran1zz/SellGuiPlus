package org.mik.sellguiplus.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mik.sellguiplus.config.ConfigManager;
import org.mik.sellguiplus.gui.holder.ConfirmationGuiHolder;
import org.mik.sellguiplus.util.ItemBuilder;
import org.mik.sellguiplus.util.TextUtil;

public class ConfirmationGui {

    private final ConfigManager configManager;

    public ConfirmationGui(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void open(Player player, double totalPrice, String currencySymbol) {
        FileConfiguration menus = configManager.getMenus();
        String title = TextUtil.color(menus.getString("confirmation-gui.title", "&8Confirm Sale"));
        Inventory inventory = Bukkit.createInventory(new ConfirmationGuiHolder(), 27, title);

        // Set border items
        Material borderMaterial = Material.matchMaterial(menus.getString("confirmation-gui.border-material", "GRAY_STAINED_GLASS_PANE"));
        String borderName = menus.getString("confirmation-gui.border-name", " ");
        ItemStack borderItem = ItemBuilder.build(borderMaterial, borderName, null);

        // Fill borders (top, bottom, and sides)
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(18 + i, borderItem);
        }
        for (int i = 0; i < 3; i++) {
            inventory.setItem(i * 9, borderItem);
            inventory.setItem(i * 9 + 8, borderItem);
        }

        // Total price display (center top area)
        List<String> priceLore = new ArrayList<>();
        priceLore.add(TextUtil.color("&f" + currencySymbol + TextUtil.formatNumber(totalPrice)));
        ItemStack priceDisplay = ItemBuilder.build(Material.GOLD_BLOCK, TextUtil.color("&6Total Sale Value"), priceLore);
        inventory.setItem(11, priceDisplay);

        // Confirm button (left side)
        List<String> confirmLore = new ArrayList<>();
        confirmLore.add(TextUtil.color("&aClick to confirm"));
        ItemStack confirmButton = ItemBuilder.build(Material.LIME_CONCRETE, TextUtil.color("&a&lCONFIRM"), confirmLore);
        inventory.setItem(12, confirmButton);

        // Cancel button (right side)
        List<String> cancelLore = new ArrayList<>();
        cancelLore.add(TextUtil.color("&cClick to cancel"));
        ItemStack cancelButton = ItemBuilder.build(Material.RED_CONCRETE, TextUtil.color("&c&lCANCEL"), cancelLore);
        inventory.setItem(14, cancelButton);

        player.openInventory(inventory);
    }
}
