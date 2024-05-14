package org.acme;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Uni;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(MySQLTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
class CrossSellingRecommendationTest {

    @Inject
    MySQLPool client;

    @Channel("CrossSellingRecommendation")
    Emitter<CrossSellingRecommendation> emitter;

    @BeforeEach
    public void setupDatabase() {
        client.query(
                "CREATE TABLE IF NOT EXISTS CrossSellingRecommendations (id INT PRIMARY KEY AUTO_INCREMENT, LoyaltyCardId BIGINT, ExpiryDate DATETIME)")
                .execute().await().indefinitely();
        client.query("TRUNCATE TABLE CrossSellingRecommendations").execute().await().indefinitely();
    }

    @Test
    void testFindAll() {
        insertTestRecommendation(1L, 100L);
        Multi<CrossSellingRecommendation> recommendations = CrossSellingRecommendation.findAll(client);
        assertEquals(1, recommendations.collect().asList().await().indefinitely().size());
    }

    @Test
    void testFindById() {
        Long id = insertTestRecommendation(2L, 100L);
        Uni<CrossSellingRecommendation> coupon = CrossSellingRecommendation.findById(client, id);
        assertNotNull(coupon.await().indefinitely());
    }

    @Test
    void testSave() {
        CrossSellingRecommendation recommendation = new CrossSellingRecommendation();
        Uni<Boolean> result = recommendation.save(client, 101L);
        assertTrue(result.await().indefinitely());
    }

    @Test
    void testDelete() {
        Long id = insertTestRecommendation(3L, 102L);
        Uni<Boolean> result = CrossSellingRecommendation.delete(client, id);
        assertTrue(result.await().indefinitely());
    }

    @Test
    void testUpdate() {
        Long id = insertTestRecommendation(4L, 103L);
        Uni<Boolean> result = CrossSellingRecommendation.update(client, id, 104L);
        assertTrue(result.await().indefinitely());
    }

    private Long insertTestRecommendation(Long id, Long loyaltyCardId) {
        client.preparedQuery(
                "INSERT INTO CrossSellingRecommendations(id, LoyaltyCardId) VALUES (?,?)")
                .execute(Tuple.of(id, loyaltyCardId))
                .await().indefinitely();
        return id;
    }
}
