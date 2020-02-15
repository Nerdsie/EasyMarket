package me.richardcollins.market.commands;

import me.richardcollins.market.Helper;
import me.richardcollins.market.Settings;
import me.richardcollins.market.managers.MarketManager;
import me.richardcollins.market.market.Market;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PriceCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Error: You must be a player to do this.");
            return true;
        }

        Player player = (Player) sender;
        String neededPerms = "mymarket.command.price";

        if (!sender.hasPermission(neededPerms)) {
            sender.sendMessage(ChatColor.RED + "Error: You need the '" + neededPerms + "' to do this.");
            return true;
        }

        if (args.length >= 1) {
            ItemStack toPrice = null;

            if (MarketManager.playerChosenItem(player.getName())) {
                toPrice = MarketManager.getChosenItemStack(player.getName());
            }

            if (args.length == 2) {
                if (args[1].equalsIgnoreCase("this") || args[1].equalsIgnoreCase("hand") || args[1].equalsIgnoreCase("inhand")) {
                    toPrice = player.getItemInHand();

                    if (toPrice == null || toPrice.getType() == Material.AIR) {
                        player.sendMessage(ChatColor.RED + "Error: You don't have an item in your");
                    }
                } else {
                    int data = 0;

                    if (args[1].contains(":")) {
                        if (toPrice.getType().getMaxDurability() <= 0) {
                            data = Integer.parseInt(args[1].split(":")[1]);
                        }

                        args[1] = args[1].split(":")[0];
                    }

                    try {
                        toPrice = new ItemStack(Material.getMaterial(args[1].toUpperCase()));
                    } catch (Exception e) {
                        toPrice = null;
                    }

                    if (toPrice == null) {
                        try {
                            toPrice = new ItemStack(Material.getMaterial(Integer.parseInt(args[1])));
                        } catch (Exception ee) {
                            toPrice = null;
                        }
                    }

                    if (toPrice == null) {
                        player.sendMessage(ChatColor.RED + "Error: The item " + args[1].toUpperCase() + " was not found.");
                        player.sendMessage(ChatColor.RED + "You can enter an item name OR an item id.");
                        return true;
                    }

                    try {
                        toPrice.setDurability((short) data);
                    } catch (Exception e) {
                        toPrice.setDurability((short) 0);
                    }
                }
            } else {
                if (!MarketManager.playerChosenItem(player.getName())) {
                    player.sendMessage(ChatColor.RED + "Error: Please use /market, select an item, then use /buy <amount>");
                    return true;
                }
            }

            int amount = 0;

            try {
                amount = Integer.parseInt(args[0]);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error: '" + args[0] + "' is not a valid amount.");
                return true;
            }

            if (amount <= 0.0) {
                player.sendMessage(ChatColor.RED + "Error: Amount must be positive.");
                return true;
            }

            int amountInMarket = Market.getCollection(toPrice, true).getAmount();

            if (amountInMarket < amount) {
                player.sendMessage(ChatColor.RED + "Error: You are trying to check the price for " + amount + " " + toPrice.getType().name() + "(S) but there are only " + amountInMarket + " for sale.");
                return true;
            }

            double totalPrice = Market.getPrice(toPrice, amount);

            player.sendMessage(Settings.PREFIX + "It would cost " + ChatColor.RED + totalPrice + " gold" + ChatColor.GREEN + " to buy " + ChatColor.RED + amount + " " + Helper.getName(toPrice) + "(S)");
            return true;
        }

        player.sendMessage(ChatColor.RED + "You can replace <item> with 'this' to use what's in your hand.");
        player.sendMessage(ChatColor.RED + "You can select an item with /market instead of entering an item.");
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "You can use this to withdraw gold ingots.");
        player.sendMessage(ChatColor.RED + "/price <amount> <item>");
        return true;
    }
}
