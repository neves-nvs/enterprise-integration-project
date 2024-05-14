package org.acme;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import io.quarkus.test.common.QuarkusTestResource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import jakarta.inject.Inject;

@QuarkusTestResource(MySQLTestResource.class)
public class SelledProductTest {

    @Inject
    MySQLPool client;

    @BeforeEach
    public void setupDatabase() {
        client.query(
                "CREATE TABLE IF NOT EXISTS SelledProducts (id INT PRIMARY KEY AUTO_INCREMENT, idProduct BIGINT UNSIGNED, idCustomer BIGINT UNSIGNED, idShop BIGINT UNSIGNED, location VARCHAR(255), idLoyaltyCard BIGINT UNSIGNED, idPromotion BIGINT UNSIGNED)")
                .execute().await().indefinitely();
        client.query("TRUNCATE TABLE SelledProducts").execute().await().indefinitely();
    }

    @Test
    public void testSave() {
        Uni<Boolean> result = SelledProduct.save(client, 1L, 1L, 1L, "Location A", 1L, 1L);
        assertTrue(result.await().indefinitely());
    }

    @Test
    public void testFindById() {
        SelledProduct.save(client, 2L, 2L, 2L, "Location B", 2L, 2L).await().indefinitely();
        Uni<SelledProduct> result = SelledProduct.findById(client, 1L);
        SelledProduct selledProduct = result.await().indefinitely();
        assertNotNull(selledProduct);
        assertEquals(1L, selledProduct.id);
        assertEquals("Location A", selledProduct.location);
    }

    @Test
    public void testFindAll() {
        SelledProduct.save(client, 3L, 3L, 3L, "Location C", 3L, 3L).await().indefinitely();
        Multi<SelledProduct> result = SelledProduct.findAll(client);
        List<SelledProduct> selledProducts = result.collect().asList().await().indefinitely();
        assertEquals(3, selledProducts.size());
    }

    @Test
    public void testUpdate() {
        SelledProduct.save(client, 4L, 4L, 4L, "Location D", 4L, 4L).await().indefinitely();
        Uni<Boolean> result = SelledProduct.update(client, 1L, 5L, 5L, 5L, "Updated Location", 5L, 5L);
        assertTrue(result.await().indefinitely());

        SelledProduct updatedProduct = SelledProduct.findById(client, 1L).await().indefinitely();
        assertNotNull(updatedProduct);
        assertEquals("Updated Location", updatedProduct.location);
    }

    @Test
    public void testDelete() {
        SelledProduct.save(client, 5L, 5L, 5L, "Location E", 5L, 5L).await().indefinitely();
        Uni<Boolean> result = SelledProduct.delete(client, 1L);
        assertTrue(result.await().indefinitely());

        SelledProduct deletedProduct = SelledProduct.findById(client, 1L).await().indefinitely();
        assertNull(deletedProduct);
    }
}
