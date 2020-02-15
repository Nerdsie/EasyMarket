package me.richardcollins.market.market;

import me.richardcollins.market.Helper;
import me.richardcollins.tools.custom.sound.SoundTools;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class Market {
    public static ArrayList<Offer> offers = new ArrayList<Offer>();

    public static ArrayList<OfferCollection> cachedCollections = new ArrayList<OfferCollection>();

    public static void resetCache() {
        cachedCollections = null;
    }

    public static void removeOffer(Offer o) {
        if (offers.contains(o)) {
            offers.remove(o);
        }

        me.richardcollins.market.Market.getDataManager().removeOffer(o.getID());

        resetCache();
    }

    public static void addOffer(Offer o, boolean addToDatabase) {
        if (o.getPricePer() <= 0.0) {
            return;
        }

        offers.add(o);

        if (addToDatabase) {
            me.richardcollins.market.Market.getDataManager().addOffer(o);
        }

        resetCache();
    }

    public static void addOffer(Player p, Offer o) {
        if (o.getPricePer() <= 0.0) {
            return;
        }

        Helper.removeItemsFromInventory(p, o.getItemStack(), true, o.getAmount());
        offers.add(o);

        me.richardcollins.market.Market.getDataManager().addOffer(o);

        resetCache();
    }

    //Returns a different offercollection for each data value.
    public static ArrayList<OfferCollection> getDifferentOffers(int id) {
        ArrayList<Integer> values = new ArrayList<Integer>();

        boolean running = true;

        OfferCollection collection = getCollection(new ItemStack(id), false);

        while (running) {
            int lowest = -1;
            Offer offer = null;

            for (int i = 0; i < collection.getOffers().size(); i++) {
                int data = (int) collection.getOffers().get(i).getItemStack().getDurability();

                if (lowest == -1 || data < lowest) {
                    if (!values.contains(data)) {
                        lowest = data;
                        offer = collection.getOffers().get(i);
                    }
                }
            }

            if (lowest == -1) {
                running = false;
            } else {
                values.add(lowest);
                collection.getOffers().remove(offer);
            }
        }

        ArrayList<OfferCollection> collections = new ArrayList<OfferCollection>();

        for (int data : values) {
            ItemStack toFind = new ItemStack(id);

            toFind.setDurability((short) data);

            if (getCollection(toFind, true).getAmount() > 0) {
                collections.add(getCollection(toFind, true));
            }
        }

        return collections;
    }

    //Returns all offers matching an itemstack (and data if boolean is true)
    public static OfferCollection getCollection(ItemStack itemStack, boolean reqDataMatch) {
        int id = itemStack.getTypeId();
        int data = itemStack.getDurability();

        OfferCollection toGet = new OfferCollection();

        for (Offer o : offers) {
            if (!reqDataMatch) {
                toGet.addOffer(o);
            } else {
                if (o.getItemStack().getTypeId() == id) {
                    if (o.getItemStack().getDurability() == (short) data) {
                        toGet.addOffer(o);
                    }
                }
            }
        }

        return toGet;
    }

    public static ArrayList<Offer> getOrderedCollection(ItemStack item, int amount) {
        ArrayList<Offer> toRet = new ArrayList<Offer>();

        boolean running = true;
        int amountLeft = amount;

        OfferCollection collection = getCollection(item, true);

        while (running) {
            Offer offer = null;

            for (int i = 0; i < collection.getOffers().size(); i++) {
                double pricePer = (int) collection.getOffers().get(i).getPricePer();

                Offer current = collection.getOffers().get(i);

                if (offer == null || pricePer < offer.getPricePer()) {
                    if (!toRet.contains(current)) {
                        offer = current;
                    }
                }
            }

            if (offer == null) {
                running = false;
            } else {
                toRet.add(offer);
                collection.getOffers().remove(offer);

                amountLeft -= offer.getAmount();
            }

            if (amountLeft <= 0) {
                running = false;
            }
        }

        return toRet;
    }

    public static ArrayList<OfferCollection> getAllCollections() {
        if (cachedCollections != null && !cachedCollections.isEmpty()) {
            return cachedCollections;
        }

        ArrayList<OfferCollection> toRet = new ArrayList<OfferCollection>();

        for (int id : getIDOfMarketItems()) {
            for (OfferCollection oC : getDifferentOffers(id)) {
                if (oC.getAmount() > 0) {
                    toRet.add(oC);
                }
            }
        }

        cachedCollections = toRet;

        return toRet;
    }

    public static double getPrice(ItemStack itemStack, int amount) {
        ArrayList<Offer> collection = getOrderedCollection(itemStack, amount);

        int amountLeft = amount;
        double total = 0;

        for (Offer o : collection) {
            if (amountLeft >= o.getAmount()) {
                total += (o.getAmount() * o.getPricePer());
            } else {
                total += o.getPricePer() * amountLeft;
            }

            amountLeft -= o.getAmount();
        }

        return Helper.roundOff(total);
    }

    public static void executeTransaction(Player buyer, ItemStack itemStack, int amount) {
        ArrayList<Offer> collection = getOrderedCollection(itemStack, amount);

        ArrayList<Player> past = new ArrayList<Player>();

        for (Offer o : collection) {
            Player target = Bukkit.getPlayerExact(o.getSeller());

            if (target != null && target.isOnline()) {
                if (!past.contains(target)) {
                    past.add(target);
                    SoundTools.play(target, Sound.SHEEP_SHEAR, 1F, 1F);
                }
            }

            amount = o.executeTransaction(buyer, amount);
        }

        if (amount <= 0) {
            cachedCollections = null;
            return;
        }

        cachedCollections = null;
    }

    public static ArrayList<Integer> getIDOfMarketItems() {
        ArrayList<Integer> ids = new ArrayList<Integer>();

        for (Material m : Material.values()) {
            int amountInMarket = me.richardcollins.market.market.Market.getCollection(new ItemStack(m), false).getAmount();

            if (amountInMarket > 0) {
                ids.add(m.getId());
            }
        }

        return ids;
    }

    public static int amountInMarket(ItemStack i, boolean matchData) {
        OfferCollection collection = me.richardcollins.market.market.Market.getCollection(i.clone(), true);

        //Verify it has items.
        if (collection.getOffers().size() > 0) {

            //How many of this specific item are on the market?
            int amountInMarket = collection.getAmount();

            return amountInMarket;
        }

        return 0;
    }
}