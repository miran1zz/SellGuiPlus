package org.mik.sellguiplus.model;

import org.bukkit.Material;

public class SellItem {

    private final String key;
    private Material material;
    private String displayName;
    private String skullTexture;
    private double price;
    private boolean enabled;
    private int adminSlot;
    private int adminPage;

    public SellItem(String key, Material material, String displayName, String skullTexture, double price, boolean enabled, int adminSlot, int adminPage) {
        this.key = key;
        this.material = material;
        this.displayName = displayName;
        this.skullTexture = skullTexture;
        this.price = price;
        this.enabled = enabled;
        this.adminSlot = adminSlot;
        this.adminPage = adminPage;
    }

    public String getKey() {
        return key;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSkullTexture() {
        return skullTexture;
    }

    public void setSkullTexture(String skullTexture) {
        this.skullTexture = skullTexture;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAdminSlot() {
        return adminSlot;
    }

    public void setAdminSlot(int adminSlot) {
        this.adminSlot = adminSlot;
    }

    public int getAdminPage() {
        return adminPage;
    }

    public void setAdminPage(int adminPage) {
        this.adminPage = adminPage;
    }
}
