package me.richardcollins.market.managers;

import me.richardcollins.market.market.PendingOffer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class MarketManager {
	public static HashMap<String, ItemStack> chosen = new HashMap<String, ItemStack>();
	public static HashMap<String, PendingOffer> pending = new HashMap<String, PendingOffer>();

	public static boolean playerChosenItem(String name) {
		return chosen.containsKey(name);
	}

	public static boolean playerHasPending(String name) {
		return pending.containsKey(name);
	}

	public static ItemStack getChosenItemStack(String name) {
		return chosen.get(name);
	}

	public static void removePending(String name) {
		chosen.remove(name);
	}

	public static void clearChosen(String name) {
		chosen.remove(name);
	}

	public static void setPending(Player player, PendingOffer pendingOffer) {
		pending.put(player.getName(), pendingOffer);
	}

	public static void setChosenItemStack(String n, ItemStack m) {
		chosen.put(n, m);
	}

	public static PendingOffer getPending(String name) {
		return pending.get(name);
	}
}
