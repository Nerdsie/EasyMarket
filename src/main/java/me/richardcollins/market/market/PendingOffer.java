package me.richardcollins.market.market;

import me.richardcollins.market.Helper;
import me.richardcollins.market.Settings;
import me.richardcollins.market.managers.MarketManager;
import me.richardcollins.tools.custom.sound.SoundTools;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PendingOffer {
    private ItemStack itemStack;
    private double price;
    private int amount;
    private boolean active = true;

    public PendingOffer(int a, ItemStack m, double p) {
        setItemStack(m);
        setAmount(a);
        setPrice(p);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public double getPrice() {
        return price;
    }

    public double getUpdatedPrice() {
        return Market.getPrice(getItemStack(), getAmount());
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void executeOffer(Player player) {
        setPrice(getUpdatedPrice());
        Market.executeTransaction(player, getItemStack(), getAmount());
        MarketManager.removePending(player.getName());

        player.sendMessage(Settings.PREFIX + "You have bought " + ChatColor.RED + amount + " " + Helper.getName(getItemStack()) + "(S)" + ChatColor.GREEN + " for " + ChatColor.RED + getPrice() + " gold.");

        SoundTools.play(player, Sound.SHEEP_SHEAR, 1F, 1F);

        setActive(false);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
