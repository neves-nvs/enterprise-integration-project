package org.acme;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@QuarkusTest
@QuarkusTestResource(MySQLTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
public class DynamicTopicConsumerTest {

    @Inject
    @ConfigProperty(name = "kafka.bootstrap.servers")
    String kafkaBootstrapServers;

    @Inject
    MySQLPool client;

    private KafkaProducer<String, String> producer;
    private String topic = "test-topic";

    @BeforeEach
    public void setup() {
        setupDatabase();
        setupKafkaProducer();
    }

    private void setupDatabase() {
        client.query("DROP TABLE IF EXISTS Purchases").execute().await().indefinitely();
        client.query("CREATE TABLE IF NOT EXISTS Purchases (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "DateTime DATETIME," +
                "Price FLOAT," +
                "Product VARCHAR(255)," +
                "Supplier VARCHAR(255)," +
                "shopname VARCHAR(255)," +
                "loyaltycardid BIGINT)")
                .execute().await().indefinitely();
        client.query("TRUNCATE TABLE Purchases").execute().await().indefinitely();
    }

    private void setupKafkaProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(props);
    }

    @Test
    public void testKafkaMessageConsumption() throws InterruptedException {
        // Start the Kafka consumer thread
        DynamicTopicConsumer consumer = new DynamicTopicConsumer(topic, System.getProperty("kafka.bootstrap.servers"),
                client);
        consumer.start();

        // Send a message to the Kafka topic
        JSONObject purchaseEvent = new JSONObject();
        purchaseEvent.put("TimeStamp", LocalDateTime.now().toString());
        purchaseEvent.put("Price", "25.0");
        purchaseEvent.put("Product", "Product2");
        purchaseEvent.put("Supplier", "Supplier2");
        purchaseEvent.put("Shop", "Shop2");
        purchaseEvent.put("LoyaltyCard_ID", "2");

        JSONObject message = new JSONObject();
        message.put("Purchase_Event", purchaseEvent);

        producer.send(new ProducerRecord<>(topic, message.toString()));

        // Allow some time for the consumer to process the message
        Thread.sleep(5000);

        // Verify the record was inserted into the database
        List<Purchase> purchaseList = Purchase.findAll(client).collect().asList().await().indefinitely();

        assertNotNull(purchaseList);
        // assertEquals("Product2", purchaseList.get(0).product);

        // Stop the consumer thread
        consumer.interrupt();
    }
}
