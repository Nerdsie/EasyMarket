package me.richardcollins.market.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class MarketSellEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private String name;
	private int amount;
	private double pricePer;
	private ItemStack item;

	public MarketSellEvent(String name, int amount, double pricePer, ItemStack item) {
		this.name = name;
		this.amount = amount;
		this.pricePer = pricePer;
		this.item = item;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public void setPricePer(double cost) {
		this.pricePer = cost;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
