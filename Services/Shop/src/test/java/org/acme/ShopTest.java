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
public class ShopTest {

    @Inject
    MySQLPool client;

    @BeforeEach
    public void setupDatabase() {
        client.query(
                "CREATE TABLE IF NOT EXISTS Shops (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), location VARCHAR(255))")
                .execute().await().indefinitely();
        client.query("TRUNCATE TABLE Shops").execute().await().indefinitely();
    }

    @Test
    public void testSaveShop() {
        Shop shop = new Shop();
        Uni<Boolean> result = shop.save(client, "Apple", "New York");

        assertTrue(result.await().indefinitely());

        Shop savedShop = Shop.findById(client, 1L).await().indefinitely();
        assertNotNull(savedShop);
        assertEquals("Apple", savedShop.name);
        assertEquals("New York", savedShop.location);
    }

    @Test
    public void testFindAllShops() {
        client.preparedQuery("INSERT INTO Shops (name, location) VALUES (?,?)")
                .execute(Tuple.of("Apple", "New York")).await().indefinitely();
        client.preparedQuery("INSERT INTO Shops (name, location) VALUES (?,?)")
                .execute(Tuple.of("Samsung", "Los Angeles")).await().indefinitely();

        List<Shop> shops = Shop.findAll(client).collect().asList().await().indefinitely();

        assertNotNull(shops);
        assertEquals(2, shops.size());
        assertEquals("Apple", shops.get(0).name);
        assertEquals("Samsung", shops.get(1).name);
    }

    @Test
    public void testFindShopById() {
        client.preparedQuery("INSERT INTO Shops (name, location) VALUES (?,?)")
                .execute(Tuple.of("Apple", "New York")).await().indefinitely();

        Shop shop = Shop.findById(client, 1L).await().indefinitely();

        assertNotNull(shop);
        assertEquals("Apple", shop.name);
        assertEquals("New York", shop.location);
    }

    @Test
    public void testUpdateShop() {
        client.preparedQuery("INSERT INTO Shops (name, location) VALUES (?,?)")
                .execute(Tuple.of("Apple", "New York")).await().indefinitely();

        Uni<Boolean> result = Shop.update(client, 1L, "Samsung", "San Francisco");

        assertTrue(result.await().indefinitely());

        Shop updatedShop = Shop.findById(client, 1L).await().indefinitely();
        assertNotNull(updatedShop);
        assertEquals("Samsung", updatedShop.name);
        assertEquals("San Francisco", updatedShop.location);
    }

    @Test
    public void testDeleteShop() {
        client.preparedQuery("INSERT INTO Shops (name, location) VALUES (?,?)")
                .execute(Tuple.of("Apple", "New York")).await().indefinitely();

        Uni<Boolean> result = Shop.delete(client, 1L);

        assertTrue(result.await().indefinitely());

        Shop deletedShop = Shop.findById(client, 1L).await().indefinitely();
        assertNull(deletedShop);
    }
}
