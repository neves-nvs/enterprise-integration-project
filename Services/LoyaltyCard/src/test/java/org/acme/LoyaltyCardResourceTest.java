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
public class LoyaltyCardResourceTest {

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
  void testGet() {
    insertTestLoyaltyCard(1L, 3L);
    given()
        .when().get("/Loyaltycard")
        .then()
        .statusCode(200);
    // .body("$.size()", is(1));
  }

  @Test
  void testGetSingle() {
    Long id = insertTestLoyaltyCard(1L, 3L);
    given()
        .when().get("/Loyaltycard/" + id)
        .then()
        .statusCode(200)
        .body("id", is(id.intValue()));
  }

  @Test
  @Description("Test the creation of a discount coupon")
  void testCreate() {
    Loyaltycard card = new Loyaltycard(1L, 1L, 3L);
    given()
        .contentType(ContentType.JSON)
        .body(card)
        .when().post("/Loyaltycard")
        .then()
        .statusCode(201);
    // .header("Location", containsString("/discountCoupon/"));

    // DiscountCoupon receivedRecord = DiscountCoupon.from(new
    // JsonObject(record.value()));
    // assertEquals(coupon, receivedRecord);
  }

  @Test
  void testDelete() {
    Long id = insertTestLoyaltyCard(2L, 4L);
    given()
        .when().delete("/Loyaltycard/" + id)
        .then()
        .statusCode(204);
  }

  @Test
  void testUpdate() {
    Long id = insertTestLoyaltyCard(3L, 5L);
    given()
        .when().put("/Loyaltycard/" + id + "/" + 987654321L + "/" + 50L)
        .then()
        .statusCode(204);
  }

  private Long insertTestLoyaltyCard(Long idCustomer, Long idShop) {
    client.preparedQuery("INSERT INTO LoyaltyCards (idCustomer,idShop) VALUES (?,?)")
        .execute(io.vertx.mutiny.sqlclient.Tuple.of(idCustomer, idShop))
        .await().indefinitely();
    return client.query("SELECT LAST_INSERT_ID()")
        .execute()
        .onItem().transform(rowSet -> rowSet.iterator().next().getLong(0))
        .await().indefinitely();
  }
}
