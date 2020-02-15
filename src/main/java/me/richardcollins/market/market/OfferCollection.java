package me.richardcollins.market.market;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class OfferCollection {
	private ArrayList<Offer> offers = new ArrayList<Offer>();

	public OfferCollection(ArrayList<Offer> allOffers) {
		offers = allOffers;
	}

	public OfferCollection() {
	}

	public int getAmount() {
		int toRet = 0;

		for (Offer o : getOffers()) {
			toRet += o.getAmount();
		}

		return toRet;
	}

	public ArrayList<Offer> getOffers() {
		return offers;
	}

	public void setOffers(ArrayList<Offer> offer) {
		this.offers = offer;
	}

	public void addOffer(Offer o) {
		getOffers().add(o);
	}

	public ItemStack getItemStack() {
		try {
			return offers.get(0).getItemStack();
		} catch (Exception e) {
			return null;
		}
	}
}
