package me.richardcollins.market.commands;

import me.richardcollins.market.Settings;
import me.richardcollins.market.managers.MarketManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Error: You must be a player to do this.");
            return true;
        }

        Player player = (Player) sender;
        String neededPerms = "mymarket.command.cancel";

        if (!sender.hasPermission(neededPerms)) {
            sender.sendMessage(ChatColor.RED + "Error: You need the '" + neededPerms + "' to do this.");
            return true;
        }

        if (MarketManager.playerHasPending(player.getName())) {
            player.sendMessage(Settings.PREFIX + ChatColor.GREEN + "Pending transaction " + ChatColor.RED + "cancelled.");
            MarketManager.removePending(player.getName());
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Error: You don't have any pending transactions to cancel.");
            return true;
        }
    }
}
