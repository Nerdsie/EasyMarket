package me.richardcollins.market.storage;

import me.richardcollins.market.market.Offer;

import java.sql.ResultSet;

public class DataManager {
	public MySQL core = null;

	public static String tableName = "offers";

	public DataManager(MySQL c) {
		core = c;

		core.open();

		startDB();
	}

	/*

	seller
	amount
	item-id
	item-data
	price-per
	enchants

	 */

	public void startDB() {
		if (!core.tableExists(tableName)) {
			String query = "CREATE TABLE IF NOT EXISTS " + tableName + " ( `id` bigint(20) NOT NULL auto_increment, `seller` text NOT NULL, `amount` int NOT NULL, " +
					"`item-id` int NOT NULL, `item-data` int NOT NULL, `price-per` double NOT NULL, `enchants` text NOT NULL, `active` boolean DEFAULT true, PRIMARY KEY (`id`));";
			core.execute(query);
		}
	}

	public ResultSet getAllOffers() {
		String query = "SELECT * FROM " + tableName + ";";
		ResultSet rSet = core.select(query);

		return rSet;
	}

	public void updateOfferAmount(Offer offer) {
		String query = "UPDATE " + tableName + " SET amount =  '" + offer.getAmount() + "' WHERE id = '" + offer.getID() + "';";
		core.update(query);
	}

	public void removeOffer(int id) {
		String query = "UPDATE " + tableName + " SET active = '0' WHERE id = '" + id + "';";
		core.update(query);
	}

	public void addOffer(Offer offer) {
		String query = "INSERT INTO " + tableName + " (`seller`,`amount`, `item-id`, `item-data`, `price-per`, `enchants`) VALUES ('" + offer.getSeller() + "','" + offer.getAmount() + "'," +
				"'" + offer.getItemStack().getTypeId() + "','" + ((int) offer.getItemStack().getDurability()) + "','" + offer.getPricePer() + "','" + offer.getEnchants() + "');";
		core.insert(query);
	}
}
