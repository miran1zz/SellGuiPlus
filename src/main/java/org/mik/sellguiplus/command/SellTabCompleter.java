package org.mik.sellguiplus.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class SellTabCompleter implements TabCompleter {

    private static final List<String> SUBS = Arrays.asList("handitem", "inventory");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return SUBS.stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}
