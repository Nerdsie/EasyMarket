package me.richardcollins.market;

import me.richardcollins.universal.Universal;
import org.bukkit.ChatColor;

public class Settings {
    public static final String BOOK_LORE[] = {" ", ChatColor.GRAY + "Items currently for sale.", ChatColor.GRAY + "/nerdsmarket (/nm) for more info."};
    public static final String BOOK_TITLE = ChatColor.MAGIC + "* " + ChatColor.RESET + ChatColor.LIGHT_PURPLE + "Nerds Market" +
            ChatColor.RESET + ChatColor.MAGIC + " *" + ChatColor.RESET;

    public static final boolean GIVE_ON_JOIN = false;
    public static final boolean GIVE_ON_NEW = true;

    public static final String PREFIX = ChatColor.DARK_AQUA + " [" + ChatColor.GOLD + "Market" + ChatColor.DARK_AQUA + "] " + ChatColor.GREEN;

    public static final int STACK_SIZE = 64;

    // ------- MySQL info --------- //

    public static String database = "market";

    public static void load(Market plugin) {
        database = Universal.getUniversalConfig().getString("database.market");
    }
}
