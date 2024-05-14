package org.acme;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(MySQLTestResource.class)
public class LoyaltyCardTest {

    @Inject
    MySQLPool client;

    @BeforeEach
    public void setupDatabase() {
        client.query(
                "CREATE TABLE IF NOT EXISTS LoyaltyCards (id INT PRIMARY KEY AUTO_INCREMENT, idCustomer BIGINT UNSIGNED, idShop BIGINT UNSIGNED)")
                .execute().await().indefinitely();
        client.query("TRUNCATE TABLE LoyaltyCards").execute().await().indefinitely();
    }

    @Test
    public void testSaveLoyaltyCard() {
        Loyaltycard loyaltycard = new Loyaltycard();
        Uni<Boolean> result = loyaltycard.save(client, 1L, 1L);

        assertTrue(result.await().indefinitely());

        Loyaltycard savedLoyaltycard = Loyaltycard.findById(client, 1L).await().indefinitely();
        assertNotNull(savedLoyaltycard);
        assertEquals(1, savedLoyaltycard.idCustomer);
        assertEquals(1, savedLoyaltycard.idShop);
    }

    @Test
    public void testFindAllLoyaltycards() {
        client.preparedQuery("INSERT INTO LoyaltyCards (idCustomer,idShop) VALUES (?,?)")
                .execute(Tuple.of(1, 1)).await().indefinitely();
        client.preparedQuery("INSERT INTO LoyaltyCards (idCustomer,idShop) VALUES (?,?)")
                .execute(Tuple.of(2, 2)).await().indefinitely();

        List<Loyaltycard> loyaltycards = Loyaltycard.findAll(client).collect().asList().await().indefinitely();

        assertNotNull(loyaltycards);
        assertEquals(2, loyaltycards.size());
        assertEquals(1, loyaltycards.get(0).idCustomer);
        assertEquals(2, loyaltycards.get(1).idCustomer);
    }

    @Test
    public void testFindLoyaltycardById() {
        client.preparedQuery("INSERT INTO LoyaltyCards (idCustomer,idShop) VALUES (?,?)")
                .execute(Tuple.of(1, 1)).await().indefinitely();

        Loyaltycard loyaltycard = Loyaltycard.findById(client, 1L).await().indefinitely();

        assertNotNull(loyaltycard);
        assertEquals(1, loyaltycard.idCustomer);
        assertEquals(1, loyaltycard.idShop);
    }

    @Test
    public void testUpdateLoyaltycard() {
        client.preparedQuery("INSERT INTO LoyaltyCards (idCustomer,idShop) VALUES (?,?)")
                .execute(Tuple.of(1, 1)).await().indefinitely();

        Uni<Boolean> result = Loyaltycard.update(client, 1L, 1L, 3L);

        assertTrue(result.await().indefinitely());

        Loyaltycard updatedLoyaltycard = Loyaltycard.findById(client, 1L).await().indefinitely();
        assertNotNull(updatedLoyaltycard);
        assertEquals(1, updatedLoyaltycard.idCustomer);
        assertEquals(3, updatedLoyaltycard.idShop);
    }

    @Test
    public void testDeleteLoyaltycard() {
        client.preparedQuery("INSERT INTO LoyaltyCards (idCustomer,idShop) VALUES (?,?)")
                .execute(Tuple.of(1, 1)).await().indefinitely();

        Uni<Boolean> result = Loyaltycard.delete(client, 1L);

        assertTrue(result.await().indefinitely());

        Loyaltycard deletedLoyaltycard = Loyaltycard.findById(client, 1L).await().indefinitely();
        assertNull(deletedLoyaltycard);
    }
}
