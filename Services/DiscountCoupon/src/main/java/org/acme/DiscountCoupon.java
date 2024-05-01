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
		return "{ " + "id: " + id + ", DiscountType:" + discountType + ", LoyaltyCard:" + idLoyaltyCard
				+ ", ExpiryDate:" + expiryDate + "}\n";
	}

	private static DiscountCoupon from(Row row) {
		return new DiscountCoupon(row.getLong("id"), row.getLong("LoyaltyCardId"),
				fromCode(row.getInteger("DiscountType")),
				row.getLocalDateTime("ExpiryDate"));
	}

	public static Multi<DiscountCoupon> findAll(MySQLPool client) {
		return client.query("SELECT id, LoyaltyCardId, DiscountType, ExpiryDate FROM DiscountCoupons ORDER BY id ASC")
				.execute()
				.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
				.onItem().transform(DiscountCoupon::from);
	}

	public static Uni<DiscountCoupon> findById(MySQLPool client, Long id) {
		return client
				.preparedQuery("SELECT id, LoyaltyCardId, DiscountType, ExpiryDate FROM DiscountCoupons WHERE id = ?")
				.execute(Tuple.of(id))
				.onItem().transform(RowSet::iterator)
				.onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
	}

	public Uni<Boolean> save(MySQLPool client, Long idLoyaltyCard_R, DiscountType discountType_R,
			LocalDateTime expiryDate_R) {
		return client
				.preparedQuery("INSERT INTO DiscountCoupons(LoyaltyCardId, DiscountType, ExpiryDate) VALUES (?,?,?)")
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
				"UPDATE DiscountCoupons SET LoyaltyCardId = ?, DiscountType = ?, ExpiryDate = ? WHERE id = ?")
				.execute(Tuple.of(idLoyaltyCard_R, discountType_R, expiryDate_R, id))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);

	}
}
