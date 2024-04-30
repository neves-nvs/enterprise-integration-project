package org.acme;

import java.time.Instant;
import java.time.LocalDateTime;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
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

	public static DiscountType fromCode(Integer code) {
		return DiscountType.values()[code];
	}

	public Long idLoyaltyCard;
	public DiscountType discountType;
	public LocalDateTime expiryDate;

	public DiscountCoupon() {
	}

	public DiscountCoupon(Long idLoyaltyCard, DiscountType discountType, LocalDateTime expiryDate) {
		this.idLoyaltyCard = idLoyaltyCard;
		this.discountType = discountType;
		this.expiryDate = expiryDate;
	}

	@Override
	public String toString() {
		return "{ DiscountType:" + discountType + " LoyaltyCard:" + idLoyaltyCard + " ExpiryDate:" + expiryDate + "}\n";
	}

	private static DiscountCoupon from(Row row) {
		return new DiscountCoupon(row.getLong("idLoyaltyCard"), fromCode(row.getInteger("discount")),
				row.getLocalDateTime("expiryDate"));
	}

	public static Multi<DiscountCoupon> findAll(MySQLPool client) {
		return client.query("SELECT expiryDate, discount FROM Coupons ORDER BY expiryDate ASC").execute()
				.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
				.onItem().transform(DiscountCoupon::from);
	}

	public static Uni<DiscountCoupon> findById(MySQLPool client, Long id) {
		return client.preparedQuery("SELECT expiryDate, discount FROM Coupons WHERE id = ?").execute(Tuple.of(id))
				.onItem().transform(RowSet::iterator)
				.onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
	}

	public Uni<Boolean> save(MySQLPool client, Long idLoyaltyCard_R, DiscountType discount_R,
			LocalDateTime expiryDate_R) {
		return client.preparedQuery("INSERT INTO Coupons(idLoyaltyCard, discount, expiryDate) VALUES (?,?,?)")
				.execute(Tuple.of(idLoyaltyCard_R, discount_R, expiryDate_R))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Uni<Boolean> delete(MySQLPool client, Long id_R) {
		return client.preparedQuery("DELETE FROM Coupons WHERE id = ?").execute(Tuple.of(id_R))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Uni<Boolean> update(MySQLPool client, Long id, Long idLoyaltyCard_R, Integer discountType_R,
			String expiryDate_R) {
		return client.preparedQuery(
				"UPDATE Coupons SET idLoyaltyCard = ?, discount = ?, expiryDate = ? WHERE id = ?")
				.execute(Tuple.of(idLoyaltyCard_R, discountType_R, expiryDate_R, id))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);

	}
}
