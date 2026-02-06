package org.mik.sellguiplus.listeners;

import java.util.Set;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.Sellguiplus;
import org.mik.sellguiplus.gui.AdminGui;
import org.mik.sellguiplus.gui.holder.AdminGuiHolder;
import org.mik.sellguiplus.gui.holder.SellGuiHolder;
import org.mik.sellguiplus.manager.SellProcessor;
import org.mik.sellguiplus.model.SellItem;
import org.mik.sellguiplus.util.SoundUtil;

public class GuiListener implements Listener {

    private final JavaPlugin plugin;
    private final AdminGui adminGui;
    private final SellProcessor sellProcessor;

    public GuiListener(JavaPlugin plugin, AdminGui adminGui, SellProcessor sellProcessor) {
        this.plugin = plugin;
        this.adminGui = adminGui;
        this.sellProcessor = sellProcessor;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (holder instanceof SellGuiHolder) {
            SellGuiHolder sellHolder = (SellGuiHolder) holder;
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < event.getInventory().getSize()) {
                Set<Integer> blocked = sellHolder.getBlockedSlots();
                if (blocked.contains(slot)) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        if (holder instanceof AdminGuiHolder) {
            event.setCancelled(true);
            AdminGuiHolder adminHolder = (AdminGuiHolder) holder;
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= event.getInventory().getSize()) {
                return;
            }
            SellItem item = adminHolder.getItemSlots().get(slot);
            int page = adminHolder.getPage();
            if (item == null) {
                if (handleAdminSearchClick(player, slot, page)) {
                    return;
                }
                handleAdminNavClick(player, event.getInventory(), slot, page);
                return;
            }
            if (event.getClick() == ClickType.LEFT) {
                adminGui.handleLeftClick(player, item, page);
            } else if (event.getClick() == ClickType.RIGHT) {
                adminGui.handleRightClick(player, item, page);
            } else if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                adminGui.handleShiftClick(player, item, page);
            } else if (event.getClick() == ClickType.MIDDLE) {
                adminGui.handleMiddleClick(player, item, page);
            }
            if (plugin.getConfig().getBoolean("sounds.enabled", true)) {
                SoundUtil.play(player, SoundUtil.soundFromConfig(plugin.getConfig(), "sounds.gui-click", Sound.UI_BUTTON_CLICK));
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof SellGuiHolder)) {
            return;
        }
        SellGuiHolder sellHolder = (SellGuiHolder) holder;
        for (int slot : event.getRawSlots()) {
            if (sellHolder.getBlockedSlots().contains(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof SellGuiHolder)) {
            return;
        }
        SellGuiHolder holder = (SellGuiHolder) inventory.getHolder();
        Player player = (Player) event.getPlayer();
        sellProcessor.sellFromGui(player, inventory.getContents(), new java.util.ArrayList<>(holder.getBlockedSlots()));
    }

    private void handleAdminNavClick(Player player, Inventory inventory, int slot, int page) {
        int nextSlot = getMenuInt("admin-gui.next-page.slot", 53);
        int prevSlot = getMenuInt("admin-gui.prev-page.slot", 45);
        if (slot == nextSlot) {
            adminGui.open(player, page + 1);
            return;
        }
        if (slot == prevSlot) {
            adminGui.open(player, Math.max(0, page - 1));
            return;
        }
        if (plugin.getConfig().getBoolean("sounds.enabled", true)) {
            SoundUtil.play(player, SoundUtil.soundFromConfig(plugin.getConfig(), "sounds.gui-click", Sound.UI_BUTTON_CLICK));
        }
    }

    private boolean handleAdminSearchClick(Player player, int slot, int page) {
        int searchSlot = getMenuInt("admin-gui.search.slot", 49);
        int clearSlot = getMenuInt("admin-gui.search-clear.slot", 50);
        if (slot == searchSlot) {
            adminGui.openSearch(player, page);
            return true;
        }
        if (slot == clearSlot) {
            adminGui.clearSearch(player, page);
            return true;
        }
        return false;
    }

    private int getMenuInt(String path, int fallback) {
        if (plugin instanceof Sellguiplus) {
            return ((Sellguiplus) plugin).getConfigManager().getMenus().getInt(path, fallback);
        }
        return fallback;
    }
}
