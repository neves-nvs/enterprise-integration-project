package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestExtension;
import io.restassured.http.ContentType;
import io.vertx.core.cli.annotations.Description;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import jakarta.inject.Inject;

@QuarkusTest
@Testcontainers
@ExtendWith(QuarkusTestExtension.class)
public class CustomerResourceTest {

  @Inject
  MySQLPool client;

  @BeforeEach
  public void setupDatabase() {
    client.query(
        "CREATE TABLE IF NOT EXISTS Customers (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), FiscalNumber BIGINT, location VARCHAR(255))")
        .execute().await().indefinitely();
    client.query("TRUNCATE TABLE Customers").execute().await().indefinitely();
  }

  @Test
  void testGet() {
    insertTestCustomer("John Doe", "New York", 123456789L);
    given()
        .when().get("/Customer")
        .then()
        .statusCode(200);
    // .body("$.size()", is(1));
  }

  @Test
  void testGetSingle() {
    Long id = insertTestCustomer("John Doe", "New York", 123456789L);
    given()
        .when().get("/Customer/" + id)
        .then()
        .statusCode(200)
        .body("id", is(id.intValue()));
  }

  @Test
  @Description("Test the creation of a discount coupon")
  void testCreate() {
    Customer customer = new Customer(1L, "John Doe", "New York", 123456789L);
    given()
        .contentType(ContentType.JSON)
        .body(customer)
        .when().post("/Customer")
        .then()
        .statusCode(201);
    // .header("Location", containsString("/discountCoupon/"));

    // DiscountCoupon receivedRecord = DiscountCoupon.from(new
    // JsonObject(record.value()));
    // assertEquals(coupon, receivedRecord);
  }

  @Test
  void testDelete() {
    Long id = insertTestCustomer("Jane Doe", "Los Angeles", 987654321L);
    given()
        .when().delete("/Customer/" + id)
        .then()
        .statusCode(204);
  }

  @Test
  void testUpdate() {
    Long id = insertTestCustomer("Jane Doe", "Los Angeles", 987654321L);
    given()
        .when().put("/Customer/" + id + "/" + "John Smith" + "/" + 987654321L + "/" + "San Francisco")
        .then()
        .statusCode(204);
  }

  private Long insertTestCustomer(String name, String location, Long FiscalNumber) {
    client.preparedQuery("INSERT INTO Customers (name, FiscalNumber, location) VALUES (?,?,?)")
        .execute(io.vertx.mutiny.sqlclient.Tuple.of(name, FiscalNumber, location))
        .await().indefinitely();
    return client.query("SELECT LAST_INSERT_ID()")
        .execute()
        .onItem().transform(rowSet -> rowSet.iterator().next().getLong(0))
        .await().indefinitely();
  }
}
