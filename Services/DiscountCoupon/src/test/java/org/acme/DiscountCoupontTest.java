package org.acme;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

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
class DiscountCouponTest {

    @Inject
    MySQLPool client;

    @Channel("DiscountCoupon")
    Emitter<DiscountCoupon> emitter;

    @BeforeEach
    public void setupDatabase() {
        client.query(
                "CREATE TABLE IF NOT EXISTS DiscountCoupons (id INT PRIMARY KEY AUTO_INCREMENT, LoyaltyCardId BIGINT, DiscountType INT, ExpiryDate DATETIME)")
                .execute().await().indefinitely();
        client.query("TRUNCATE TABLE DiscountCoupons").execute().await().indefinitely();
    }

    @Test
    void testFindAll() {
        insertTestCoupon(1L, 100L, 1, LocalDateTime.now().plusDays(10));
        Multi<DiscountCoupon> coupons = DiscountCoupon.findAll(client);
        assertEquals(1, coupons.collect().asList().await().indefinitely().size());
    }

    @Test
    void testFindById() {
        Long id = insertTestCoupon(2L, 100L, 1, LocalDateTime.now().plusDays(10));
        Uni<DiscountCoupon> coupon = DiscountCoupon.findById(client, id);
        assertNotNull(coupon.await().indefinitely());
    }

    @Test
    void testSave() {
        DiscountCoupon coupon = new DiscountCoupon();
        Uni<Boolean> result = coupon.save(client, 101L, DiscountCoupon.DiscountType.PERCENTAGE_OFF,
                LocalDateTime.now().plusDays(10));
        assertTrue(result.await().indefinitely());
    }

    @Test
    void testDelete() {
        Long id = insertTestCoupon(3L, 102L, 2, LocalDateTime.now().plusDays(10));
        Uni<Boolean> result = DiscountCoupon.delete(client, id);
        assertTrue(result.await().indefinitely());
    }

    @Test
    void testUpdate() {
        Long id = insertTestCoupon(4L, 103L, 0, LocalDateTime.now().plusDays(10));
        Uni<Boolean> result = DiscountCoupon.update(client, id, 104L, 1, "2023-12-31T23:59:59");
        assertTrue(result.await().indefinitely());
    }

    private Long insertTestCoupon(Long id, Long loyaltyCardId, int discountType, LocalDateTime expiryDate) {
        client.preparedQuery(
                "INSERT INTO DiscountCoupons(id, LoyaltyCardId, DiscountType, ExpiryDate) VALUES (?,?,?,?)")
                .execute(Tuple.of(id, loyaltyCardId, discountType, expiryDate))
                .await().indefinitely();
        return id;
    }
}
