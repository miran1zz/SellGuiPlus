package org.mik.sellguiplus.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.gui.AdminGui;
import org.mik.sellguiplus.manager.SignInputManager;
import org.mik.sellguiplus.model.PendingSignAction;
import org.mik.sellguiplus.model.SignInputSession;

public class SignListener implements Listener {

    private final JavaPlugin plugin;
    private final SignInputManager signInputManager;
    private final AdminGui adminGui;

    public SignListener(JavaPlugin plugin, SignInputManager signInputManager, AdminGui adminGui) {
        this.plugin = plugin;
        this.signInputManager = signInputManager;
        this.adminGui = adminGui;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        SignInputSession session = signInputManager.getSession(event.getPlayer().getUniqueId());
        if (session == null) {
            return;
        }
        PendingSignAction action = session.getAction();
        String input = signInputManager.extractInput(event.getLines());
        signInputManager.complete(event.getPlayer().getUniqueId());
        if (action.getType() == PendingSignAction.Type.SEARCH) {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                adminGui.applySearch(event.getPlayer(), input, action.getPage())
            );
        }
    }
}
