package org.acme;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public class MySQLTestResource implements QuarkusTestResourceLifecycleManager {

    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:latest"));

    @Override
    public Map<String, String> start() {
        mySQLContainer.withDatabaseName("DiscountCoupons")
                .withUsername("teste")
                .withPassword("testeteste");
        mySQLContainer.start();

        Map<String, String> config = new HashMap<>();
        config.put("quarkus.datasource.reactive.url", String.format("mysql://%s:%d/%s",
                mySQLContainer.getHost(),
                mySQLContainer.getMappedPort(3306),
                mySQLContainer.getDatabaseName()));
        config.put("quarkus.datasource.username", mySQLContainer.getUsername());
        config.put("quarkus.datasource.password", mySQLContainer.getPassword());

        return config;
    }

    @Override
    public void stop() {
        if (mySQLContainer != null) {
            mySQLContainer.stop();
        }
    }
}
