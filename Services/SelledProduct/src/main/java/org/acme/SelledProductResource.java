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

@Path("SelledProduct")
public class SelledProductResource {

	@Inject
	io.vertx.mutiny.mysqlclient.MySQLPool client;

	@Inject
	@ConfigProperty(name = "myapp.schema.create", defaultValue = "true")
	boolean schemaCreate;

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
						+ "DiscountCouponID BIGINT UNSIGNED, "
						+ "CustomerID BIGINT UNSIGNED, " // byCustomer
						+ "Location TEXT, " // byLocation
						+ "LoyaltyCardID BIGINT UNSIGNED," // byLoyaltyCard
						+ "ShopID BIGINT UNSIGNED)" // byShop
				).execute())
				.flatMap(r -> client.query(
						"INSERT INTO SelledProducts (PurchaseId,DiscountCouponID,CustomerID,Location,LoyaltyCardID,ShopID) VALUES (1,1,1,'Lisbon',1,1)")
						.execute())
				.flatMap(r -> client.query(
						"INSERT INTO SelledProducts (PurchaseId,DiscountCouponID,CustomerID,Location,LoyaltyCardID,ShopID) VALUES (2,2,2,'Setúbal',2,2)")
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
		return SelledProduct
				.save(client, selledProduct.idPurchase, selledProduct.idCoupon, selledProduct.idCustomer,
						selledProduct.location,
						selledProduct.idLoyaltycard, selledProduct.idShop)
				.onItem().transform(id -> URI.create("/SelledProduct/" + id))
				.onItem().transform(uri -> Response.created(uri).build());
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
	@Path("/{id}/{idPurchase}/{idCoupon}/{idCustomer}/{location}/{idLoyaltycard}/{idShop}")
	public Uni<Response> update(Long id, Long idPurchase, Long idCustomer, String location, Long idLoyaltycard,
			Long idShop) {
		return SelledProduct.update(client, id, idPurchase, idCustomer, idShop, idLoyaltycard, location, null, location)
				.onItem().transform(updated -> updated ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
				.onItem().transform(status -> Response.status(status).build());
	}

}
