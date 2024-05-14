package org.acme;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(MySQLTestResource.class)
class CustomerTest {

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
    public void testSaveCustomer() {
        Customer customer = new Customer();
        Uni<Boolean> result = customer.save(client, "John Doe", 123456789L, "New York");

        assertTrue(result.await().indefinitely());

        Customer savedCustomer = Customer.findById(client, 1L).await().indefinitely();
        assertNotNull(savedCustomer);
        assertEquals("John Doe", savedCustomer.name);
        assertEquals(123456789L, savedCustomer.FiscalNumber);
        assertEquals("New York", savedCustomer.location);
    }

    @Test
    public void testFindAllCustomers() {
        client.preparedQuery("INSERT INTO Customers (name, FiscalNumber, location) VALUES (?,?,?)")
                .execute(Tuple.of("John Doe", 123456789L, "New York")).await().indefinitely();
        client.preparedQuery("INSERT INTO Customers (name, FiscalNumber, location) VALUES (?,?,?)")
                .execute(Tuple.of("Jane Doe", 987654321L, "Los Angeles")).await().indefinitely();

        List<Customer> customers = Customer.findAll(client).collect().asList().await().indefinitely();

        assertNotNull(customers);
        assertEquals(2, customers.size());
        assertEquals("John Doe", customers.get(0).name);
        assertEquals("Jane Doe", customers.get(1).name);
    }

    @Test
    public void testFindCustomerById() {
        client.preparedQuery("INSERT INTO Customers (name, FiscalNumber, location) VALUES (?,?,?)")
                .execute(Tuple.of("John Doe", 123456789L, "New York")).await().indefinitely();

        Customer customer = Customer.findById(client, 1L).await().indefinitely();

        assertNotNull(customer);
        assertEquals("John Doe", customer.name);
        assertEquals(123456789L, customer.FiscalNumber);
        assertEquals("New York", customer.location);
    }

    @Test
    public void testUpdateCustomer() {
        client.preparedQuery("INSERT INTO Customers (name, FiscalNumber, location) VALUES (?,?,?)")
                .execute(Tuple.of("John Doe", 123456789L, "New York")).await().indefinitely();

        Uni<Boolean> result = Customer.update(client, 1L, "John Smith", 987654321L, "San Francisco");

        assertTrue(result.await().indefinitely());

        Customer updatedCustomer = Customer.findById(client, 1L).await().indefinitely();
        assertNotNull(updatedCustomer);
        assertEquals("John Smith", updatedCustomer.name);
        assertEquals(987654321L, updatedCustomer.FiscalNumber);
        assertEquals("San Francisco", updatedCustomer.location);
    }

    @Test
    public void testDeleteCustomer() {
        client.preparedQuery("INSERT INTO Customers (name, FiscalNumber, location) VALUES (?,?,?)")
                .execute(Tuple.of("John Doe", 123456789L, "New York")).await().indefinitely();

        Uni<Boolean> result = Customer.delete(client, 1L);

        assertTrue(result.await().indefinitely());

        Customer deletedCustomer = Customer.findById(client, 1L).await().indefinitely();
        assertNull(deletedCustomer);
    }
}
