package org.mik.sellguiplus;

import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.command.SellCommand;
import org.mik.sellguiplus.command.SellGuiAdminCommand;
import org.mik.sellguiplus.command.SellGuiAdminTabCompleter;
import org.mik.sellguiplus.command.SellGuiCommand;
import org.mik.sellguiplus.command.SellTabCompleter;
import org.mik.sellguiplus.config.ConfigManager;
import org.mik.sellguiplus.economy.EconomyManager;
import org.mik.sellguiplus.gui.AdminGui;
import org.mik.sellguiplus.gui.ConfirmationGui;
import org.mik.sellguiplus.gui.SellGui;
import org.mik.sellguiplus.listeners.ChatListener;
import org.mik.sellguiplus.listeners.GuiListener;
import org.mik.sellguiplus.listeners.PlayerQuitListener;
import org.mik.sellguiplus.listeners.SignListener;
import org.mik.sellguiplus.manager.ChatInputManager;
import org.mik.sellguiplus.manager.CooldownManager;
import org.mik.sellguiplus.manager.SellItemManager;
import org.mik.sellguiplus.manager.SellProcessor;
import org.mik.sellguiplus.manager.SignInputManager;

public final class Sellguiplus extends JavaPlugin {

    private ConfigManager configManager;
    private EconomyManager economyManager;
    private SellItemManager sellItemManager;
    private SellProcessor sellProcessor;
    private CooldownManager cooldownManager;
    private ChatInputManager chatInputManager;
    private SignInputManager signInputManager;
    private SellGui sellGui;
    private ConfirmationGui confirmationGui;
    private AdminGui adminGui;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadAll();

        economyManager = new EconomyManager(this);
        economyManager.setup();

        sellItemManager = new SellItemManager(this);
        sellItemManager.loadItems();

        cooldownManager = new CooldownManager(this);
        chatInputManager = new ChatInputManager();
        signInputManager = new SignInputManager(this);
        sellProcessor = new SellProcessor(this, economyManager, sellItemManager, cooldownManager);

        sellGui = new SellGui(configManager, sellItemManager);
        confirmationGui = new ConfirmationGui(configManager);
        adminGui = new AdminGui(this, configManager, sellItemManager, chatInputManager, signInputManager);

        getCommand("sell").setExecutor(new SellCommand(this, sellProcessor));
        getCommand("sell").setTabCompleter(new SellTabCompleter());
        getCommand("sellgui").setExecutor(new SellGuiCommand(this, sellGui));
        getCommand("sellguiadmin").setExecutor(new SellGuiAdminCommand(this, adminGui));
        getCommand("sellguiadmin").setTabCompleter(new SellGuiAdminTabCompleter());

        getServer().getPluginManager().registerEvents(new GuiListener(this, adminGui, sellProcessor, confirmationGui), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, chatInputManager, adminGui), this);
        getServer().getPluginManager().registerEvents(new SignListener(this, signInputManager, adminGui), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(chatInputManager, cooldownManager, signInputManager), this);
    }

    @Override
    public void onDisable() {
        if (configManager != null) {
            configManager.saveAll();
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public SellItemManager getSellItemManager() {
        return sellItemManager;
    }

    public void reloadPlugin() {
        configManager.reloadAll();
        sellItemManager.loadItems();
        economyManager.setup();
    }
}
