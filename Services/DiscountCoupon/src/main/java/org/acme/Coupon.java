package org.acme;

import java.time.Instant;
import java.time.LocalDateTime;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class Coupon {

	public enum DiscountType {
		PERCENTAGE_OFF,
		AMOUNT_OFF,
		BUY_ONE_GET_ONE_FREE,
	}

	public static DiscountType fromCode(Integer code) {
		for (DiscountType type : DiscountType.values()) {
			if (type.getCode() == code) {
				return type;
			}
		}
		throw new IllegalArgumentException("No discount type with code " + code);
	}

	private Long idLoyaltyCard;
	private DiscountType discountType;
	private LocalDateTime expiryDate;

	public Coupon() {
	}

	public Coupon(Long idLoyaltyCard, DiscountType discountType, LocalDateTime expiryDate) {
		this.idLoyaltyCard = idLoyaltyCard;
		this.discountType = discountType;
		this.expiryDate = expiryDate;
	}

	@Override
	public String toString() {
		return "{ DiscountType:" + discountType + " LoyaltyCard:" + idLoyaltyCard + " ExpiryDate:" + expiryDate + "}\n";
	}

	private static Coupon from(Row row) {
		return new Coupon(row.getLong("idLoyaltyCard"), fromCode(row.getInteger("discount")),
				row.getLocalDateTime("expiryDate"));
	}

	// public static Multi<Customer> findAll(MySQLPool client) {
	// return client.query("SELECT expiryDate, discount FROM Coupons ORDER BY
	// expiryDate ASC").execute()
	// .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
	// .onItem().transform(Coupon);
	// }
	public static Multi<Coupon> findAll(MySQLPool client) {
		return client.query("SELECT expiryDate, discount FROM Coupons ORDER BY expiryDate ASC").execute()
				.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
				.onItem().transform(Coupon::from);
	}

	// public static Uni<Coupon> findByType(MySQLPool client, Long discount) {
	// return client.preparedQuery("SELECT expiryDate, discount FROM Coupons WHERE
	// discount = ?")
	// .execute(Tuple.of(discount))
	// .onItem().transform(RowSet::iterator)
	// .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) :
	// null);
	// }

	public static Uni<Coupon> findByLoyaltyCardId(MySQLPool client, Long idLoyaltyCard) {
		return client.preparedQuery("SELECT expiryDate, discount FROM Coupons WHERE idLoyaltyCard = ?")
				.execute(Tuple.of(idLoyaltyCard))
				.onItem().transform(RowSet::iterator)
				.onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
	}

	public Uni<Boolean> save(MySQLPool client, Long idLoyaltyCard_R, Long idShop_R, DiscountType discount_R,
			LocalDateTime expiryDate_R) {
		return client.preparedQuery("INSERT INTO Coupons(idLoyaltyCard, idShop, discount, expiryDate) VALUES (?,?,?,?)")
				.execute(Tuple.of(idLoyaltyCard_R, idShop_R, discount_R, expiryDate_R))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	// public static Uni<Boolean> delete(MySQLPool client, Long id_R) {
	// return client.preparedQuery("DELETE FROM Customers WHERE id =
	// ?").execute(Tuple.of(id_R))
	// .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	// }
	// delete from db
	public static Uni<Boolean> delete(MySQLPool client, Long id_R) {
		return client.preparedQuery("DELETE FROM Coupons WHERE id = ?").execute(Tuple.of(id_R))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	// public static Uni<Boolean> update(MySQLPool client, Long id_R, String name_R,
	// Long fnumber, String loc) {
	// return client.preparedQuery("UPDATE Customers SET name = ?, FiscalNumber = ?
	// , location = ? WHERE id = ?")
	// .execute(Tuple.of(name_R, fnumber, loc, id_R))
	// .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	// }
	// update db
	public static Uni<Boolean> update(MySQLPool client, Long id_R, Long idLoyaltyCard_R, Long idShop_R,
			DiscountType discount_R, LocalDateTime expiryDate_R) {
		return client.preparedQuery(
				"UPDATE Coupons SET idLoyaltyCard = ? , idShop = ? , discount = ? , expiryDate = ? WHERE id = ?")
				.execute(Tuple.of(idLoyaltyCard_R, idShop_R, discount_R, expiryDate_R, id_R))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}
}
