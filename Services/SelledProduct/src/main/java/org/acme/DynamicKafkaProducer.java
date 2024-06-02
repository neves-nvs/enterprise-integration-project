package org.acme;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.apache.kafka.clients.consumer.KafkaConsumer;

public class DynamicKafkaProducer {

    @Inject
    @ConfigProperty(name = "kafka.bootstrap.servers")
    public String kafkaServers;

    private static String TOPIC_BY_DISCOUNTCOUPON = "SelledProductByDiscountCoupon";
    private static String TOPIC_BY_CUSTOMER = "SelledProductByCustomer";
    private static String TOPIC_BY_LOCATION = "SelledProductByLocation";
    private static String TOPIC_BY_LOYALTYCARD = "SelledProductByLoyaltyCard";
    private static String TOPIC_BY_SHOP = "SelledProductByShop";

    DynamicKafkaProducer(String kafkaServers) {
        this.kafkaServers = kafkaServers;
    }

    public ArrayList<String> getTopicsByObject(SelledProduct selledProduct) {
        ArrayList<String> topics = new ArrayList<>();

        if (selledProduct.idDiscountCoupon != null) {
            topics.add(TOPIC_BY_DISCOUNTCOUPON);
        }
        if (selledProduct.idCustomer != null) {
            topics.add(TOPIC_BY_CUSTOMER);
        }
        if (selledProduct.location != null) {
            topics.add(TOPIC_BY_LOCATION);
        }
        if (selledProduct.idLoyaltyCard != null) {
            topics.add(TOPIC_BY_LOYALTYCARD);
        }
        if (selledProduct.idShop != null) {
            topics.add(TOPIC_BY_SHOP);
        }
        return topics;
    }

    public void run(SelledProduct selledProduct) {
        System.out.println("Kafka servers: " + kafkaServers);
        Properties properties = new Properties();
        System.out.println("Kafka servers: " + kafkaServers);
        properties.put("bootstrap.servers", kafkaServers);
        properties.put("connections.max.idle.ms", 10000);
        properties.put("request.timeout.ms", 5000);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        ArrayList<String> topics = getTopicsByObject(selledProduct);

        topics.forEach(topic -> {
            // TODO use worker instead of blocking the main thread
            // TODO use client.listTopics() to check if topic exists
            try (AdminClient client = AdminClient.create(properties)) {
                CreateTopicsResult result = client.createTopics(List.of(new NewTopic(topic, 1, (short) 1)));
                result.all().get(); // This will throw an exception if the topic already exists, which we ignore
            } catch (Exception exc) {
                // TODO handle exception TopicExistsException
                send(producer, selledProduct, topic);
            }
            // catch (ExecutionException | InterruptedException e) {
            // // throw new RuntimeException("Failed to create Kafka topic", e);
            // }
        });

        producer.close();
    }

    public void send(KafkaProducer<String, String> producer, SelledProduct selledProduct, String topic) {
        try {
            System.out.println("Sending message to Kafka: " + selledProduct.toString() + " on topic " + topic);
            String jsonData = new ObjectMapper().writeValueAsString(selledProduct);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, jsonData);

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Error sending message to Kafka: " + exception.getMessage());
                } else {
                    System.out
                            .println("Message sent to topic " + metadata.topic() + " with offset " + metadata.offset());
                }
            });
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing SelledProduct: " + e.getMessage());
        }
    }

    public String CreateNewTopicIfNeed(Topic topic) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaServers);
        properties.put("connections.max.idle.ms", 10000);
        properties.put("request.timeout.ms", 5000);

        try (AdminClient client = AdminClient.create(properties)) {
            CreateTopicsResult result = client.createTopics(List.of(
                    new NewTopic(topic.TopicName, 1, (short) 1)));
            try {
                result.all().get();
            } catch (TopicExistsException exc) {
                throw exc;
            } catch (ExecutionException | InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return ("New Topic created = " + topic);
    }

    public String RemoveOneTopic(Topic topic) {
        Properties properties = new Properties();
        properties.put("kafka.bootstrap.servers", kafkaServers);
        properties.put("connections.max.idle.ms", 10000);
        properties.put("request.timeout.ms", 5000);
        try (AdminClient client = AdminClient.create(properties)) {
            DeleteTopicsResult result = client.deleteTopics(Collections.singletonList(topic.TopicName));
            try {
                result.all().get();
            } catch (org.apache.kafka.common.errors.UnknownTopicOrPartitionException exc) {
                throw exc;
            } catch (ExecutionException | InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return ("New Topic deleted = " + topic);
    }

    public Map<String, Long> countAllTopics() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaServers);
        properties.put("group.id", "my-group");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        List<String> topics = Arrays.asList(TOPIC_BY_DISCOUNTCOUPON, TOPIC_BY_CUSTOMER, TOPIC_BY_LOCATION,
                TOPIC_BY_LOYALTYCARD, TOPIC_BY_SHOP);

        Map<String, Long> topicRecordCountMap = new HashMap<>();
        topicRecordCountMap.put(TOPIC_BY_DISCOUNTCOUPON, 0L);
        topicRecordCountMap.put(TOPIC_BY_CUSTOMER, 0L);
        topicRecordCountMap.put(TOPIC_BY_LOCATION, 0L);
        topicRecordCountMap.put(TOPIC_BY_LOYALTYCARD, 0L);
        topicRecordCountMap.put(TOPIC_BY_SHOP, 0L);

        for (String topic : topics) {
            consumer.subscribe(Collections.singletonList(topic));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
            topicRecordCountMap.put(topic, (long) records.count());
            consumer.unsubscribe();
        }

        consumer.close();

        return topicRecordCountMap;
    }
}
