package org.acme;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.is;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Collections;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestExtension;
import io.restassured.http.ContentType;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import jakarta.inject.Inject;

@QuarkusTest
@Testcontainers
@ExtendWith(QuarkusTestExtension.class)
public class ShopResourceTest {

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
  void testGet() {
    insertTestShop("Apple", "New York");
    given()
        .when().get("/Shop")
        .then()
        .statusCode(200);
    // .body("$.size()", is(1));
  }

  @Test
  void testGetSingle() {
    Long id = insertTestShop("Apple", "New York");
    given()
        .when().get("/Shop/" + id)
        .then()
        .statusCode(200)
        .body("id", is(id.intValue()));
  }

  @Test
  @Description("Test the creation of a discount coupon")
  void testCreate() {
    Shop shop = new Shop(1L, "Apple", "New York");
    given()
        .contentType(ContentType.JSON)
        .body(shop)
        .when().post("/Shop")
        .then()
        .statusCode(201);
    // .header("Location", containsString("/discountCoupon/"));

    // DiscountCoupon receivedRecord = DiscountCoupon.from(new
    // JsonObject(record.value()));
    // assertEquals(coupon, receivedRecord);
  }

  @Test
  void testDelete() {
    Long id = insertTestShop("Samsung", "Los Angeles");
    given()
        .when().delete("/Shop/" + id)
        .then()
        .statusCode(204);
  }

  @Test
  void testUpdate() {
    Long id = insertTestShop("Samsung", "Los Angeles");
    given()
        .when().put("/Shop/" + id + "/" + "Huawei" + "/" + "San Francisco")
        .then()
        .statusCode(204);
  }

  private Long insertTestShop(String name, String location) {
    client.preparedQuery("INSERT INTO Shops (name, location) VALUES (?,?)")
        .execute(io.vertx.mutiny.sqlclient.Tuple.of(name, location))
        .await().indefinitely();
    return client.query("SELECT LAST_INSERT_ID()")
        .execute()
        .onItem().transform(rowSet -> rowSet.iterator().next().getLong(0))
        .await().indefinitely();
  }
}
