package org.mik.sellguiplus.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.config.ConfigManager;
import org.mik.sellguiplus.gui.holder.AdminGuiHolder;
import org.mik.sellguiplus.manager.ChatInputManager;
import org.mik.sellguiplus.manager.SellItemManager;
import org.mik.sellguiplus.manager.SignInputManager;
import org.mik.sellguiplus.model.PendingChatAction;
import org.mik.sellguiplus.model.SellItem;
import org.mik.sellguiplus.util.InventoryUtil;
import org.mik.sellguiplus.util.ItemBuilder;
import org.mik.sellguiplus.util.TextUtil;
import org.bukkit.ChatColor;

public class AdminGui {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final SellItemManager sellItemManager;
    private final ChatInputManager chatInputManager;
    private final SignInputManager signInputManager;
    private final Map<UUID, String> searches = new ConcurrentHashMap<>();

    public AdminGui(JavaPlugin plugin, ConfigManager configManager, SellItemManager sellItemManager, ChatInputManager chatInputManager, SignInputManager signInputManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.sellItemManager = sellItemManager;
        this.chatInputManager = chatInputManager;
        this.signInputManager = signInputManager;
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        FileConfiguration menus = configManager.getMenus();
        FileConfiguration messages = configManager.getMessages();
        int size = menus.getInt("admin-gui.size", 54);
        String title = TextUtil.color(menus.getString("admin-gui.title", "&8SellGUI Admin"))
            .replace("{page}", String.valueOf(page + 1));

        Map<Integer, SellItem> itemSlots = new HashMap<>();
        AdminGuiHolder holder = new AdminGuiHolder(itemSlots, page);
        Inventory inventory = Bukkit.createInventory(holder, size, title);

        applyBorder(inventory, menus, size);

        List<Integer> slots = menus.getIntegerList("admin-gui.item-slots");
        if (slots == null || slots.isEmpty()) {
            slots = getDefaultSlots(size);
        }
        int perPage = slots.size();
        List<SellItem> items = new ArrayList<>(sellItemManager.getItems().values());
        items.sort(Comparator.comparing(SellItem::getKey));
        String search = getSearch(player);
        if (search != null) {
            String normalized = search.trim().toLowerCase();
            if (!normalized.isEmpty()) {
                items.removeIf(item -> !matchesSearch(item, normalized));
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil(items.size() / (double) perPage));
        int clampedPage = Math.max(0, Math.min(page, totalPages - 1));

        renderNavigation(inventory, menus, clampedPage, totalPages);
        renderSearch(inventory, menus, search);

        int start = clampedPage * perPage;
        int end = Math.min(items.size(), start + perPage);
        List<SellItem> pageItems = items.subList(start, end);

        Map<Integer, SellItem> placed = new HashMap<>();
        List<Integer> available = new ArrayList<>(slots);

        for (SellItem item : pageItems) {
            int adminSlot = item.getAdminSlot();
            int adminPage = item.getAdminPage();
            if (adminSlot >= 0 && adminPage == clampedPage && available.contains(adminSlot)) {
                ItemStack stack = buildAdminItem(messages, item);
                inventory.setItem(adminSlot, stack);
                placed.put(adminSlot, item);
                available.remove((Integer) adminSlot);
            }
        }

        int index = 0;
        for (SellItem item : pageItems) {
            if (placed.containsValue(item)) {
                continue;
            }
            while (index < available.size() && inventory.getItem(available.get(index)) != null) {
                index++;
            }
            if (index >= available.size()) {
                break;
            }
            int slot = available.get(index);
            ItemStack stack = buildAdminItem(messages, item);
            inventory.setItem(slot, stack);
            itemSlots.put(slot, item);
            index++;
        }

        itemSlots.putAll(placed);
        player.openInventory(inventory);
    }

    public void handleLeftClick(Player player, SellItem item, int page) {
        sellItemManager.setEnabled(item.getKey(), !item.isEnabled());
        open(player, page);
    }

    public void handleRightClick(Player player, SellItem item, int page) {
        chatInputManager.setPending(player.getUniqueId(), new PendingChatAction(PendingChatAction.Type.PRICE, item.getKey(), page));
        TextUtil.sendMessage(player, plugin, "messages.admin-set-price");
        player.closeInventory();
    }

    public void handleShiftClick(Player player, SellItem item, int page) {
        chatInputManager.setPending(player.getUniqueId(), new PendingChatAction(PendingChatAction.Type.SLOT, item.getKey(), page));
        TextUtil.sendMessage(player, plugin, "messages.admin-set-slot");
        player.closeInventory();
    }

    public void handleMiddleClick(Player player, SellItem item, int page) {
        sellItemManager.resetPrice(item.getKey());
        open(player, page);
        TextUtil.sendMessage(player, plugin, "messages.admin-reset-price");
    }

    public void handleChatInput(Player player, PendingChatAction action, String message, int page) {
        if (action.getType() == PendingChatAction.Type.PRICE) {
            try {
                double price = Double.parseDouble(message);
                if (price < 0) {
                    TextUtil.sendMessage(player, plugin, "messages.admin-invalid-number");
                } else {
                    sellItemManager.setPrice(action.getItemKey(), price);
                    TextUtil.sendMessage(player, plugin, "messages.admin-price-set", "{price}", TextUtil.formatNumber(price));
                }
            } catch (NumberFormatException ex) {
                TextUtil.sendMessage(player, plugin, "messages.admin-invalid-number");
            }
        } else if (action.getType() == PendingChatAction.Type.SLOT) {
            try {
                int slot = Integer.parseInt(message);
                int size = configManager.getMenus().getInt("admin-gui.size", 54);
                if (slot < 0 || slot >= size) {
                    TextUtil.sendMessage(player, plugin, "messages.admin-invalid-number");
                } else {
                    sellItemManager.setAdminSlot(action.getItemKey(), slot, page);
                    TextUtil.sendMessage(player, plugin, "messages.admin-slot-set", "{slot}", String.valueOf(slot));
                }
            } catch (NumberFormatException ex) {
                TextUtil.sendMessage(player, plugin, "messages.admin-invalid-number");
            }
        }
        chatInputManager.clear(player.getUniqueId());
        open(player, page);
    }

    public void openSearch(Player player, int page) {
        signInputManager.openSearch(player, page);
    }

    public void applySearch(Player player, String input, int page) {
        if (input == null || input.trim().isEmpty()) {
            searches.remove(player.getUniqueId());
            TextUtil.sendMessage(player, plugin, "messages.admin-search-cleared");
        } else {
            String trimmed = input.trim();
            searches.put(player.getUniqueId(), trimmed);
            TextUtil.sendMessage(player, plugin, "messages.admin-search-set", "{query}", trimmed);
        }
        open(player, page);
    }

    public void clearSearch(Player player, int page) {
        searches.remove(player.getUniqueId());
        TextUtil.sendMessage(player, plugin, "messages.admin-search-cleared");
        open(player, page);
    }

    public String getSearch(Player player) {
        return searches.get(player.getUniqueId());
    }

    private ItemStack buildAdminItem(FileConfiguration messages, SellItem item) {
        String statusKey = item.isEnabled() ? "gui.status-enabled" : "gui.status-disabled";
        String status = messages.getString(statusKey, item.isEnabled() ? "&aEnabled" : "&cDisabled");
        List<String> lore = new ArrayList<>(messages.getStringList("gui.admin-item-lore"));
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, lore.get(i)
                .replace("{price}", TextUtil.formatNumber(item.getPrice()))
                .replace("{status}", status)
                .replace("{currency}", plugin.getConfig().getString("currency.symbol", "$")));
        }
        if (item.getSkullTexture() != null && !item.getSkullTexture().isEmpty()) {
            return ItemBuilder.buildSkull(item.getDisplayName(), lore, item.getSkullTexture());
        }
        return ItemBuilder.build(item.getMaterial(), item.getDisplayName(), lore);
    }

    private void renderNavigation(Inventory inventory, FileConfiguration menus, int page, int totalPages) {
        int nextSlot = menus.getInt("admin-gui.next-page.slot", 53);
        int prevSlot = menus.getInt("admin-gui.prev-page.slot", 45);
        if (page > 0) {
            inventory.setItem(prevSlot, ItemBuilder.build(
                Material.matchMaterial(menus.getString("admin-gui.prev-page.material", "ARROW")),
                menus.getString("admin-gui.prev-page.name", "&ePrevious"),
                menus.getStringList("admin-gui.prev-page.lore")
            ));
        }
        if (page + 1 < totalPages) {
            inventory.setItem(nextSlot, ItemBuilder.build(
                Material.matchMaterial(menus.getString("admin-gui.next-page.material", "ARROW")),
                menus.getString("admin-gui.next-page.name", "&eNext"),
                menus.getStringList("admin-gui.next-page.lore")
            ));
        }
    }

    private void renderSearch(Inventory inventory, FileConfiguration menus, String query) {
        int searchSlot = menus.getInt("admin-gui.search.slot", 49);
        int clearSlot = menus.getInt("admin-gui.search-clear.slot", 50);
        List<String> searchLore = menus.getStringList("admin-gui.search.lore");
        List<String> clearLore = menus.getStringList("admin-gui.search-clear.lore");
        String displayQuery = (query == null || query.isEmpty()) ? "-" : query;

        ItemStack searchItem = ItemBuilder.build(
            Material.matchMaterial(menus.getString("admin-gui.search.material", "OAK_SIGN")),
            menus.getString("admin-gui.search.name", "&eSearch"),
            replaceLore(searchLore, "{query}", displayQuery)
        );
        ItemStack clearItem = ItemBuilder.build(
            Material.matchMaterial(menus.getString("admin-gui.search-clear.material", "BARRIER")),
            menus.getString("admin-gui.search-clear.name", "&cClear Search"),
            replaceLore(clearLore, "{query}", displayQuery)
        );
        inventory.setItem(searchSlot, searchItem);
        inventory.setItem(clearSlot, clearItem);
    }

    private List<String> replaceLore(List<String> lore, String key, String value) {
        List<String> out = new ArrayList<>();
        for (String line : lore) {
            out.add(line.replace(key, value));
        }
        return out;
    }

    private boolean matchesSearch(SellItem item, String search) {
        String key = item.getKey().toLowerCase();
        String name = ChatColor.stripColor(TextUtil.color(item.getDisplayName())).toLowerCase();
        String material = item.getMaterial().name().toLowerCase();
        return key.contains(search) || name.contains(search) || material.contains(search);
    }

    private List<Integer> getDefaultSlots(int size) {
        List<Integer> slots = new ArrayList<>();
        List<Integer> border = new ArrayList<>(InventoryUtil.getBorderSlots(size));
        for (int i = 0; i < size; i++) {
            if (border.contains(i)) {
                continue;
            }
            slots.add(i);
        }
        return slots;
    }

    private void applyBorder(Inventory inventory, FileConfiguration menus, int size) {
        Material material = Material.matchMaterial(menus.getString("admin-gui.border-material", "BLACK_STAINED_GLASS_PANE"));
        String name = menus.getString("admin-gui.border-name", " ");
        ItemStack border = ItemBuilder.build(material, name, null);
        for (int slot : InventoryUtil.getBorderSlots(size)) {
            inventory.setItem(slot, border);
        }
    }
}
