package org.mik.sellguiplus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mik.sellguiplus.manager.SellProcessor;
import org.mik.sellguiplus.util.TextUtil;

public class SellCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final SellProcessor sellProcessor;

    public SellCommand(JavaPlugin plugin, SellProcessor sellProcessor) {
        this.plugin = plugin;
        this.sellProcessor = sellProcessor;
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
        if (args.length == 0) {
            TextUtil.sendMessage(player, plugin, "messages.sell-usage");
            return true;
        }
        if ("handitem".equalsIgnoreCase(args[0])) {
            sellProcessor.sellHand(player);
            return true;
        }
        if ("inventory".equalsIgnoreCase(args[0])) {
            sellProcessor.sellInventory(player);
            return true;
        }
        TextUtil.sendMessage(player, plugin, "messages.sell-usage");
        return true;
    }
}
