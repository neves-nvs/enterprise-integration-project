package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import java.util.List;

public class SelledProduct {

	public Long id;
	public Long idPurchase;

	public Long idDiscountCoupon;
	public Long idCustomer;
	public String location;
	public Long idLoyaltyCard;
	public Long idShop;

	public SelledProduct(Long id, Long idPurchase, Long idDiscountCoupon, Long idCustomer, String location,
			Long idLoyaltyCard, Long idShop) {
		this.id = id;
		this.idPurchase = idPurchase;
		this.idDiscountCoupon = idDiscountCoupon;
		this.idCustomer = idCustomer;
		this.location = location;
		this.idLoyaltyCard = idLoyaltyCard;
		this.idShop = idShop;
	}

	@Override
	public String toString() {
		return "{ id:" + id + ", idPurchase:" + idPurchase + ", idDiscountCoupon:" + idDiscountCoupon + ", idCustomer:"
				+ idCustomer + ", location:" + location + ", idLoyaltyCard:" + idLoyaltyCard + ", idShop:" + idShop
				+ "}\n";
	}

	public static SelledProduct from(Row row) {
		return new SelledProduct(
				row.getLong("id"),
				row.getLong("PurchaseId"),
				row.getLong("DiscountCouponId"),
				row.getLong("CustomerId"),
				row.getString("Location"),
				row.getLong("LoyaltyCardId"),
				row.getLong("ShopId"));
	}

	public static Uni<SelledProduct> findById(MySQLPool client, Long id) {
		return client.preparedQuery(
				"SELECT id, PurchaseId, DiscountCouponId, CustomerId, Location, LoyaltyCardId, ShopId FROM SelledProducts WHERE id = ?")
				.execute(Tuple.of(id))
				.onItem().transform(RowSet::iterator)
				.onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
	}

	public static Multi<SelledProduct> findAll(MySQLPool client) {
		return client.query(
				"SELECT id, PurchaseId, DiscountCouponId, CustomerId, Location, LoyaltyCardId, ShopId FROM SelledProducts ORDER BY id ")
				.execute()
				.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
				.onItem().transform(SelledProduct::from);
	}

	public static Uni<Boolean> save(MySQLPool client, Long idPurchase_R, Long idDiscountCoupon_R, Long idCustomer_R,
			String location, Long idLoyaltyCard_R, Long idShop_R) {

		Tuple tuple = Tuple.tuple();
		tuple.addLong(idPurchase_R);
		tuple.addLong(idDiscountCoupon_R == null ? 0L : idDiscountCoupon_R);
		tuple.addLong(idCustomer_R == null ? 0L : idCustomer_R);
		tuple.addString(location);
		tuple.addLong(idLoyaltyCard_R == null ? 0L : idLoyaltyCard_R);
		tuple.addLong(idShop_R == null ? 0L : idShop_R);

		return client.preparedQuery(
				"INSERT INTO SelledProducts(PurchaseId, DiscountCouponId, CustomerId, Location, LoyaltyCardId, ShopId) VALUES (?,?,?,?,?,?)")
				.execute(tuple)
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Uni<Boolean> delete(MySQLPool client, Long id) {
		return client.preparedQuery("DELETE FROM SelledProducts WHERE id = ?").execute(Tuple.of(id))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Uni<Boolean> update(MySQLPool client, Long id, Long idPurchase, Long idDiscountCoupon,
			Long idCustomer, String location, Long idLoyaltyCard, Long idShop) {
		return client.preparedQuery(
				"UPDATE SelledProducts SET PurchaseId = ?, DiscountCouponId = ?, CustomerId = ?, Location = ?, LoyaltyCardId = ?, ShopId = ? WHERE id = ?")
				.execute(Tuple
						.tuple(List.of(idPurchase, idDiscountCoupon, idCustomer, location, idLoyaltyCard, idShop, id)))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

}
