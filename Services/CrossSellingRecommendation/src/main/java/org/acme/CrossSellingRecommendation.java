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

	public CrossSellingRecommendation(Long id, Long idLoyaltyCard, LocalDateTime expiryDate) {
		this.id = id;
		this.idLoyaltyCard = idLoyaltyCard;
		this.expiryDate = expiryDate;
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

	// Format
	// [
	// {
	// "id": 0,
	// "timestamp": "2022-03-10T12:15:50",
	// "price": 0,
	// "product": "string",
	// "supplier": "string",
	// "shop_name": "string",
	// "loyaltyCard_id": 0
	// }
	// ]
	public static Multi<CrossSellingRecommendation> generate(MySQLPool client, JsonArray purchases) {
		// Convert JsonArray to List<JsonObject>
		List<JsonObject> jsonObjectList = new ArrayList<>();
		for (int i = 0; i < purchases.size(); i++) {
			jsonObjectList.add(purchases.getJsonObject(i));
		}

		// Prepare recommendations
		List<CrossSellingRecommendation> recommendations = new ArrayList<>();
		for (JsonObject purchase : jsonObjectList) {
			String shopName = purchase.getString("shop_name");
			Long loyaltyCardId = purchase.getLong("loyaltyCard_id");

			boolean hasPurchase = jsonObjectList.stream()
					.anyMatch(p -> p.getString("shop_name").equals(shopName));

			CrossSellingRecommendation recommendation = new CrossSellingRecommendation(null, loyaltyCardId,
					LocalDateTime.now().plus(1, ChronoUnit.WEEKS));
			recommendations.add(recommendation);

			if (!hasPurchase) {
				CrossSellingRecommendation recommendationWithoutPurchase = new CrossSellingRecommendation(null,
						loyaltyCardId);
				recommendations.add(recommendationWithoutPurchase);
			}
		}

		List<Tuple> batch = recommendations.stream()
				.map(recommendation -> Tuple.of(recommendation.idLoyaltyCard))
				.collect(Collectors.toList());

		Uni<Multi<CrossSellingRecommendation>> uni = client
				.preparedQuery("INSERT INTO CrossSellingRecommendations (LoyaltyCardId) VALUES (?)")
				.executeBatch(batch)
				.onItem().ignore().andContinueWithNull()
				.replaceWith(Multi.createFrom().iterable(recommendations));

		return uni.onItem().transformToMulti(u -> Multi.createFrom().items(recommendations.stream()));
	}
}
