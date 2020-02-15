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

public class DepositCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Error: You must be a player to do this.");
            return true;
        }

        Player player = (Player) sender;
        String neededPerms = "mymarket.command.deposit";

        if (!sender.hasPermission(neededPerms)) {
            sender.sendMessage(ChatColor.RED + "Error: You need the '" + neededPerms + "' to do this.");
            return true;
        }

        if (args.length == 1) {
            int amount = 0;

            if (args[0].equalsIgnoreCase("all")) {
                amount = Helper.countItemsInInventory(player, new ItemStack(Material.GOLD_INGOT), false);
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

            int heldGold = Helper.countItemsInInventory(player, new ItemStack(Material.GOLD_INGOT), false);

            if (heldGold < amount) {
                player.sendMessage(ChatColor.RED + "Error: You cannot deposit " + amount + " gold when you only have " + heldGold + " in your inventory.");
                return true;
            }

            Economy.getAPI().getProfile(player.getName()).addGold(amount);
            Helper.removeItemsFromInventory(player, new ItemStack(Material.GOLD_INGOT), false, amount);

            player.sendMessage(Settings.PREFIX + ChatColor.RED + "" + amount + " gold" + ChatColor.GREEN + " deposited into the bank.");
            return true;
        } else {

            player.sendMessage(ChatColor.RED + "You can put 'all' instead of an amount.");
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "You can use this to deposit gold ingots.");
            player.sendMessage(ChatColor.RED + "/deposit <amount>");
        }

        return true;
    }
}
