package org.mik.sellguiplus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.gui.SellGui;
import org.mik.sellguiplus.util.TextUtil;

public class SellGuiCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final SellGui sellGui;

    public SellGuiCommand(JavaPlugin plugin, SellGui sellGui) {
        this.plugin = plugin;
        this.sellGui = sellGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            TextUtil.sendMessage(sender, plugin, "messages.player-only");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("sellgui.use")) {
            TextUtil.sendMessage(player, plugin, "messages.no-permission");
            return true;
        }
        sellGui.open(player);
        return true;
    }
}
