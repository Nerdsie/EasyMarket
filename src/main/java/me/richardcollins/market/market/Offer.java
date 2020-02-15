package me.richardcollins.market.market;

import me.richardcollins.economy.Economy;
import me.richardcollins.market.Helper;
import me.richardcollins.market.Market;
import me.richardcollins.market.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Offer {
    private int ID = 0;
    private ItemStack itemStack;
    private int amount = 0;
    private double pricePer = 0.0;
    private String seller = "";

    public Offer(int id, String s, ItemStack i, int a, double p) {
        setID(id);
        setItemStack(i);
        setAmount(a);
        setPricePer(p);
        setSeller(s);

        if (getPricePer() <= 0.0) {
            setPricePer(0.01);
        }
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public ItemStack getItemStack() {
        itemStack.setAmount(1);

        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack.clone();

        this.itemStack.setAmount(1);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPricePer() {
        return pricePer;
    }

    public void setPricePer(double pricePer) {
        this.pricePer = pricePer;
    }

    public void removeAmount(int am) {
        amount -= am;
    }

    public String getEnchants() {
        StringBuilder sb = new StringBuilder();

        boolean ran = false;
        for (Enchantment e : getItemStack().getEnchantments().keySet()) {
            if (ran) {
                sb.append(",,");
            } else {
                ran = true;
            }

            sb.append(e.getId() + "," + getItemStack().getEnchantments().get(e));
        }

        return sb.toString();
    }

    public int executeTransaction(Player buyer, int amount) {
        int needToBuy = amount;
        int amountToBuy = amount;

        if (amountToBuy > getAmount()) {
            amountToBuy = getAmount();
        }

        double totalPrice = Helper.roundOff(amountToBuy * getPricePer());

        Economy.getAPI().getProfile(buyer).transfer(getSeller(), totalPrice);

        removeAmount(amountToBuy);

        if (getAmount() <= 0) {
            me.richardcollins.market.market.Market.removeOffer(this);
        }

        Market.getDataManager().updateOfferAmount(this);

        Helper.giveItems(buyer, getItemStack(), amountToBuy);

        Player sellerP = Bukkit.getPlayerExact(seller);

        if (sellerP != null && sellerP.isOnline()) {
            sellerP.sendMessage(Settings.PREFIX + ChatColor.RED + "" + amountToBuy + " " + Helper.getName(getItemStack()) + "(S) " + ChatColor.GREEN + "sold for " + ChatColor.RED + totalPrice + " gold.");
        }

        return needToBuy - amountToBuy;
    }

    public boolean isMatch(ItemStack i, boolean reqDataMatch) {
        if (i.getType() == getItemStack().getType()) {
            if (!reqDataMatch) {
                return true;
            } else {
                if (getItemStack().getType().getMaxDurability() <= 0) {
                    if (i.getDurability() == getItemStack().getDurability()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        return false;
    }
}
