package me.richardcollins.market;

import me.richardcollins.market.managers.MarketManager;
import me.richardcollins.market.market.Market;
import me.richardcollins.market.market.OfferCollection;
import me.richardcollins.tools.custom.sound.SoundTools;
import me.richardcollins.tools.events.icons.IconClickEvent;
import me.richardcollins.tools.events.item.ItemClickEvent;
import me.richardcollins.tools.events.menus.EmptyClickEvent;
import me.richardcollins.tools.handlers.IconHandler;
import me.richardcollins.tools.handlers.MenuHandler;
import me.richardcollins.tools.objects.elements.Icon;
import me.richardcollins.tools.objects.elements.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Map;

public class Helper extends me.richardcollins.universal.Helper {
    static int itemsPerPage = 44;

    public static int getMarketPages() {
        int pages = Market.getAllCollections().size() / itemsPerPage;

        if (Market.getAllCollections().size() % itemsPerPage != 0) {
            return pages + 1;
        } else {
            return pages;
        }
    }

    public static int countAvailableSpace(Player player, ItemStack i) {
        int count = 0;

        for (ItemStack item : player.getInventory()) {
            //Is slot empty?
            if (item == null || item.getType() == Material.AIR) {
                //Add a possible stack to the count.
                count += i.getMaxStackSize();
            } else {

                //ItemStack matches?
                if (item.getType() == i.getType() && i.getDurability() == i.getDurability()) {
                    //Add the remaining space in the stack to the count.
                    count += i.getMaxStackSize() - item.getAmount();
                }
            }
        }

        return count;
    }

    public static double roundOff(double d) {
        return Math.round(100 * d) / ((double) 100);
    }

    public static int countItemsInInventory(Player p, ItemStack m, boolean reqData) {
        int amount = 0;

        for (Map.Entry<Integer, ? extends ItemStack> entry : p.getInventory().all(m.getType()).entrySet()) {
            ItemStack i = entry.getValue();

            //Is item not null?
            if (i != null) {
                //Is type matching?
                if (i.getType() == m.getType()) {
                    //Is un-enchanted?
                    if (i.getEnchantments().isEmpty()) {
                        //Is unused or...
                        if ((i.getDurability() == 0 && i.getType().getMaxDurability() > 0)
                                //Isn't tool but data doesn't matter or...
                                || i.getType().getMaxDurability() <= 0 && !reqData
                                //Isn't tool and data matches.
                                || (i.getDurability() == m.getDurability() && i.getType().getMaxDurability() <= 0 && reqData)) {

                            //Add amount in stack to count.
                            amount += i.getAmount();
                        }
                    }
                }
            }
        }

        return amount;
    }

    public static void removeItemsFromInventory(Player p, ItemStack m, boolean reqData, int amount) {
        int toRemove = amount;

        for (int count = 0; count < p.getInventory().getContents().length; count++) {
            ItemStack i = p.getInventory().getContents()[count];

            if (i != null) {
                int size = i.getAmount();

                // Anything left to remove?
                if (toRemove > 0) {

                    //Is this what we want to remove?
                    if (i.getType() == m.getType()) {

                        //Is it non-enchanted?
                        if (i.getEnchantments().isEmpty()) {


                            //Is it unused or...
                            if ((i.getDurability() == 0 && i.getType().getMaxDurability() > 0)
                                    //Is it not a tool, and matching the data isn't required or...
                                    || i.getType().getMaxDurability() <= 0 && !reqData
                                    //Is it not a tool, and the data's match.
                                    || (i.getDurability() == m.getDurability() && i.getType().getMaxDurability() <= 0 && reqData)) {

                                //If there's more to remove than there is in the stack.
                                if (toRemove >= i.getAmount()) {

                                    //We've now removed i.getAmount(), leaving toRemove remaining.
                                    toRemove -= i.getAmount();

                                    //Delete the stack, it's all been removed.
                                    p.getInventory().setItem(count, null);

                                    //If we don't need to remove anything, stop looping.
                                    if (toRemove <= 0) {
                                        return;
                                    }
                                } else {

                                    //Remove what we need to from the stack, then stop looping.
                                    ItemStack newItemstack = i.clone();
                                    newItemstack.setAmount(i.getAmount() - toRemove);

                                    p.getInventory().setItem(count, newItemstack);

                                    return;
                                }
                            }
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }

    public static void openMarket(Player player, int page) {
        getMarketMenu(player, page).open(player);
    }

    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("MyMarket");
    }

    public static Menu getMarketMenu(Player player, int page) {
        //If you're too far, set to first page (loop back)
        if (page >= Helper.getMarketPages()) {
            page = 0;
        }
        //Same the other way.
        if (page < 0) {
            page = Helper.getMarketPages() - 1;
        }

        //Inventory position.
        int pos = 0;
        //Block ID of first market item.
        int id = getFirstMarketID(page);
        //Minimum arraylist id (How many items would be on previous pages)
        int minID = itemsPerPage * page;
        //How many positions have we gone through?
        int count = minID;
        //How many positions can we go through?
        int maxID = minID + (itemsPerPage - 1);

        Menu menu = new Menu(player.getName(), "Market || Page " + (page + 1) + "/" + Helper.getMarketPages(), 54).setHandler(new MenuHandler() {
            @Override
            public void onItemClick(ItemClickEvent e) {
                super.onItemClick(e);

                e.setNewMenu(getSellingMenu(e.getPlayer(), e.getItem(), e.getMenu()));
            }
        });
        menu.setMeta("page", page + "");

        ArrayList<OfferCollection> collections = Market.getAllCollections();

        //If we haven't gone through as many items as we need for the page
        while (count <= maxID) {

            try {
                //Get the offer.
                OfferCollection collection = collections.get(count);

                //Verify it has items.
                if (collection.getOffers().size() > 0) {

                    //How many of this specific item are on the market?
                    final int amountInMarket = collection.getAmount();

                    if (amountInMarket > 0) {
                        //Get item for display in market.
                        ItemStack toShow = collection.getOffers().get(0).getItemStack();

                        //Set the title of the item.
                        String title = ChatColor.DARK_GREEN + "\u2727 " + ChatColor.DARK_AQUA + Helper.getName(collection.getItemStack()) + ChatColor.AQUA + " (" +
                                collection.getItemStack().getTypeId() + ":" + toShow.getDurability() + ")";

                        //Get the minimum price of the item.
                        double price = Helper.roundOff(Market.getPrice(toShow, 1));

                        //Keep the middle of the bottom 2 rows clean for navigation.
                        if (pos == 38 || pos == 47) {
                            pos += 5;
                        }

                        //Prepare the icon for the market.
                        Icon icon = new Icon(pos, title, collection.getOffers().get(0).getItemStack()).setHandler(new IconHandler() {
                            @Override
                            public void onClick(IconClickEvent event) {
                                super.onClick(event);

                                MarketManager.setChosenItemStack(event.getPlayerName(), event.getItem());

                                if (event.getOriginalEvent().getClick() == ClickType.RIGHT) {
                                    Menu detailedMenu = getSellingMenu(event.getPlayer(), event.getItem(), event.getMenu());

                                    event.setCloseInventory(false);
                                    event.setNewMenu(detailedMenu);

                                    return;
                                }

                                Menu detailedMenu = getBuyingMenu(event.getPlayer(), event.getItem(), event.getMenu());

                                event.setCloseInventory(false);
                                event.setNewMenu(detailedMenu);
                            }
                        });

                        //Set the lore to show important information.
                        icon.setMeta("amount", amountInMarket);

                        icon.addLore("");
                        icon.addLore(ChatColor.GRAY + "Amount in Market: " + ChatColor.YELLOW + amountInMarket);
                        icon.addLore(ChatColor.GRAY + "Minimum Price: " + ChatColor.GOLD + price + "g");

                        //Add the icon to the market.
                        menu.addIcon(icon);

                        //update which position you should fill next.
                        pos++;
                    }
                }

                //Update arraylist id to show you've already parsed latest item.
                count++;
            } catch (Exception e) {
                count++;
            }

            id++;
        }

        //Prepare the navigation icons.

        ItemStack stack = new ItemStack(Material.DOUBLE_PLANT);
        stack.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 42);

        Icon nextPage = new Icon(5, 5, ChatColor.DARK_AQUA + "\u2726" + ChatColor.DARK_GREEN + " Next Page", stack).setHandler(new IconHandler() {

            @Override
            public void onClick(IconClickEvent event) {
                super.onClick(event);

                int current = getIcon().getParent().getMetaInteger("page", 1);
                int newPage = current + 1;

                openMarket(event.getPlayer(), newPage);
            }
        });

        nextPage.addLore(ChatColor.GRAY + "   Go to the next page.");


        Icon prevPage = new Icon(3, 5, ChatColor.DARK_AQUA + "\u2726" + ChatColor.DARK_RED + " Previous Page", stack).setHandler(new IconHandler() {
            @Override
            public void onClick(IconClickEvent event) {
                super.onClick(event);

                int current = getIcon().getParent().getMetaInteger("page", 1);

                int newPage = current - 1;

                openMarket(event.getPlayer(), newPage);
            }
        });

        prevPage.addLore(ChatColor.GRAY + "   Go to the previous page.");

        //Add the navigation icons.
        menu.addIcon(nextPage);
        menu.addIcon(prevPage);

        return menu;
    }

    public static Menu getBuyingMenu(Player player, ItemStack itemStack, Menu old) {
        final int amountInMarket = Market.amountInMarket(itemStack, true);

        String title = ChatColor.DARK_GRAY + "Cart: " + Helper.getName(itemStack);
        final Menu detailedMenu = new Menu(player, title, 54).setHandler(new MenuHandler() {

            @Override
            public void onBlankClick(EmptyClickEvent e) {
                super.onBlankClick(e);

                if (e.getOriginalEvent().getSlotType() == InventoryType.SlotType.OUTSIDE) {
                    e.setCloseInventory(true);
                }
            }
        });

        detailedMenu.setMeta("item_purchasing", itemStack);

        if (old != null) {
            detailedMenu.setMeta("old_menu", old);
        }

        int[] values = new int[]{
                1, 2, 5, 8, 10, 16, 32, 48, 64
        };

        if (old != null) {
            Icon cIcon = new Icon(2, 2, ChatColor.GRAY + " \u21A9 Return to Market.", new ItemStack(Material.CHEST)).setHandler(new IconHandler() {
                @Override
                public void onClick(IconClickEvent event) {
                    super.onClick(event);

                    event.setNewMenu(Helper.getMarketMenu(event.getPlayer(), ((Menu) getIcon().getParent().getMeta("old_menu")).getMetaInteger("page", 1)));
                }
            });

            cIcon.addLore(" ");
            cIcon.addLore(ChatColor.DARK_GRAY + "Go back to the Market GUI.");
            detailedMenu.addIcon(cIcon);
        }

        ItemStack stack = new ItemStack(Material.EMERALD);
        stack.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 42);

        Icon cIcon = new Icon(6, 2, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "$" + ChatColor.RESET + "" + ChatColor.GREEN + " Click to Complete Purchase.", stack).setHandler(new IconHandler() {
            @Override
            public void onClick(IconClickEvent event) {
                super.onClick(event);

                ItemStack buying = (ItemStack) getIcon().getParent().getMeta("item_purchasing");
                int inCart = getIcon().getParent().getMetaInteger("cart", 1);
                double totalPrice = Market.getPrice((ItemStack) getIcon().getParent().getMeta("item_purchasing"), inCart);

                event.getPlayer().performCommand("buy " + inCart + " " + buying.getType() + ":" + buying.getDurability() + " true " + totalPrice);

                event.setNewMenu(Helper.getMarketMenu(event.getPlayer(), ((Menu) getIcon().getParent().getMeta("old_menu")).getMetaInteger("page", 1)));
            }

            @Override
            public void update() {
                super.update();

                getIcon().getLore().clear();

                int inCart = getIcon().getParent().getMetaInteger("cart", 1);
                double totalPrice = Market.getPrice((ItemStack) getIcon().getParent().getMeta("item_purchasing"), inCart);

                getIcon().addLore(ChatColor.GRAY + "     Amount in Cart: " + ChatColor.AQUA + inCart);
                getIcon().addLore(ChatColor.GRAY + "     Total Price: " + ChatColor.GOLD + totalPrice + " gold.");
                getIcon().addLore("");
                getIcon().addLore(ChatColor.GRAY + "     Amount In Market: " + ChatColor.WHITE + Market.amountInMarket((ItemStack) getIcon().getParent().getMeta("item_purchasing"), true));

                getIcon().getParent().updateIcon(getIcon());
            }
        });

        detailedMenu.addIcon(cIcon);

        cIcon.getParent().setMeta("cart", 1);
        cIcon.getHandler().update();

        for (int w = 0; w <= 1; w++) {
            for (int i = 0; i < 9; i++) {
                int x = i;
                int y = w;

                String name = "";

                if (w == 0) {
                    name = ChatColor.AQUA + "\u2714 " + ChatColor.GREEN + "Add " + ChatColor.DARK_GREEN + values[i] + ChatColor.GREEN + " to cart.";
                } else {
                    name = ChatColor.GOLD + "\u2716 " + ChatColor.RED + "Remove " + ChatColor.DARK_RED + values[i] + ChatColor.RED + " from cart.";
                }

                Icon icon = new Icon(x, y, name, new ItemStack(Material.WOOL, values[i], (w == 0) ? (short) 5 : (short) 14)).setHandler(new IconHandler() {
                    @Override
                    public void onClick(IconClickEvent event) {
                        super.onClick(event);


                        if (getIcon().getMetaBoolean("on", true)) {
                            if (getIcon().getMetaBoolean("give", true)) {
                                getIcon().getParent().setMeta("cart", getIcon().getParent().getMetaInteger("cart", 1) + event.getItem().getAmount());
                            } else {
                                getIcon().getParent().setMeta("cart", getIcon().getParent().getMetaInteger("cart", 1) - event.getItem().getAmount());
                            }
                        }

                        event.getMenu().getHandler().update();
                        event.setCloseInventory(false);
                    }

                    @Override
                    public void update() {
                        super.update();

                        int inCart = getIcon().getParent().getMetaInteger("cart", 1);

                        if (getIcon().getMetaBoolean("give", true)) {
                            int check = inCart + getIcon().getAmount();
                            int inMarket = Market.amountInMarket((ItemStack) getIcon().getParent().getMeta("item_purchasing"), true);

                            if (check > inMarket) {
                                if (getIcon().getMetaBoolean("on", true)) {
                                    getIcon().setItemStack(new ItemStack(Material.WOOL, getIcon().getAmount(), (short) 8));
                                    getIcon().setMeta("on", false);

                                    getIcon().getLore().clear();
                                    getIcon().addLore(ChatColor.GRAY + "  Disabled: Would exceed items in market.");
                                }
                            } else {
                                if (!getIcon().getMetaBoolean("on", true)) {
                                    getIcon().setItemStack(new ItemStack(Material.WOOL, getIcon().getAmount(), (short) 5));
                                    getIcon().setMeta("on", true);


                                    getIcon().getLore().clear();
                                    getIcon().setName(ChatColor.AQUA + "\u2714 " + ChatColor.GREEN + "Add " + ChatColor.DARK_GREEN + getIcon().getAmount() + ChatColor.GREEN + " to cart.");
                                }
                            }
                        } else {
                            int check = inCart - getIcon().getAmount();

                            if (check <= 0) {
                                if (getIcon().getMetaBoolean("on", true)) {
                                    getIcon().setItemStack(new ItemStack(Material.WOOL, getIcon().getAmount(), (short) 7));
                                    getIcon().setMeta("on", false);

                                    getIcon().getLore().clear();
                                    getIcon().addLore(ChatColor.GRAY + "  Disabled: Cannot purchase less than one item.");
                                }
                            } else {
                                if (!getIcon().getMetaBoolean("on", true)) {
                                    getIcon().setItemStack(new ItemStack(Material.WOOL, getIcon().getAmount(), (short) 14));
                                    getIcon().setMeta("on", true);

                                    getIcon().getLore().clear();
                                    getIcon().setName(ChatColor.GOLD + "\u2716 " + ChatColor.RED + "Remove " + ChatColor.DARK_RED + getIcon().getAmount() + ChatColor.RED + " from cart.");
                                }
                            }
                        }


                        getIcon().getParent().updateIcon(getIcon());
                    }
                });

                icon.setMeta("on", true);
                icon.setMeta("give", (w == 0) ? true : false);
                detailedMenu.addIcon(icon);
            }
        }
        for (int i = 8; i >= 0; i--) {
            for (int ii = 5; ii >= 4; ii--) {
                int x = i;
                int y = ii;

                if (x > 0 || y == 5) {
                    Icon icon = new Icon(x, y, "", ((ItemStack) detailedMenu.getMeta("item_purchasing")).clone()).setHandler(new IconHandler() {
                        @Override
                        public void onClick(IconClickEvent event) {
                            super.onClick(event);

                            event.setCloseInventory(false);
                        }

                        @Override
                        public void update() {
                            super.update();

                            int inCart = getIcon().getParent().getMetaInteger("cart", 1);
                            double totalPrice = Market.getPrice((ItemStack) getIcon().getParent().getMeta("item_purchasing"), inCart);
                            int toShow = getIcon().getParent().getMetaInteger("cart", 1);

                            OfferCollection collection = Market.getCollection(getIcon().getItemStack().clone(), true);

                            //Verify it has items.
                            if (collection.getOffers().size() > 0) {

                                //How many of this specific item are on the market?
                                int amountInMarket = collection.getAmount();
                            }

                            toShow = Math.min(toShow, amountInMarket);
                            toShow -= ((8 - getIcon().getLocation().getX() + ((5 - getIcon().getLocation().getY()) * 9)) * Settings.STACK_SIZE);

                            if (toShow > 0) {
                                int amount = Math.min(toShow, Settings.STACK_SIZE);
                                getIcon().setAmount(amount);

                                if (!getIcon().isVisible()) {
                                    getIcon().setVisible(true);
                                }

                                getIcon().getLore().clear();

                                getIcon().addLore("");
                                getIcon().addLore(ChatColor.WHITE + "  Amount in Cart: " + ChatColor.GRAY + inCart);
                                getIcon().addLore(ChatColor.WHITE + "  Total Price: " + ChatColor.GRAY + totalPrice + " gold.");
                                getIcon().addLore("");
                                getIcon().addLore(ChatColor.WHITE + "  Amount In Market: " + ChatColor.GRAY + Market.amountInMarket((ItemStack) getIcon().getParent().getMeta("item_purchasing"), true));
                            } else {
                                getIcon().setVisible(false);
                            }

                            getIcon().getParent().updateIcon(getIcon());
                        }
                    });

                    detailedMenu.addIcon(icon);
                    detailedMenu.getHandler().update();
                    icon.getHandler().update();
                }
            }
        }

        Icon icon = new Icon(0, 4, "", new ItemStack(Material.BOOK)).setHandler(new IconHandler() {
            @Override
            public void onClick(IconClickEvent event) {
                super.onClick(event);

                event.setCloseInventory(false);
            }

            @Override
            public void update() {
                super.update();

                int toShow = getIcon().getParent().getMetaInteger("cart", 1);

                OfferCollection collection = Market.getCollection(getIcon().getItemStack().clone(), true);

                //Verify it has items.
                if (collection.getOffers().size() > 0) {

                    //How many of this specific item are on the market?
                    int amountInMarket = collection.getAmount();
                }

                toShow = Math.min(toShow, amountInMarket);
                toShow -= ((8 - getIcon().getLocation().getX() + ((5 - getIcon().getLocation().getY()) * 8)) * Settings.STACK_SIZE);

                if (toShow > 0) {
                    getIcon().setVisible(true);
                    getIcon().setAmount(1);
                    getIcon().getLore().clear();
                    getIcon().setName(ChatColor.GRAY + "" + ChatColor.BOLD + "* EXTRA ITEMS *");

                    int inCart = getIcon().getParent().getMetaInteger("cart", 1);
                    double totalPrice = Market.getPrice((ItemStack) getIcon().getParent().getMeta("item_purchasing"), inCart);

                    getIcon().addLore("  " + ChatColor.RED + " You are purchasing more items");
                    getIcon().addLore("  " + ChatColor.RED + "   than can be displayed.");
                    getIcon().addLore("");
                    getIcon().addLore(ChatColor.WHITE + "  Amount in Cart: " + ChatColor.GRAY + inCart);
                    getIcon().addLore(ChatColor.WHITE + "  Total Price: " + ChatColor.GRAY + totalPrice + " gold.");
                    getIcon().addLore("");
                    getIcon().addLore(ChatColor.WHITE + "  Amount In Market: " + ChatColor.GRAY + Market.amountInMarket((ItemStack) getIcon().getParent().getMeta("item_purchasing"), true));
                } else {
                    getIcon().setVisible(false);
                }

                getIcon().getParent().updateIcon(getIcon());
            }
        });

        detailedMenu.addIcon(icon);
        icon.getHandler().update();
        detailedMenu.getHandler().update();

        return detailedMenu;
    }

    public static Menu getSellingMenu(Player player, ItemStack itemStack, Menu old) {
        final int amountInMarket = Market.amountInMarket(itemStack, true);

        String title = ChatColor.DARK_GRAY + "Selling: " + Helper.getName(itemStack);
        final Menu detailedMenu = new Menu(player, title, 54);

        detailedMenu.setMeta("price", 0.01);

        detailedMenu.setMeta("item", itemStack);

        if (old != null) {
            detailedMenu.setMeta("old_menu", old);
        }

        int[] values = new int[]{
                1, 5, 10, 16, 32, 48, 64
        };

        int[] nugs = new int[]{
                1, 5, 10, 25, 50
        };

        int[] ings = new int[]{
                1, 5, 10, 20, 50
        };

        if (old != null) {

            Icon cIcon = new Icon(6, 0, ChatColor.GRAY + " \u21A9 Return to Market.", new ItemStack(Material.CHEST)).setHandler(new IconHandler() {
                @Override
                public void onClick(IconClickEvent event) {
                    super.onClick(event);

                    event.setNewMenu(Helper.getMarketMenu(event.getPlayer(), ((Menu) getIcon().getParent().getMeta("old_menu")).getMetaInteger("page", 1)));
                }
            });

            cIcon.addLore(" ");
            cIcon.addLore(ChatColor.DARK_GRAY + "Go back to the Market GUI.");
            detailedMenu.addIcon(cIcon);
        }

        ItemStack stack = new ItemStack(Material.EMERALD);
        stack.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 42);

        Icon cIcon = new Icon(7, 0, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "$" + ChatColor.RESET + "" + ChatColor.GREEN + " Click to Complete Sale.", stack).setHandler(new IconHandler() {
            @Override
            public void onClick(IconClickEvent event) {
                super.onClick(event);

                ItemStack buying = (ItemStack) getIcon().getParent().getMeta("item");
                int inCart = getIcon().getParent().getMetaInteger("cart", 1);
                double price = getIcon().getParent().getMetaDouble("price", 0.01);

                if (price <= 0) {
                    event.setCloseInventory(false);
                    event.getMenu().updateAllIcons();

                    SoundTools.play(event.getPlayer(), Sound.NOTE_BASS, 1F, 0F);

                    return;
                }

                event.getPlayer().performCommand("sell " + inCart + " " + buying.getType() + ":" + buying.getDurability() + " " + price + "p");

                event.setNewMenu(Helper.getMarketMenu(event.getPlayer(), ((Menu) getIcon().getParent().getMeta("old_menu")).getMetaInteger("page", 1)));
            }

            @Override
            public void update() {
                super.update();

                int inCart = getIcon().getParent().getMetaInteger("cart", 1);
                double price = getIcon().getParent().getMetaDouble("price", 0.01);

                if (price <= 0) {
                    getIcon().getParent().setMeta("price", 0.01);
                    price = 0.01;

                    SoundTools.play(getIcon().getOwner(), Sound.NOTE_BASS, 1F, 0F);

                    getIcon().getParent().updateAllIcons();
                }

                getIcon().getLore().clear();

                getIcon().addLore(ChatColor.GRAY + "     Amount Selling: " + ChatColor.AQUA + inCart);
                getIcon().addLore("");
                getIcon().addLore(ChatColor.GRAY + "     Price Per Item: " + ChatColor.GOLD + price + " gold.");
                getIcon().addLore(ChatColor.GRAY + "     Total Price: " + ChatColor.GOLD + price * inCart + " gold.");

                getIcon().getParent().updateIcon(getIcon());
            }
        });

        detailedMenu.addIcon(cIcon);

        cIcon.getParent().setMeta("cart", 1);
        cIcon.getHandler().update();

        cIcon = new Icon(8, 0, ChatColor.AQUA + "" + ChatColor.BOLD + "Match Market Price", new ItemStack(Material.PAPER)).setHandler(new IconHandler() {
            @Override
            public void onClick(IconClickEvent event) {
                super.onClick(event);

                double totalPrice = Market.getPrice((ItemStack) getIcon().getParent().getMeta("item"), 1);

                getIcon().getParent().setMeta("price", totalPrice);

                event.setCloseInventory(false);
            }

            @Override
            public void update() {
                super.update();

                getIcon().getLore().clear();

                int inCart = getIcon().getParent().getMetaInteger("cart", 1);
                double price = getIcon().getParent().getMetaDouble("price", 0.01);

                getIcon().addLore(ChatColor.GRAY + "     Amount Selling: " + ChatColor.AQUA + inCart);
                getIcon().addLore("");
                getIcon().addLore(ChatColor.GRAY + "     Price Per Item: " + ChatColor.GOLD + price + " gold.");
                getIcon().addLore(ChatColor.GRAY + "     Total Price: " + ChatColor.GOLD + price * inCart + " gold.");
            }
        });

        detailedMenu.addIcon(cIcon);

        cIcon.getParent().setMeta("cart", 1);
        cIcon.getHandler().update();

        for (int i = 0; i < nugs.length; i++) {
            int x = i;
            int y = 0;

            String name = "";

            name = ChatColor.AQUA + "\u2714 " + ChatColor.GOLD + nugs[i] + " cents.";

            Icon icon = new Icon(x, y, name, new ItemStack(Material.GOLD_NUGGET, nugs[i])).setHandler(new IconHandler() {
                @Override
                public void onClick(IconClickEvent event) {
                    super.onClick(event);

                    if (event.getOriginalEvent().getClick() == ClickType.LEFT) {
                        getIcon().getParent().setMeta("price", getIcon().getParent().getMetaDouble("price", 0.01) + ((double) (event.getItem().getAmount()) / 100));
                    }

                    if (event.getOriginalEvent().getClick() == ClickType.RIGHT) {
                        getIcon().getParent().setMeta("price", getIcon().getParent().getMetaDouble("price", 0.01) - ((double) (event.getItem().getAmount()) / 100));
                    }

                    event.getMenu().getHandler().update();
                    event.setCloseInventory(false);
                }
            });

            icon.addLore(ChatColor.GREEN + "   Left click to add to price.");
            icon.addLore(ChatColor.RED + "   Right click to remove from price.");
            detailedMenu.addIcon(icon);
        }

        for (int i = 0; i < ings.length; i++) {
            int x = i;
            int y = 1;

            String name = "";

            name = ChatColor.AQUA + "\u2714 " + ChatColor.GOLD + ings[i] + " gold.";

            Icon icon = new Icon(x, y, name, new ItemStack(Material.GOLD_INGOT, ings[i])).setHandler(new IconHandler() {
                @Override
                public void onClick(IconClickEvent event) {
                    super.onClick(event);

                    if (event.getOriginalEvent().getClick() == ClickType.LEFT) {
                        getIcon().getParent().setMeta("price", Helper.roundOff(getIcon().getParent().getMetaDouble("price", 0.01) + ((double) event.getItem().getAmount())));
                    }

                    if (event.getOriginalEvent().getClick() == ClickType.RIGHT) {
                        getIcon().getParent().setMeta("price", Helper.roundOff(getIcon().getParent().getMetaDouble("price", 0.01) - ((double) event.getItem().getAmount())));
                    }

                    event.getMenu().getHandler().update();
                    event.setCloseInventory(false);
                }
            });

            icon.addLore(ChatColor.GREEN + "   Left click to add to price.");
            icon.addLore(ChatColor.RED + "   Right click to remove from price.");
            detailedMenu.addIcon(icon);
        }

        for (int w = 0; w <= 1; w++) {
            for (int i = 0; i < values.length; i++) {
                int x = i;
                int y = w + 2;

                String name = "";

                if (w == 0) {
                    name = ChatColor.AQUA + "\u2714 " + ChatColor.GREEN + "Add " + ChatColor.DARK_GREEN + values[i] + ChatColor.GREEN + " to sale.";
                } else {
                    name = ChatColor.GOLD + "\u2716 " + ChatColor.RED + "Remove " + ChatColor.DARK_RED + values[i] + ChatColor.RED + " from sale.";
                }

                Icon icon = new Icon(x, y, name, new ItemStack(Material.WOOL, values[i], (w == 0) ? (short) 5 : (short) 14)).setHandler(new IconHandler() {
                    @Override
                    public void onClick(IconClickEvent event) {
                        super.onClick(event);


                        if (getIcon().getMetaBoolean("on", true)) {
                            if (getIcon().getMetaBoolean("give", true)) {
                                getIcon().getParent().setMeta("cart", getIcon().getParent().getMetaInteger("cart", 1) + event.getItem().getAmount());
                            } else {
                                getIcon().getParent().setMeta("cart", getIcon().getParent().getMetaInteger("cart", 1) - event.getItem().getAmount());
                            }
                        }

                        event.getMenu().getHandler().update();
                        event.setCloseInventory(false);
                    }

                    @Override
                    public void update() {
                        super.update();

                        int inCart = getIcon().getParent().getMetaInteger("cart", 1);

                        if (getIcon().getMetaBoolean("give", true)) {
                            int check = inCart + getIcon().getAmount();

                            if (check > Helper.countItemsInInventory(getIcon().getOwner(), ((ItemStack) getIcon().getParent().getMeta("item")), true)) {
                                if (getIcon().getMetaBoolean("on", true)) {
                                    getIcon().setItemStack(new ItemStack(Material.WOOL, getIcon().getAmount(), (short) 8));
                                    getIcon().setMeta("on", false);

                                    getIcon().getLore().clear();
                                    getIcon().addLore(ChatColor.GRAY + "  Disabled: Would exceed items in inventory.");
                                }
                            } else {
                                if (!getIcon().getMetaBoolean("on", true)) {
                                    getIcon().setItemStack(new ItemStack(Material.WOOL, getIcon().getAmount(), (short) 5));
                                    getIcon().setMeta("on", true);


                                    getIcon().getLore().clear();
                                    getIcon().setName(ChatColor.AQUA + "\u2714 " + ChatColor.GREEN + "Add " + ChatColor.DARK_GREEN + getIcon().getAmount() + ChatColor.GREEN + " to sale.");
                                }
                            }
                        } else {
                            int check = inCart - getIcon().getAmount();

                            if (check <= 0) {
                                if (getIcon().getMetaBoolean("on", true)) {
                                    getIcon().setItemStack(new ItemStack(Material.WOOL, getIcon().getAmount(), (short) 7));
                                    getIcon().setMeta("on", false);

                                    getIcon().getLore().clear();
                                    getIcon().addLore(ChatColor.GRAY + "  Disabled: Cannot purchase less than one item.");
                                }
                            } else {
                                if (!getIcon().getMetaBoolean("on", true)) {
                                    getIcon().setItemStack(new ItemStack(Material.WOOL, getIcon().getAmount(), (short) 14));
                                    getIcon().setMeta("on", true);

                                    getIcon().getLore().clear();
                                    getIcon().setName(ChatColor.GOLD + "\u2716 " + ChatColor.RED + "Remove " + ChatColor.DARK_RED + getIcon().getAmount() + ChatColor.RED + " from sale.");
                                }
                            }
                        }


                        getIcon().getParent().updateIcon(getIcon());
                    }
                });

                icon.setMeta("on", true);
                icon.setMeta("give", (w == 0) ? true : false);
                detailedMenu.addIcon(icon);
            }
        }
        for (int i = 0; i < 8; i++) {
            int x = i;
            int y = 5;

            Icon icon = new Icon(x, y, "", ((ItemStack) detailedMenu.getMeta("item")).clone()).setHandler(new IconHandler() {
                @Override
                public void onClick(IconClickEvent event) {
                    super.onClick(event);

                    event.setCloseInventory(false);
                }

                @Override
                public void update() {
                    super.update();

                    int toShow = getIcon().getParent().getMetaInteger("cart", 1);
                    toShow -= (getIcon().getLocation().getX() * Settings.STACK_SIZE);

                    if (toShow > 0) {
                        int amount = Math.min(toShow, Settings.STACK_SIZE);
                        getIcon().setAmount(amount);

                        if (!getIcon().isVisible()) {
                            getIcon().setVisible(true);
                        }

                        getIcon().getLore().clear();

                        int inCart = getIcon().getParent().getMetaInteger("cart", 1);
                        double price = getIcon().getParent().getMetaDouble("price", 0.01);

                        getIcon().addLore(ChatColor.GRAY + "     Amount Selling: " + ChatColor.AQUA + inCart);
                        getIcon().addLore("");
                        getIcon().addLore(ChatColor.GRAY + "     Price Per Item: " + ChatColor.GOLD + price + " gold.");
                        getIcon().addLore(ChatColor.GRAY + "     Total Price: " + ChatColor.GOLD + price * inCart + " gold.");
                    } else {
                        getIcon().setVisible(false);
                    }

                    getIcon().getParent().updateIcon(getIcon());
                }
            });

            detailedMenu.addIcon(icon);
            detailedMenu.getHandler().update();
            icon.getHandler().update();
        }

        Icon icon = new Icon(8, 5, "", new ItemStack(Material.BOOK)).setHandler(new IconHandler() {

            @Override
            public void onClick(IconClickEvent event) {
                super.onClick(event);

                event.setCloseInventory(false);
            }

            @Override
            public void update() {
                super.update();

                int toShow = getIcon().getParent().getMetaInteger("cart", 1);

                OfferCollection collection = Market.getCollection(getIcon().getItemStack().clone(), true);

                //Verify it has items.
                if (collection.getOffers().size() > 0) {

                    //How many of this specific item are on the market?
                    int amountInMarket = collection.getAmount();
                }

                toShow = Math.min(toShow, amountInMarket);
                toShow -= (getIcon().getLocation().getX() * Settings.STACK_SIZE);

                if (toShow > 0) {
                    getIcon().setVisible(true);
                    getIcon().setAmount(1);
                    getIcon().getLore().clear();
                    getIcon().setName(ChatColor.GRAY + "" + ChatColor.BOLD + "* EXTRA ITEMS *");

                    int inCart = getIcon().getParent().getMetaInteger("cart", 1);
                    double price = getIcon().getMetaDouble("price", 0.01);

                    getIcon().addLore("  " + ChatColor.RED + " You are selling more items");
                    getIcon().addLore("  " + ChatColor.RED + "   than can be displayed.");
                    getIcon().addLore("");

                    getIcon().addLore(ChatColor.GRAY + "     Amount Selling: " + ChatColor.AQUA + inCart);
                    getIcon().addLore("");
                    getIcon().addLore(ChatColor.GRAY + "     Price Per Item: " + ChatColor.GOLD + price + " gold.");
                    getIcon().addLore(ChatColor.GRAY + "     Total Price: " + ChatColor.GOLD + price * inCart + " gold.");
                } else {
                    getIcon().setVisible(false);
                }

                getIcon().getParent().updateIcon(getIcon());
            }
        });

        detailedMenu.addIcon(icon);
        icon.getHandler().update();
        detailedMenu.getHandler().update();

        return detailedMenu;
    }

    private static int getFirstMarketID(int page) {
        if (page == 0) {
            return 0;
        }

        //Get the first itemID for page.
        int itemid = Market.getIDOfMarketItems().get(48 * page);

        //Find out which index is the correct material.
        for (int i = 0; i < Material.values().length; i++) {
            if (Material.values()[i].getId() == itemid) {
                return i;
            }
        }

        return 0;
    }
}
