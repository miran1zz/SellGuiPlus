package org.mik.sellguiplus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mik.sellguiplus.Sellguiplus;
import org.mik.sellguiplus.gui.AdminGui;
import org.mik.sellguiplus.util.TextUtil;

public class SellGuiAdminCommand implements CommandExecutor {

    private final Sellguiplus plugin;
    private final AdminGui adminGui;

    public SellGuiAdminCommand(Sellguiplus plugin, AdminGui adminGui) {
        this.plugin = plugin;
        this.adminGui = adminGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("sellgui.admin")) {
                TextUtil.sendMessage(player, plugin, "messages.no-permission");
                return true;
            }
        }
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            TextUtil.sendMessage(sender, plugin, "messages.admin-help");
            return true;
        }
        if ("reload".equalsIgnoreCase(args[0])) {
            plugin.reloadPlugin();
            TextUtil.sendMessage(sender, plugin, "messages.admin-reload");
            return true;
        }
        if (!(sender instanceof Player)) {
            TextUtil.sendMessage(sender, plugin, "messages.player-only");
            return true;
        }
        Player player = (Player) sender;
        if ("config".equalsIgnoreCase(args[0])) {
            adminGui.open(player, 0);
            return true;
        }
        TextUtil.sendMessage(player, plugin, "messages.admin-help");
        return true;
    }
}
