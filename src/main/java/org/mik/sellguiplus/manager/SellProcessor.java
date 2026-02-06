package org.mik.sellguiplus.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.economy.EconomyManager;
import org.mik.sellguiplus.model.SellItem;
import org.mik.sellguiplus.util.SoundUtil;
import org.mik.sellguiplus.util.TextUtil;

public class SellProcessor {

    private final JavaPlugin plugin;
    private final EconomyManager economyManager;
    private final SellItemManager sellItemManager;
    private final CooldownManager cooldownManager;

    public SellProcessor(JavaPlugin plugin, EconomyManager economyManager, SellItemManager sellItemManager, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.sellItemManager = sellItemManager;
        this.cooldownManager = cooldownManager;
    }

    public void sellHand(Player player) {
        if (!ensureReady(player)) {
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR || hand.getAmount() <= 0) {
            TextUtil.sendMessage(player, plugin, "messages.nothing-to-sell");
            return;
        }
        if (sellItemManager.isBlacklisted(hand.getType())) {
            TextUtil.sendMessage(player, plugin, "messages.item-blacklisted");
            return;
        }
        SellItem item = sellItemManager.getByMaterial(hand.getType());
        if (item == null || !item.isEnabled()) {
            TextUtil.sendMessage(player, plugin, "messages.item-disabled");
            return;
        }
        int sellAmount = applyMaxPerClick(hand.getAmount());
        if (sellAmount <= 0) {
            TextUtil.sendMessage(player, plugin, "messages.nothing-to-sell");
            return;
        }
        double total = item.getPrice() * sellAmount * economyManager.getMultiplier(player);
        hand.setAmount(hand.getAmount() - sellAmount);
        player.getInventory().setItemInMainHand(hand.getAmount() <= 0 ? null : hand);
        economyManager.deposit(player, total);
        cooldownManager.mark(player.getUniqueId());
        sendSuccess(player, sellAmount, total);
    }

    public void sellInventory(Player player) {
        if (!ensureReady(player)) {
            return;
        }
        Map<Material, Integer> counts = new HashMap<>();
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }
            if (sellItemManager.isBlacklisted(stack.getType())) {
                continue;
            }
            SellItem item = sellItemManager.getByMaterial(stack.getType());
            if (item == null || !item.isEnabled()) {
                continue;
            }
            counts.put(stack.getType(), counts.getOrDefault(stack.getType(), 0) + stack.getAmount());
        }
        if (counts.isEmpty()) {
            TextUtil.sendMessage(player, plugin, "messages.nothing-to-sell");
            return;
        }
        sellCountsAsync(player, counts);
    }

    public void sellFromGui(Player player, ItemStack[] contents, List<Integer> blockedSlots) {
        if (!ensureReady(player)) {
            returnItems(player, collectReturnItems(contents, blockedSlots));
            return;
        }
        ItemStack[] snapshot = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            if (blockedSlots != null && blockedSlots.contains(i)) {
                snapshot[i] = null;
                continue;
            }
            ItemStack stack = contents[i];
            snapshot[i] = stack == null ? null : stack.clone();
        }
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int maxPerClick = plugin.getConfig().getInt("economy.max-sell-per-click", 0);
            double multiplier = economyManager.getMultiplier(player);
            int totalAmount = 0;
            double totalPrice = 0.0D;
            List<ItemStack> toReturn = new ArrayList<>();

            for (ItemStack stack : snapshot) {
                if (stack == null || stack.getType() == Material.AIR) {
                    continue;
                }
                Material material = stack.getType();
                if (sellItemManager.isBlacklisted(material)) {
                    toReturn.add(stack);
                    continue;
                }
                SellItem item = sellItemManager.getByMaterial(material);
                if (item == null || !item.isEnabled()) {
                    toReturn.add(stack);
                    continue;
                }
                int amount = stack.getAmount();
                if (maxPerClick > 0 && totalAmount + amount > maxPerClick) {
                    int allowed = Math.max(0, maxPerClick - totalAmount);
                    if (allowed > 0) {
                        totalAmount += allowed;
                        totalPrice += item.getPrice() * allowed * multiplier;
                    }
                    int remaining = amount - allowed;
                    if (remaining > 0) {
                        ItemStack remainStack = stack.clone();
                        remainStack.setAmount(remaining);
                        toReturn.add(remainStack);
                    }
                    break;
                }
                totalAmount += amount;
                totalPrice += item.getPrice() * amount * multiplier;
            }

            final int finalAmount = totalAmount;
            final double finalPrice = totalPrice;
            final List<ItemStack> finalReturn = new ArrayList<>(toReturn);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                if (finalAmount <= 0) {
                    returnItems(player, finalReturn);
                    TextUtil.sendMessage(player, plugin, "messages.nothing-to-sell");
                    return;
                }
                returnItems(player, finalReturn);
                economyManager.deposit(player, finalPrice);
                cooldownManager.mark(uuid);
                sendSuccess(player, finalAmount, finalPrice);
            });
        });
    }

    private void sellCountsAsync(Player player, Map<Material, Integer> counts) {
        UUID uuid = player.getUniqueId();
        double multiplier = economyManager.getMultiplier(player);
        int maxPerClick = plugin.getConfig().getInt("economy.max-sell-per-click", 0);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<Material, Integer> capped = new HashMap<>();
            int totalAmount = 0;
            double totalPrice = 0.0D;

            for (Map.Entry<Material, Integer> entry : counts.entrySet()) {
                SellItem item = sellItemManager.getByMaterial(entry.getKey());
                if (item == null) {
                    continue;
                }
                int amount = entry.getValue();
                if (maxPerClick > 0 && totalAmount + amount > maxPerClick) {
                    amount = Math.max(0, maxPerClick - totalAmount);
                }
                if (amount <= 0) {
                    break;
                }
                totalAmount += amount;
                totalPrice += item.getPrice() * amount * multiplier;
                capped.put(entry.getKey(), amount);
                if (maxPerClick > 0 && totalAmount >= maxPerClick) {
                    break;
                }
            }

            if (totalAmount <= 0) {
                Bukkit.getScheduler().runTask(plugin, () ->
                    TextUtil.sendMessage(player, plugin, "messages.nothing-to-sell")
                );
                return;
            }

            final int finalAmount = totalAmount;
            final double finalPrice = totalPrice;
            final Map<Material, Integer> finalCapped = new HashMap<>(capped);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                removeItems(player, finalCapped);
                economyManager.deposit(player, finalPrice);
                cooldownManager.mark(uuid);
                sendSuccess(player, finalAmount, finalPrice);
            });
        });
    }

    private boolean ensureReady(Player player) {
        if (!economyManager.isReady()) {
            TextUtil.sendMessage(player, plugin, "messages.economy-missing");
            return false;
        }
        if (sellItemManager.isWorldBlocked(player.getWorld().getName())) {
            TextUtil.sendMessage(player, plugin, "messages.world-blocked");
            return false;
        }
        if (cooldownManager.isOnCooldown(player.getUniqueId())) {
            long remaining = cooldownManager.getRemaining(player.getUniqueId());
            TextUtil.sendMessage(player, plugin, "messages.cooldown", "{seconds}", String.valueOf(remaining));
            return false;
        }
        return true;
    }

    private void removeItems(Player player, Map<Material, Integer> toRemove) {
        for (Map.Entry<Material, Integer> entry : toRemove.entrySet()) {
            int remaining = entry.getValue();
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack stack = contents[i];
                if (stack == null || stack.getType() != entry.getKey()) {
                    continue;
                }
                int take = Math.min(remaining, stack.getAmount());
                stack.setAmount(stack.getAmount() - take);
                if (stack.getAmount() <= 0) {
                    contents[i] = null;
                }
                remaining -= take;
                if (remaining <= 0) {
                    break;
                }
            }
            player.getInventory().setContents(contents);
        }
    }

    private void returnItems(Player player, List<ItemStack> items) {
        for (ItemStack stack : items) {
            Map<Integer, ItemStack> remaining = player.getInventory().addItem(stack);
            for (ItemStack drop : remaining.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
    }

    private List<ItemStack> collectReturnItems(ItemStack[] contents, List<Integer> blockedSlots) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < contents.length; i++) {
            if (blockedSlots != null && blockedSlots.contains(i)) {
                continue;
            }
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }
            items.add(stack.clone());
        }
        return items;
    }

    private int applyMaxPerClick(int amount) {
        int maxPerClick = plugin.getConfig().getInt("economy.max-sell-per-click", 0);
        if (maxPerClick <= 0) {
            return amount;
        }
        return Math.min(amount, maxPerClick);
    }

    private void sendSuccess(Player player, int amount, double total) {
        TextUtil.sendMessage(player, plugin, "messages.sell-success",
            "{amount}", String.valueOf(amount),
            "{total}", TextUtil.formatNumber(total),
            "{currency}", economyManager.getCurrencySymbol()
        );
        if (plugin.getConfig().getBoolean("sounds.enabled", true)) {
            SoundUtil.play(player, SoundUtil.soundFromConfig(plugin.getConfig(), "sounds.sell-success", Sound.ENTITY_PLAYER_LEVELUP));
        }
        if (plugin.getConfig().getBoolean("effects.title.enabled", false)) {
            String title = TextUtil.color(plugin.getConfig().getString("effects.title.title", "&aSold!"));
            String subtitle = TextUtil.color(plugin.getConfig().getString("effects.title.subtitle", "&7+{total}{currency}")
                .replace("{amount}", String.valueOf(amount))
                .replace("{total}", TextUtil.formatNumber(total))
                .replace("{currency}", economyManager.getCurrencySymbol()));
            int fadeIn = plugin.getConfig().getInt("effects.title.fade-in", 5);
            int stay = plugin.getConfig().getInt("effects.title.stay", 20);
            int fadeOut = plugin.getConfig().getInt("effects.title.fade-out", 5);
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
        if (plugin.getConfig().getBoolean("effects.particles.enabled", false)) {
            SoundUtil.spawnParticles(player, plugin.getConfig());
        }
    }
}
