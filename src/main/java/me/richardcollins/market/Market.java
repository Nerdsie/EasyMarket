package me.richardcollins.market;

import me.richardcollins.market.commands.*;
import me.richardcollins.market.market.Offer;
import me.richardcollins.market.storage.DataManager;
import me.richardcollins.market.storage.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Market extends JavaPlugin {
    public static DataManager dataManager = null;
    public static Market plugin;

    public void onEnable() {
        plugin = this;

        getServer().getPluginManager().registerEvents(new MMListener(this), this);

        Settings.load(this);

        dataManager = new DataManager(new MySQL());

        try {
            load();
        } catch (Exception e) {

        }

        getCommand("buy").setExecutor(new BuyCommand());
        getCommand("deposit").setExecutor(new DepositCommand());
        getCommand("price").setExecutor(new PriceCommand());
        getCommand("sell").setExecutor(new SellCommand());
        getCommand("selling").setExecutor(new SellingCommand());
        getCommand("withdraw").setExecutor(new WithdrawCommand());
        getCommand("market").setExecutor(new MarketCommand());
        getCommand("confirm").setExecutor(new ConfirmCommand());
        getCommand("cancel").setExecutor(new CancelCommand());
    }

    public void onDisable() {
        dataManager.core.close();
    }

    public static DataManager getDataManager() {
        return dataManager;
    }

    public static MySQL getCore() {
        return getDataManager().core;
    }

    public void load() throws SQLException {
        ResultSet set = getDataManager().getAllOffers();

        while (set.next()) {
            if (set.getBoolean("active")) {
                int id = set.getInt("id");
                String seller = set.getString("seller");
                int amount = set.getInt("amount");

                if (amount > 0) {
                    int data = set.getInt("item-data");
                    int itemid = set.getInt("item-id");
                    double price = set.getDouble("price-per");

                    //String enchants = set.getString("enchants");

                    ItemStack stack = new ItemStack(itemid);

                    if (stack.getType().getMaxDurability() <= 0) {
                        if (data != 0) {
                            stack.setDurability((short) data);
                        }
                    }

                    Offer offer = new Offer(id, seller, stack, amount, price);
                    me.richardcollins.market.market.Market.addOffer(offer, false);
                }
            }
        }
    }

    public static Market getPlugin() {
        return (Market) plugin;
    }
}
