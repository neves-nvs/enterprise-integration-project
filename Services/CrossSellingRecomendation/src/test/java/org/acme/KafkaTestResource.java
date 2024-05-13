package org.acme;

import java.util.Map;
import java.util.HashMap;

import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class KafkaTestResource implements QuarkusTestResourceLifecycleManager {

    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("apache/kafka:latest"));

    @Override
    public Map<String, String> start() {
        kafkaContainer.start();

        Map<String, String> config = new HashMap<>();
        config.put("kafka.bootstrap.servers", kafkaContainer.getBootstrapServers());
        return config;
    }

    @Override
    public void stop() {
        if (kafkaContainer != null) {
            kafkaContainer.stop();
        }
    }

}
