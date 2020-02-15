package me.richardcollins.market.commands;

import me.richardcollins.market.Helper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MarketCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Error: You must be a player to do this.");
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("info")) {
                me.richardcollins.economy.Helper.sendEconomyInfo((Player) sender);
                return true;
            }
        }

        Player player = (Player) sender;
        String neededPerms = "mymarket.command.market";

        if (!sender.hasPermission(neededPerms)) {
            sender.sendMessage(ChatColor.RED + "Error: You need the '" + neededPerms + "' to do this.");
            return true;
        }

        int page = 1;

        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Exception e) {
                page = 0;
            }
        }

        Helper.openMarket(player, page - 1);

        return true;
    }
}
