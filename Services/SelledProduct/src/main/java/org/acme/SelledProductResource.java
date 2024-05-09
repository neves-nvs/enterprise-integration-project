package org.acme;

import java.net.URI;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Path("SelledProduct")
public class SelledProductResource {

	@Inject
	io.vertx.mutiny.mysqlclient.MySQLPool client;

	@Inject
	@ConfigProperty(name = "myapp.schema.create", defaultValue = "true")
	boolean schemaCreate;

	@ConfigProperty(name = "kafka.bootstrap.servers")
	String kafkaServers;

	void config(@Observes StartupEvent ev) {
		if (schemaCreate) {
			initdb();
		}
	}

	private void initdb() {
		// In a production environment this configuration SHOULD NOT be used
		client.query("DROP TABLE IF EXISTS SelledProducts")
				.execute()
				.flatMap(r -> client.query("CREATE TABLE SelledProducts ("
						+ "id SERIAL PRIMARY KEY, "
						+ "PurchaseId BIGINT UNSIGNED NOT NULL, " // Composition
						// Inheritance + "DiscountCouponID BIGINT UNSIGNED, " // byCoupon
						+ "DiscountCouponId BIGINT UNSIGNED, "
						+ "CustomerId BIGINT UNSIGNED, " // byCustomer
						+ "Location TEXT, " // byLocation
						+ "LoyaltyCardId BIGINT UNSIGNED," // byLoyaltyCard
						+ "ShopId BIGINT UNSIGNED)" // byShop
				).execute())
				.flatMap(r -> client.query(
						"INSERT INTO SelledProducts (PurchaseId,DiscountCouponId,CustomerId,Location,LoyaltyCardId,ShopId) VALUES (1,1,1,'Lisbon',1,1)")
						.execute())
				.flatMap(r -> client.query(
						"INSERT INTO SelledProducts (PurchaseId,DiscountCouponId,CustomerId,Location,LoyaltyCardId,ShopId) VALUES (2,2,2,'Setúbal',2,2)")
						.execute())
				.await().indefinitely();
	}

	@GET
	public Multi<SelledProduct> get() {
		return SelledProduct.findAll(client);
	}

	@GET
	@Path("{id}")
	public Uni<Response> getSingle(Long id) {
		return SelledProduct.findById(client, id)
				.onItem()
				.transform(selledProduct -> selledProduct != null ? Response.ok(selledProduct)
						: Response.status(Response.Status.NOT_FOUND))
				.onItem().transform(ResponseBuilder::build);
	}

	@POST
	public Uni<Response> create(SelledProduct selledProduct) {
		System.out.println("SelledProductResource.create() - selledProduct: " + selledProduct);
		return SelledProduct
				.save(client, selledProduct.idPurchase, selledProduct.idDiscountCoupon, selledProduct.idCustomer,
						selledProduct.location, selledProduct.idLoyaltyCard, selledProduct.idShop)
				.onItem().invoke(() -> {
					DynamicKafkaProducer producer = new DynamicKafkaProducer(kafkaServers);
					producer.run(selledProduct);
				})
				.onItem().transform(id -> URI.create("/SelledProduct/" + id))
				.onItem().transform(uri -> Response.created(uri).build())
				.onItem()
				.transform(response -> Response.status(Response.Status.CREATED).entity(response.getEntity()).build());

	}

	@DELETE
	@Path("{id}")
	public Uni<Response> delete(Long id) {
		return SelledProduct.delete(client, id)
				.onItem()
				.transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
				.onItem().transform(status -> Response.status(status).build());
	}

	@PUT
	@Path("/{id}/{idPurchase}/{idDiscountCoupon}/{idCustomer}/{location}/{idLoyaltyCard}/{idShop}")
	public Uni<Response> update(Long id, Long idPurchase, Long idDiscountCoupon, Long idCustomer, String location,
			Long idLoyaltyCard, Long idShop) {
		return SelledProduct
				.update(client, id, idPurchase, idDiscountCoupon, idCustomer, location, idLoyaltyCard, idShop)
				.onItem().transform(updated -> updated ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
				.onItem().transform(status -> Response.status(status).build());
	}

}
