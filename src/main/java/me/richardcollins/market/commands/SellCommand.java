package me.richardcollins.market.commands;

import me.richardcollins.market.Helper;
import me.richardcollins.market.Settings;
import me.richardcollins.market.events.MarketSellEvent;
import me.richardcollins.market.managers.MarketManager;
import me.richardcollins.market.market.Market;
import me.richardcollins.market.market.Offer;
import me.richardcollins.tools.custom.sound.SoundTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Error: You must be a player to do this.");
            return true;
        }

        Player player = (Player) sender;
        String neededPerms = "mymarket.command.sell";

        if (!sender.hasPermission(neededPerms)) {
            sender.sendMessage(ChatColor.RED + "Error: You need the '" + neededPerms + "' to do this.");
            return true;
        }

        if (args.length >= 2) {
            ItemStack toSell = null;

            int amount = 0;
            double price = 0;

            int priceIndex = 2;

            if (MarketManager.playerChosenItem(player.getName())) {
                toSell = MarketManager.getChosenItemStack(player.getName());
            }

            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("this") || args[1].equalsIgnoreCase("hand") || args[1].equalsIgnoreCase("inhand")) {
                    toSell = player.getItemInHand();

                    if (toSell == null || toSell.getType() == Material.AIR) {
                        player.sendMessage(ChatColor.RED + "Error: You don't have an item in your");
                    }
                } else {
                    int data = 0;

                    if (args[1].contains(":")) {
                        String[] split = args[1].split(":");

                        try {
                            toSell = new ItemStack(Material.getMaterial(Integer.parseInt(split[0])));
                        } catch (Exception e) {
                            try {
                                toSell = new ItemStack(Material.getMaterial(split[0]));
                            } catch (Exception ex) {

                            }
                        }

                        if (toSell.getType().getMaxDurability() <= 0) {
                            try {
                                data = Integer.parseInt(split[1]);
                            } catch (Exception e) {

                            }
                        }

                        args[1] = args[1].split(":")[0];
                    }

                    try {
                        toSell = new ItemStack(Material.getMaterial(args[1].toUpperCase()));
                    } catch (Exception e) {
                        toSell = null;
                    }

                    if (toSell == null) {
                        try {
                            toSell = new ItemStack(Material.getMaterial(Integer.parseInt(args[1])));
                        } catch (Exception ee) {
                            toSell = null;
                        }
                    }

                    if (toSell == null) {
                        player.sendMessage(ChatColor.RED + "Error: The item " + args[1].toUpperCase() + " was not found.");
                        player.sendMessage(ChatColor.RED + "You can enter an item name OR an item id.");
                        return true;
                    }

                    try {
                        toSell.setDurability((short) data);
                    } catch (Exception e) {
                        toSell.setDurability((short) 0);
                    }
                }
            } else {
                if (!MarketManager.playerChosenItem(player.getName())) {
                    player.sendMessage(ChatColor.RED + "Error: Please use /market, select an item, then use /sell <amount>");
                    return true;
                }

                priceIndex = 1;
            }

            if (toSell.getType() == Material.ENCHANTED_BOOK) {
                player.sendMessage(ChatColor.RED + "Error: You cannot sell enchanted books (yet).");
                return true;
            }

            if (args[0].equalsIgnoreCase("all")) {
                amount = Helper.countItemsInInventory(player, toSell, true);
            } else {
                try {
                    amount = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Error: '" + args[0] + "' is not a valid amount.");
                    return true;
                }
            }

            boolean per = false;

            if (args[priceIndex].endsWith("p")) {
                per = true;
                args[priceIndex] = args[priceIndex].substring(0, (args[priceIndex]).length() - 1);
            }

            try {
                price = Double.parseDouble(args[priceIndex]);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error: '" + args[priceIndex] + "' is not a valid price.");
                return true;
            }

            if (!per) {
                price = price / amount;
            }

            price = Helper.roundOff(price);

            if (price <= 0.0) {
                player.sendMessage(ChatColor.RED + "Error: Price must be positive.");
                return true;
            }

            if (amount <= 0.0) {
                player.sendMessage(ChatColor.RED + "Error: Amount must be positive.");
                return true;
            }

            if (Helper.countItemsInInventory(player, toSell, true) < amount) {
                player.sendMessage(ChatColor.RED + "Error: You cannot sell " + amount + " of " + toSell.getType().name() + " when you only have " + Helper.countItemsInInventory(player, toSell, true) + ".");
                return true;
            }

            Offer toAdd = new Offer(Market.offers.size() + 1, player.getName(), toSell, amount, price);

            if (toAdd.getPricePer() <= 0.0) {
                player.sendMessage(ChatColor.RED + "Error: Price to low for amount of items provided.");
                return true;
            }

            if (toAdd.getPricePer() > 1000000) {
                player.sendMessage(ChatColor.RED + "Error: Maximum price-per-item is 1 million.");
                return true;
            }


            MarketSellEvent event = new MarketSellEvent(player.getName(), amount, toAdd.getPricePer(), toAdd.getItemStack());
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                Market.addOffer(player, toAdd);

                SoundTools.play(player, Sound.BAT_TAKEOFF, 0.5F, 1F);

                player.sendMessage(Settings.PREFIX + ChatColor.RED + "" + amount + " " + Helper.getName(toSell) + "(S)" + ChatColor.GREEN + " selling for " + ChatColor.RED + toAdd.getPricePer() + " gold" +
                        ChatColor.GREEN + " each.");

                player.sendMessage(Settings.PREFIX + ChatColor.GREEN + "   Selling for " + ChatColor.RED + Helper.roundOff(toAdd.getPricePer() * toAdd.getAmount()) + " gold " + ChatColor.GREEN + "total.");
            }

            return true;
        }

        player.sendMessage(ChatColor.RED + "You can replace <item> with 'this' to use what's in your hand.");
        player.sendMessage(ChatColor.RED + "You can select an item with /market instead of entering an item.");
        player.sendMessage(ChatColor.RED + "You can put 'p' after the price to make it per-item instead of total.");
        player.sendMessage(ChatColor.RED + "You can put 'all' instead of an amount.");
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "You can use this to sell your items.");
        player.sendMessage(ChatColor.RED + "/sell <amount> <item> <price>");

        return true;
    }
}
