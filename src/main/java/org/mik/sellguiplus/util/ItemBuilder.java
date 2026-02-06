package org.mik.sellguiplus.util;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public final class ItemBuilder {

    private ItemBuilder() {
    }

    public static ItemStack build(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material == null ? Material.STONE : material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.color(name));
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, TextUtil.color(lore.get(i)));
                }
                meta.setLore(lore);
            }
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack buildSkull(String name, List<String> lore, String texture) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.color(name));
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, TextUtil.color(lore.get(i)));
                }
                meta.setLore(lore);
            }
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }
}
