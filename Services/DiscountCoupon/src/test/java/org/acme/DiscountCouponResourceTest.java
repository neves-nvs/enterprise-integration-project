package org.acme;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.is;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Collections;

import org.acme.DiscountCoupon.DiscountType;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
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
@QuarkusTestResource(KafkaTestResource.class)
public class DiscountCouponResourceTest {

  @Inject
  @ConfigProperty(name = "kafka.bootstrap.servers")
  String kafkaBootstrapServers;

  @Inject
  MySQLPool client;

  @BeforeAll
  public static void setup() {
    System.getProperty("kafka.bootstrap.servers", "${KAFKA_BROKER_URL}:9092");
  }

  @BeforeEach
  public void setupDatabase() {
    client.query(
        "CREATE TABLE IF NOT EXISTS DiscountCoupons (id SERIAL PRIMARY KEY, LoyaltyCardId BIGINT UNSIGNED, DiscountType INT UNSIGNED, ExpiryDate DATETIME)")
        .execute().await().indefinitely();
    client.query("TRUNCATE TABLE DiscountCoupons").execute().await().indefinitely();
  }

  @Test
  void testGet() {
    insertTestCoupon(100L, 1, LocalDateTime.now().plusDays(10));
    given()
        .when().get("/DiscountCoupon")
        .then()
        .statusCode(200);
    // .body("$.size()", is(1));
  }

  @Test
  void testGetSingle() {
    Long id = insertTestCoupon(100L, DiscountType.AMOUNT_OFF.ordinal(), LocalDateTime.now().plusDays(10));
    given()
        .when().get("/DiscountCoupon/" + id)
        .then()
        .statusCode(200)
        .body("id", is(id.intValue()));
  }

  @Test
  @Description("Test the creation of a discount coupon")
  void testCreate() {
    DiscountCoupon coupon = new DiscountCoupon(1L, 101L, DiscountCoupon.DiscountType.PERCENTAGE_OFF,
        LocalDateTime.now().plusDays(10));
    given()
        .contentType(ContentType.JSON)
        .body(coupon)
        .when().post("/DiscountCoupon")
        .then()
        .statusCode(201);
    // .header("Location", containsString("/discountCoupon/"));

    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(Collections.singletonList("DiscountCoupon"));
      ConsumerRecord<String, String> record = consumer.poll(Duration.ofSeconds(10)).iterator().next();

      assertNotNull(record);
      // DiscountCoupon receivedRecord = DiscountCoupon.from(new
      // JsonObject(record.value()));
      // assertEquals(coupon, receivedRecord);
    }
  }

  @Test
  void testDelete() {
    Long id = insertTestCoupon(102L, 2, LocalDateTime.now().plusDays(10));
    given()
        .when().delete("/DiscountCoupon/" + id)
        .then()
        .statusCode(204);
  }

  @Test
  void testUpdate() {
    Long id = insertTestCoupon(103L, 0, LocalDateTime.now().plusDays(10));
    given()
        .when().put("/DiscountCoupon/" + id + "/104/1/2023-12-31T23:59:59")
        .then()
        .statusCode(204);
  }

  private Long insertTestCoupon(Long loyaltyCardId, int discountType, LocalDateTime expiryDate) {
    client.preparedQuery("INSERT INTO DiscountCoupons(LoyaltyCardId, DiscountType, ExpiryDate) VALUES (?,?,?)")
        .execute(io.vertx.mutiny.sqlclient.Tuple.of(loyaltyCardId, discountType, expiryDate))
        .await().indefinitely();
    return client.query("SELECT LAST_INSERT_ID()")
        .execute()
        .onItem().transform(rowSet -> rowSet.iterator().next().getLong(0))
        .await().indefinitely();
  }
}
