package me.richardcollins.market.commands;

import me.richardcollins.economy.Economy;
import me.richardcollins.market.Settings;
import me.richardcollins.market.Helper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WithdrawCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Error: You must be a player to do this.");
            return true;
        }

        Player player = (Player) sender;
        String neededPerms = "mymarket.command.withdraw";

        if (!sender.hasPermission(neededPerms)) {
            sender.sendMessage(ChatColor.RED + "Error: You need the '" + neededPerms + "' to do this.");
            return true;
        }

        if (args.length >= 1) {
            int amount = 0;

            if (args[0].equalsIgnoreCase("all")) {
                amount = (int) Economy.getAPI().getProfile(player.getName()).getBalance();
            } else {
                try {
                    amount = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Error: Amount must be a whole number.");
                    return true;
                }
            }

            if (amount <= 0.0) {
                player.sendMessage(ChatColor.RED + "Error: Amount must be positive.");
                return true;
            }

            double goldInBank = Economy.getAPI().getProfile(player.getName()).getBalance();

            if (goldInBank < amount) {
                player.sendMessage(ChatColor.RED + "Error: You cannot withdraw " + amount + " gold when you only have " + goldInBank + " in the bank.");
                return true;
            }

            int available = Helper.countAvailableSpace(player, new ItemStack(Material.GOLD_INGOT));
            if (available < amount) {
                amount = available;
            }

            Economy.getAPI().getProfile(player.getName()).removeGold(amount);
            player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, amount));

            player.sendMessage(Settings.PREFIX + ChatColor.RED + "" + amount + " gold" + ChatColor.GREEN + " withdrawn. " + ChatColor.GREEN + "|| " + ChatColor.RED + Economy.getAPI().getProfile(player.getName()).getBalance()
                    + " gold" + ChatColor.GREEN + " remaining.");
            return true;
        }

        player.sendMessage(ChatColor.RED + "You can put 'all' instead of an amount.");
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "You can use this to withdraw gold ingots.");
        player.sendMessage(ChatColor.RED + "/withdraw <amount>");

        return true;
    }
}
