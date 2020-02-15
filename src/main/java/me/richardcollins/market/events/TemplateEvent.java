package me.richardcollins.market.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TemplateEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;

	public TemplateEvent() {
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
