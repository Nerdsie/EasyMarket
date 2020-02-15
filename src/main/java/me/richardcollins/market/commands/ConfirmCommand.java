package me.richardcollins.market.commands;

import me.richardcollins.market.events.MarketBuyEvent;
import me.richardcollins.market.managers.MarketManager;
import me.richardcollins.market.market.PendingOffer;
import me.richardcollins.market.Helper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfirmCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Error: You must be a player to do this.");
            return true;
        }

        Player player = (Player) sender;
        String neededPerms = "mymarket.command.confirm";

        if (!sender.hasPermission(neededPerms)) {
            sender.sendMessage(ChatColor.RED + "Error: You need the '" + neededPerms + "' to do this.");
            return true;
        }

        if (MarketManager.playerHasPending(player.getName())) {
            PendingOffer offer = MarketManager.getPending(player.getName());

            if (!offer.isActive()) {
                player.sendMessage(ChatColor.RED + "You have no pending transactions to confirm.");
                return true;
            }

            if (offer.getPrice() >= offer.getUpdatedPrice()) {
                offer.setPrice(offer.getUpdatedPrice());

                int available = Helper.countAvailableSpace(player, offer.getItemStack());

                MarketBuyEvent event = new MarketBuyEvent(player.getName(), offer.getAmount(), offer.getPrice(), offer.getItemStack());
                Bukkit.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    offer.executeOffer(player);
                }

                return true;
            } else {
                player.sendMessage(ChatColor.RED + "The price of this transaction has increased from " + offer.getPrice() + " to " + offer.getUpdatedPrice() + ".");
                player.sendMessage(ChatColor.RED + "Please use /confirm if this updated price is okay with you.");

                offer.setPrice(offer.getUpdatedPrice());

                return true;
            }
        } else {
            player.sendMessage(ChatColor.RED + "You have no pending transactions to confirm.");
            return true;
        }
    }
}
