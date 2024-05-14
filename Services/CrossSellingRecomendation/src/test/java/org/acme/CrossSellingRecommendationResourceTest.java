package org.acme;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.is;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Collections;

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
public class CrossSellingRecommendationResourceTest {

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
        "CREATE TABLE IF NOT EXISTS CrossSellingRecommendations (id SERIAL PRIMARY KEY, LoyaltyCardId BIGINT UNSIGNED)")
        .execute().await().indefinitely();
    client.query("TRUNCATE TABLE CrossSellingRecommendations").execute().await().indefinitely();
  }

  @Test
  void testGet() {
    insertTestRecommendation(100L);
    given()
        .when().get("/CrossSellingRecommendation")
        .then()
        .statusCode(200);
    // .body("$.size()", is(1));
  }

  @Test
  void testGetSingle() {
    Long id = insertTestRecommendation(100L);
    given()
        .when().get("/CrossSellingRecommendation/" + id)
        .then()
        .statusCode(200)
        .body("id", is(id.intValue()));
  }

  @Test
  @Description("Test the creation of a discount coupon")
  void testCreate() {
    CrossSellingRecommendation recommendation = new CrossSellingRecommendation(1L, 101L);
    given()
        .contentType(ContentType.JSON)
        .body(recommendation)
        .when().post("/CrossSellingRecommendation")
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
      consumer.subscribe(Collections.singletonList("CrossSellingRecommendation"));
      ConsumerRecord<String, String> record = consumer.poll(Duration.ofSeconds(10)).iterator().next();

      assertNotNull(record);
      // DiscountCoupon receivedRecord = DiscountCoupon.from(new
      // JsonObject(record.value()));
      // assertEquals(coupon, receivedRecord);
    }
  }

  @Test
  void testDelete() {
    Long id = insertTestRecommendation(102L);
    given()
        .when().delete("/CrossSellingRecommendation/" + id)
        .then()
        .statusCode(204);
  }

  @Test
  void testUpdate() {
    Long id = insertTestRecommendation(103L);
    given()
        .when().put("/CrossSellingRecommendation/" + id + "/" + 104L)
        .then()
        .statusCode(204);
  }

  private Long insertTestRecommendation(Long loyaltyCardId) {
    client.preparedQuery("INSERT INTO CrossSellingRecommendations(LoyaltyCardId) VALUES (?)")
        .execute(io.vertx.mutiny.sqlclient.Tuple.of(loyaltyCardId))
        .await().indefinitely();
    return client.query("SELECT LAST_INSERT_ID()")
        .execute()
        .onItem().transform(rowSet -> rowSet.iterator().next().getLong(0))
        .await().indefinitely();
  }
}
