package me.richardcollins.market.commands;

import me.richardcollins.economy.Economy;
import me.richardcollins.market.Settings;
import me.richardcollins.market.managers.MarketManager;
import me.richardcollins.market.market.Market;
import me.richardcollins.market.market.PendingOffer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BuyCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Error: You must be a player to do this.");
            return true;
        }

        Player player = (Player) sender;
        String neededPerms = "mymarket.command.buy";

        if (!sender.hasPermission(neededPerms)) {
            sender.sendMessage(ChatColor.RED + "Error: You need the '" + neededPerms + "' to do this.");
            return true;
        }

        if (args.length >= 1) {
            ItemStack toBuy = null;

            if (MarketManager.playerChosenItem(player.getName())) {
                toBuy = MarketManager.getChosenItemStack(player.getName());
            }

            boolean autoConfirm = false;
            double expectedPrice = 0.0;

            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("this") || args[1].equalsIgnoreCase("hand") || args[1].equalsIgnoreCase("inhand")) {
                    toBuy = player.getItemInHand();

                    if (toBuy == null || toBuy.getType() == Material.AIR) {
                        player.sendMessage(ChatColor.RED + "Error: You don't have an item in your");
                    }
                } else {
                    int data = 0;

                    if (args[1].contains(":")) {
                        String[] split = args[1].split(":");

                        try {
                            toBuy = new ItemStack(Material.getMaterial(Integer.parseInt(split[0])));
                        } catch (Exception e) {
                            try {
                                toBuy = new ItemStack(Material.getMaterial(split[0]));
                            } catch (Exception ex) {

                            }
                        }

                        if (toBuy.getType().getMaxDurability() <= 0) {
                            try {
                                data = Integer.parseInt(split[1]);
                            } catch (Exception e) {

                            }
                        }

                        args[1] = args[1].split(":")[0];
                    }

                    try {
                        toBuy = new ItemStack(Material.getMaterial(args[1].toUpperCase()));
                    } catch (Exception e) {
                        toBuy = null;
                    }

                    if (toBuy == null) {
                        try {
                            toBuy = new ItemStack(Material.getMaterial(Integer.parseInt(args[1])));
                        } catch (Exception ee) {
                            toBuy = null;
                        }
                    }

                    if (toBuy == null) {
                        player.sendMessage(ChatColor.RED + "Error: The item " + args[1].toUpperCase() + " was not found.");
                        player.sendMessage(ChatColor.RED + "You can enter an item name OR an item id.");
                        return true;
                    }

                    try {
                        toBuy.setDurability((short) data);
                    } catch (Exception e) {
                        toBuy.setDurability((short) 0);
                    }

                    if (args.length > 2) {
                        autoConfirm = Boolean.parseBoolean(args[2]);
                        expectedPrice = Double.parseDouble(args[3]);
                    }
                }
            } else {
                if (!MarketManager.playerChosenItem(player.getName())) {
                    player.sendMessage(ChatColor.RED + "Error: Please use /market, select an item, then use /buy <amount>");
                    return true;
                }
            }

            if (toBuy != null) {
                int amount = 0;

                if (args[0].equalsIgnoreCase("all")) {
                    amount = Market.getCollection(toBuy, true).getAmount();
                } else {
                    try {
                        amount = Integer.parseInt(args[0]);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Error: '" + args[0] + "' is not a valid amount.");
                        return true;
                    }
                }

                if (amount <= 0.0) {
                    player.sendMessage(ChatColor.RED + "Error: Amount must be positive.");
                    return true;
                }

                int amountInMarket = Market.getCollection(toBuy, true).getAmount();

                if (amountInMarket < amount) {
                    player.sendMessage(ChatColor.RED + "Error: You are trying to buy " + amount + " of " + toBuy.getType().name() + " but there are only " + amountInMarket + " for sale.");
                    return true;
                }

                double reqPrice = Market.getPrice(toBuy, amount);

                if (reqPrice > Economy.getAPI().getProfile(player.getName()).getBalance()) {
                    player.sendMessage(ChatColor.RED + "Error: " + reqPrice + " gold exceeds the " + Economy.getAPI().getProfile(player.getName()).getBalance() + " gold you have.");
                    return true;
                }

                if (!autoConfirm) {
                    MarketManager.setPending(player, new PendingOffer(amount, toBuy, reqPrice));
                    player.sendMessage(Settings.PREFIX + "It will cost " + ChatColor.AQUA + reqPrice + " gold " + ChatColor.GREEN + "to buy " + ChatColor.AQUA + amount + " " + toBuy.getType().name() + "(S)");
                    player.sendMessage(Settings.PREFIX + ChatColor.YELLOW + " \u27A3 Use " + ChatColor.GOLD + "/confirm" + ChatColor.YELLOW + " to complete your purchase.");
                } else {
                    MarketManager.setPending(player, new PendingOffer(amount, toBuy, expectedPrice));
                    player.performCommand("confirm");
                }
                return true;
            }
        } else {

            player.sendMessage(ChatColor.RED + "You can replace <item> with 'this' to use what's in your hand.");
            player.sendMessage(ChatColor.RED + "You can select an item with /market instead of entering an item.");
            player.sendMessage(ChatColor.RED + "You can put 'all' instead of an amount.");
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "This is used to buy items from the market.");
            player.sendMessage(ChatColor.RED + "/buy <amount> <item>");
        }

        return true;
    }
}
