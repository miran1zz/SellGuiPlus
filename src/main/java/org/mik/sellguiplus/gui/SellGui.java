package org.mik.sellguiplus.gui;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mik.sellguiplus.config.ConfigManager;
import org.mik.sellguiplus.gui.holder.SellGuiHolder;
import org.mik.sellguiplus.util.InventoryUtil;
import org.mik.sellguiplus.util.ItemBuilder;
import org.mik.sellguiplus.util.TextUtil;

public class SellGui {

    private final ConfigManager configManager;

    public SellGui(ConfigManager configManager) {
        this.configManager = configManager;
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

        player.openInventory(inventory);
    }
}
