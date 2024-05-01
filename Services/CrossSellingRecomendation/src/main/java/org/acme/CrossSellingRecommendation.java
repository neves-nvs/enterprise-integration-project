package org.acme;

import java.time.Instant;
import java.time.LocalDateTime;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class CrossSellingRecommendation {

	public Long id;
	public Long idLoyaltyCard;
	public LocalDateTime expiryDate;

	public CrossSellingRecommendation() {
	}

	public CrossSellingRecommendation(Long id, Long idLoyaltyCard) {
		this.id = id;
		this.idLoyaltyCard = idLoyaltyCard;
	}

	@Override
	public String toString() {
		return "{ " + "id: " + id + ", LoyaltyCard:" + idLoyaltyCard + "}\n";

	}

	private static CrossSellingRecommendation from(Row row) {
		return new CrossSellingRecommendation(row.getLong("id"), row.getLong("LoyaltyCardId"));
	}

	public static Multi<CrossSellingRecommendation> findAll(MySQLPool client) {
		return client.query("SELECT id, LoyaltyCardId FROM CrossSellingRecommendations ORDER BY id ASC").execute()
				.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
				.onItem().transform(CrossSellingRecommendation::from);
	}

	public static Uni<CrossSellingRecommendation> findById(MySQLPool client, Long id) {
		return client.preparedQuery("SELECT id, LoyaltyCardId FROM CrossSellingRecommendations WHERE id = ?")
				.execute(Tuple.of(id))
				.onItem().transform(RowSet::iterator)
				.onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
	}

	public Uni<Boolean> save(MySQLPool client, Long idLoyaltyCard_R) {
		return client.preparedQuery("INSERT INTO CrossSellingRecommendations(LoyaltyCardId) VALUES (?)")
				.execute(Tuple.of(idLoyaltyCard_R))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Uni<Boolean> delete(MySQLPool client, Long id_R) {
		return client.preparedQuery("DELETE FROM CrossSellingRecommendations WHERE id = ?").execute(Tuple.of(id_R))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
	}

	public static Uni<Boolean> update(MySQLPool client, Long id, Long idLoyaltyCard_R) {
		return client.preparedQuery(
				"UPDATE CrossSellingRecommendations SET LoyaltyCardId = ? WHERE id = ?")
				.execute(Tuple.of(idLoyaltyCard_R, id))
				.onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);

	}
}
