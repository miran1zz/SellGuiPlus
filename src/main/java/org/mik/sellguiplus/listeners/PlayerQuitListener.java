package org.mik.sellguiplus.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mik.sellguiplus.manager.ChatInputManager;
import org.mik.sellguiplus.manager.CooldownManager;
import org.mik.sellguiplus.manager.SignInputManager;

public class PlayerQuitListener implements Listener {

    private final ChatInputManager chatInputManager;
    private final CooldownManager cooldownManager;
    private final SignInputManager signInputManager;

    public PlayerQuitListener(ChatInputManager chatInputManager, CooldownManager cooldownManager, SignInputManager signInputManager) {
        this.chatInputManager = chatInputManager;
        this.cooldownManager = cooldownManager;
        this.signInputManager = signInputManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        chatInputManager.clear(event.getPlayer().getUniqueId());
        cooldownManager.clear(event.getPlayer().getUniqueId());
        signInputManager.clear(event.getPlayer().getUniqueId());
    }
}
