package org.mik.sellguiplus.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.gui.AdminGui;
import org.mik.sellguiplus.manager.ChatInputManager;
import org.mik.sellguiplus.model.PendingChatAction;

public class ChatListener implements Listener {

    private final JavaPlugin plugin;
    private final ChatInputManager chatInputManager;
    private final AdminGui adminGui;

    public ChatListener(JavaPlugin plugin, ChatInputManager chatInputManager, AdminGui adminGui) {
        this.plugin = plugin;
        this.chatInputManager = chatInputManager;
        this.adminGui = adminGui;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PendingChatAction action = chatInputManager.getPending(player.getUniqueId());
        if (action == null) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        plugin.getServer().getScheduler().runTask(plugin, () ->
            adminGui.handleChatInput(player, action, message, action.getPage())
        );
    }
}
