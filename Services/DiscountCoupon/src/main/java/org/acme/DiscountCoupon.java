package org.acme;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class DiscountCoupon {

	public enum DiscountType {
		PERCENTAGE_OFF,
		AMOUNT_OFF,
		BUY_ONE_GET_ONE_FREE,
	}

	public Long id;
	public Long idLoyaltyCard;
	public DiscountType discountType;
	public LocalDateTime expiryDate;

	public DiscountCoupon() {
	}

	public DiscountCoupon(Long id, Long idLoyaltyCard, DiscountType discountType, LocalDateTime expiryDate) {
		this.id = id;
		this.idLoyaltyCard = idLoyaltyCard;
		this.discountType = discountType;
		this.expiryDate = expiryDate;
	}

	@Override
	public String toString() {
		return "{ " + "id: " + id + ", discountType:" + discountType + ", loyaltyCard:" + idLoyaltyCard
				+ ", ExpiryDate:" + expiryDate + "}\n";
	}

	private static DiscountCoupon from(Row row) {
		return new DiscountCoupon(
				row.getLong("id"),
				row.getLong("loyaltyCardId"),
				DiscountType.values()[row.getInteger("discountType")],
				row.getLocalDateTime("expiryDate"));
	}

	public static DiscountCoupon from(JsonObject json) {
		return new DiscountCoupon(
				json.getLong("id"),
				json.getLong("loyaltyCardId"),
				DiscountType.values()[json.getInteger("discountType")],
				LocalDateTime.parse(json.getString("expiryDate")));
	}

	public static Multi<DiscountCoupon> findAll(MySQLPool client) {
		return client.query("SELECT id, loyaltyCardId, discountType, expiryDate FROM DiscountCoupons ORDER BY id ASC")
				.execute()
				.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
				.onItem().transform(DiscountCoupon::from);
	}

	public static Uni<DiscountCoupon> findById(MySQLPool client, Long id) {
		return client
				.preparedQuery("SELECT id, loyaltyCardId, discountType, expiryDate FROM DiscountCoupons WHERE id = ?")
				.execute(Tuple.of(id))
				.onItem().transform(RowSet::iterator)
				.onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
	}

	public Uni<Boolean> save(MySQLPool client, Long idLoyaltyCard_R, DiscountType discountType_R,
			LocalDateTime expiryDate_R) {
		return client
				.preparedQuery("INSERT INTO DiscountCoupons(loyaltyCardId, discountType, expiryDate) VALUES (?,?,?)")
				.execute(Tuple.of(idLoyaltyCard_R, discountType_R.ordinal(), expiryDate_R))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Uni<Boolean> delete(MySQLPool client, Long id_R) {
		return client.preparedQuery("DELETE FROM DiscountCoupons WHERE id = ?").execute(Tuple.of(id_R))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Uni<Boolean> update(MySQLPool client, Long id, Long idLoyaltyCard_R, Integer discountType_R,
			String expiryDate_R) {
		return client.preparedQuery(
				"UPDATE DiscountCoupons SET loyaltyCardId = ?, discountType = ?, expiryDate = ? WHERE id = ?")
				.execute(Tuple.of(idLoyaltyCard_R, discountType_R, expiryDate_R, id))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Multi<DiscountCoupon> generate(MySQLPool client) {
		return client.query("SELECT id, loyaltyCardId, discountType, expiryDate FROM DiscountCoupons ORDER BY id ASC")
				.execute()
				.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
				.onItem().transform(DiscountCoupon::from)
				.onItem().transformToUniAndConcatenate(discountCoupon -> {
					if (discountCoupon.expiryDate.isBefore(LocalDateTime.now())) {
						return client.preparedQuery("DELETE FROM DiscountCoupons WHERE id = ?")
								.execute(Tuple.of(discountCoupon.id))
								.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1)
								.onItem().transform(deleted -> deleted ? discountCoupon : null);
					}
					return Uni.createFrom().item(discountCoupon);
				});
	}

	public static Multi<DiscountCoupon> generate(MySQLPool client, JsonArray purchases, JsonArray customers) {
		List<JsonObject> jsonObjectList = new ArrayList<>();
		List<JsonObject> jsonObjectListCustomers = new ArrayList<>();
		purchases.forEach(purchase -> jsonObjectList.add((JsonObject) purchase));
		customers.forEach(customer -> jsonObjectListCustomers.add((JsonObject) customer));

		List<DiscountCoupon> discountCoupons = new ArrayList<>();
		for (JsonObject purchase : jsonObjectList) {
			String shopName = purchase.getString("shop_name");
			Long loyaltyCardId = purchase.getLong("loyaltyCard_id");

			boolean hasPurchase = jsonObjectList.stream()
					.anyMatch(p -> p.getString("shop_name").equals(shopName));

			DiscountCoupon discountCoupon = new DiscountCoupon(null, loyaltyCardId, DiscountType.AMOUNT_OFF,
					LocalDateTime.now().plus(1, ChronoUnit.WEEKS));
			discountCoupons.add(discountCoupon);

			if (!hasPurchase) {
				DiscountCoupon discountCouponWithoutPurchase = new DiscountCoupon(null, loyaltyCardId,
						DiscountType.PERCENTAGE_OFF, LocalDateTime.now().plus(1, ChronoUnit.WEEKS));
				discountCoupons.add(discountCouponWithoutPurchase);
			}
		}

		List<Tuple> batch = discountCoupons.stream()
				.map(discountCoupon -> Tuple.of(discountCoupon.idLoyaltyCard, discountCoupon.discountType.ordinal(),
						discountCoupon.expiryDate))
				.collect(Collectors.toList());

		Uni<Multi<DiscountCoupon>> uni = client
				.preparedQuery(
						"INSERT INTO DiscountCoupons (loyaltyCardId, discountType, expiryDate) VALUES (?, ?, ?)")
				.executeBatch(batch)
				.onItem().ignore().andContinueWithNull()
				.replaceWith(Multi.createFrom().iterable(discountCoupons));

		return uni.onItem().transformToMulti(u -> Multi.createFrom().items(discountCoupons.stream()));
	}
}
