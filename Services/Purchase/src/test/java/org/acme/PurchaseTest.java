package org.acme;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;

import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.inject.Inject;
import java.util.List;

@QuarkusTest
@QuarkusTestResource(MySQLTestResource.class)
public class PurchaseTest {

    // @Inject
    // String kafkaBootstrapServers;

    @Inject
    MySQLPool client;

    @BeforeEach
    public void setup() {
        client.query(
                "CREATE TABLE IF NOT EXISTS Purchases (id INT PRIMARY KEY AUTO_INCREMENT, DateTime DATETIME, Price FLOAT, Product VARCHAR(255), Supplier VARCHAR(255), shopname VARCHAR(255), loyaltycardid BIGINT)")
                .execute().await().indefinitely();
        client.query("TRUNCATE TABLE Purchases").execute().await().indefinitely();
    }

    @Test
    public void testFindAll() {
        insertTestPurchase().await().indefinitely();

        Multi<Purchase> purchases = Purchase.findAll(client);
        List<Purchase> purchaseList = purchases.collect().asList().await().indefinitely();
        assertNotNull(purchaseList);
        // assertEquals(0, purchaseList.size());
    }

    @Test
    public void testFindById() {
        insertTestPurchase().await().indefinitely();

        Uni<Purchase> purchase = Purchase.findById(client, 1L);
        Purchase result = purchase.await().indefinitely();

        assertNotNull(result);
        assertEquals(1L, result.id);
    }

    private Uni<Long> insertTestPurchase() {
        return client.query(
                "INSERT INTO Purchases (DateTime, Price, Product, Supplier, shopname, loyaltycardid) VALUES ('2038-01-19 03:14:07', 12.34, 'one product', 'supplier', 'arco cego', 1)")
                .execute()
                .onItem().transformToUni(rows -> client.query("SELECT LAST_INSERT_ID() AS id").execute())
                .onItem().transform(rowSet -> {
                    Row row = rowSet.iterator().next();
                    return row.getLong("id");
                });
    }
}
