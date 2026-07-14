package org.mik.sellguiplus.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.Sellguiplus;
import org.mik.sellguiplus.gui.AdminGui;
import org.mik.sellguiplus.gui.ConfirmationGui;
import org.mik.sellguiplus.gui.holder.AdminGuiHolder;
import org.mik.sellguiplus.gui.holder.ConfirmationGuiHolder;
import org.mik.sellguiplus.gui.holder.SellGuiHolder;
import org.mik.sellguiplus.manager.SellProcessor;
import org.mik.sellguiplus.model.SellItem;
import org.mik.sellguiplus.util.SoundUtil;

public class GuiListener implements Listener {

    private final JavaPlugin plugin;
    private final AdminGui adminGui;
    private final ConfirmationGui confirmationGui;
    private final SellProcessor sellProcessor;
    private final Map<String, SellGuiHolder> pendingConfirmations = new HashMap<>();

    public GuiListener(JavaPlugin plugin, AdminGui adminGui, SellProcessor sellProcessor, ConfirmationGui confirmationGui) {
        this.plugin = plugin;
        this.adminGui = adminGui;
        this.sellProcessor = sellProcessor;
        this.confirmationGui = confirmationGui;
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

        // Handle confirmation GUI clicks
        if (holder instanceof ConfirmationGuiHolder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= event.getInventory().getSize()) {
                return;
            }

            // Confirm button (slot 12)
            if (slot == 12) {
                handleConfirmationAccept(player);
                return;
            }

            // Cancel button (slot 14)
            if (slot == 14) {
                handleConfirmationCancel(player);
                return;
            }
            return;
        }

        // Handle sell GUI clicks
        if (holder instanceof SellGuiHolder) {
            SellGuiHolder sellHolder = (SellGuiHolder) holder;
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < event.getInventory().getSize()) {
                Set<Integer> blocked = sellHolder.getBlockedSlots();
                
                // Check if this is the confirm button
                int confirmSlot = getConfirmButtonSlot();
                if (slot == confirmSlot) {
                    event.setCancelled(true);
                    handleConfirmSale(player, event.getInventory(), sellHolder);
                    return;
                }
                
                // Block clicks on border slots
                if (blocked.contains(slot)) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        // Handle admin GUI clicks
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
        
        Player player = (Player) event.getPlayer();
        SellGuiHolder holder = (SellGuiHolder) inventory.getHolder();
        String playerUUID = player.getUniqueId().toString();
        
        // Only return items if no confirmation is pending
        if (!pendingConfirmations.containsKey(playerUUID)) {
            ItemStack[] contents = inventory.getContents();
            List<ItemStack> toReturn = new ArrayList<>();
            
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                // Skip border slots and confirm button
                if (holder.getBlockedSlots().contains(i) || i == getConfirmButtonSlot()) {
                    continue;
                }
                if (item != null && item.getType() != Material.AIR) {
                    toReturn.add(item.clone());
                }
            }
            
            // Return items to player
            if (!toReturn.isEmpty()) {
                sellProcessor.returnItems(player, toReturn);
            }
        } else {
            // Remove from pending if this was a confirmation close
            pendingConfirmations.remove(playerUUID);
        }
    }

    private void handleConfirmSale(Player player, Inventory inventory, SellGuiHolder holder) {
        ItemStack[] contents = inventory.getContents();
        
        // Calculate total
        double total = sellProcessor.calculateTotal(player, contents, holder.getBlockedSlots());
        
        // Store pending items and total in holder
        holder.setPendingItems(contents.clone());
        holder.setPendingTotal(total);
        
        String currencySymbol = ((Sellguiplus) plugin).getEconomyManager().getCurrencySymbol();
        holder.setCurrencySymbol(currencySymbol);
        
        // Store in map
        String playerUUID = player.getUniqueId().toString();
        pendingConfirmations.put(playerUUID, holder);
        
        // Open confirmation GUI
        confirmationGui.open(player, total, currencySymbol);
    }

    private void handleConfirmationAccept(Player player) {
        String playerUUID = player.getUniqueId().toString();
        SellGuiHolder holder = pendingConfirmations.get(playerUUID);
        
        if (holder == null) {
            player.closeInventory();
            return;
        }
        
        ItemStack[] items = holder.getPendingItems();
        Set<Integer> blockedSlots = holder.getBlockedSlots();
        
        pendingConfirmations.remove(playerUUID);
        player.closeInventory();
        
        // Process the sale
        sellProcessor.sellFromGuiConfirmed(player, items, blockedSlots);
    }

    private void handleConfirmationCancel(Player player) {
        String playerUUID = player.getUniqueId().toString();
        SellGuiHolder holder = pendingConfirmations.get(playerUUID);
        
        if (holder != null) {
            // Return items to player
            ItemStack[] items = holder.getPendingItems();
            List<ItemStack> toReturn = new ArrayList<>();
            
            for (int i = 0; i < items.length; i++) {
                ItemStack item = items[i];
                if (holder.getBlockedSlots().contains(i)) {
                    continue;
                }
                if (item != null && item.getType() != Material.AIR) {
                    toReturn.add(item.clone());
                }
            }
            
            if (!toReturn.isEmpty()) {
                sellProcessor.returnItems(player, toReturn);
            }
            
            pendingConfirmations.remove(playerUUID);
        }
        
        player.closeInventory();
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

    private int getConfirmButtonSlot() {
        if (plugin instanceof Sellguiplus) {
            return ((Sellguiplus) plugin).getConfigManager().getMenus().getInt("sell-gui.confirm-button-slot", 49);
        }
        return 49;
    }
}
