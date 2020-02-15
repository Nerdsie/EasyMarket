package me.richardcollins.market.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class MarketChooseEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private ItemStack item;
	private String name;

	public MarketChooseEvent(ItemStack item, String name) {
		this.item = item;
		this.name = name;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
