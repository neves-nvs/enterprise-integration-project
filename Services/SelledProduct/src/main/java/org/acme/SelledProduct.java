package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import java.sql.Timestamp;
import java.util.List;

public class SelledProduct {

	public Long id;
	public Long idPurchase;

	public Integer idCoupon;
	public Long idCustomer;
	public String location;
	public Long idLoyaltycard;
	public Long idShop;

	public SelledProduct(Long id, Long idPurchase, Integer idCoupon, String location, Long idCustomer,
			Long idLoyaltycard, Long idShop) {
		this.id = id;
		this.idPurchase = idPurchase;

		this.idCoupon = idCoupon;
		this.idCustomer = idCustomer;
		this.location = location;
		this.idLoyaltycard = idLoyaltycard;
		this.idShop = idShop;
	}

	@Override
	public String toString() {
		return "{ id:" + id + ", idPurchase:" + idPurchase + ", idCoupon:" + idCoupon + ", idCustomer:" + idCustomer
				+ ", location:" + location + ", idLoyaltycard:" + idLoyaltycard + ", idShop:" + idShop + "}\n";
	}

	public static Uni<SelledProduct> findById(MySQLPool client, Long id) {
		return client.preparedQuery(
				"SELECT id, idPurchase, idCustomer, idShop, idLoyaltycard, location FROM SelledProduct WHERE id = ?")
				.execute(Tuple.of(id))
				.onItem().transform(RowSet::iterator)
				.onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
	}

	public static Multi<SelledProduct> findAll(MySQLPool client) {
		return client.query(
				"SELECT id, idPurchase, idCustomer, idShop, idLoyaltycard, location FROM SelledProduct ORDER BY id ASC")
				.execute()
				.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
				.onItem().transform(SelledProduct::from);
	}

	public static Uni<Boolean> save(MySQLPool client, Long idPurchase_R, Integer idCoupon_R, Long idCustomer_R,
			String location, Long idLoyaltycard_R, Long idShop_R) {
		return client.preparedQuery(
				"INSERT INTO SelledProduct(idPurchase, idCustomer, idShop, idLoyaltycard, product, price, location) VALUES (?,?,?,?,?,?,?)")
				.execute(Tuple.tuple(
						List.of(idPurchase_R, idCoupon_R, idCustomer_R, location, idLoyaltycard_R, idShop_R)))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Uni<Boolean> delete(MySQLPool client, Long id) {
		return client.preparedQuery("DELETE FROM SelledProduct WHERE id = ?").execute(Tuple.of(id))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Uni<Boolean> update(MySQLPool client, Long id, Long idPurchase, Long idCustomer, Long idShop,
			Long idLoyaltycard, String product, Float price, String location) {
		return client.preparedQuery(
				"UPDATE SelledProduct SET idPurchase = ?, idCustomer = ?, idShop = ?, idLoyaltycard = ?, product = ?, price = ?, location = ? WHERE id = ?")
				.execute(Tuple
						.tuple(List.of(idPurchase, idCustomer, idShop, idLoyaltycard, product, price, location, id)))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static SelledProduct from(Row row) {
		return new SelledProduct(
				row.getLong("id"), row.getLong("idPurchase"),
				row.getInteger("idCoupon"),
				row.getString("location"),
				row.getLong("idCustomer"),
				row.getLong("idLoyaltycard"),
				row.getLong("idShop"));
	}

	public Uni<Boolean> update(MySQLPool client, Long id, Long idPurchase, Integer idCoupon, Long idCustomer,
			String location, Long idLoyaltycard, Long idShop) {
		return client.preparedQuery(
				"UPDATE SelledProduct SET idPurchase = ?, idCoupon = ?, idCustomer = ?, location = ?, idLoyaltycard = ?, idShop = ? WHERE id = ?")
				.execute(Tuple.from(List.of(idPurchase, idCoupon, idCustomer, location, idLoyaltycard, idShop, id)))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

}
