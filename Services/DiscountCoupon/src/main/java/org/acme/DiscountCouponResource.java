package org.acme;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import java.net.URI;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Path("DiscountCoupon")
public class DiscountCouponResource {

	@Channel("DiscountCoupon")
	Emitter<DiscountCoupon> emitter;

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
		client
				.query("DROP TABLE IF EXISTS DiscountCoupons")
				.execute()
				.flatMap(r -> client.query(
						"CREATE TABLE DiscountCoupons (id SERIAL PRIMARY KEY, LoyaltyCardId BIGINT UNSIGNED, DiscountType INT UNSIGNED, ExpiryDate DATETIME)")
						.execute())
				.flatMap(r -> client.query(
						"INSERT INTO DiscountCoupons (LoyaltyCardId, DiscountType, ExpiryDate) VALUES(1, 1, '2024-01-19')")
						.execute())
				.await()
				.indefinitely();
	}

	@GET
	public Multi<DiscountCoupon> get() {
		return DiscountCoupon.findAll(client);
	}

	@GET
	@Path("{id}")
	public Uni<Response> getSingle(Long id) {
		return DiscountCoupon.findById(client, id)
				.onItem()
				.transform(discountCoupon -> discountCoupon != null ? Response.ok(discountCoupon)
						: Response.status(Response.Status.NOT_FOUND))
				.onItem().transform(ResponseBuilder::build);
	}

	@POST
	public Uni<Response> create(DiscountCoupon discountCoupon) {
		return discountCoupon
				.save(client, discountCoupon.idLoyaltyCard, discountCoupon.discountType, discountCoupon.expiryDate)
				.onItem().invoke(() -> emitter.send(discountCoupon))
				.onItem().transform(id -> URI.create("/discountCoupon/" + id))
				.onItem().transform(uri -> Response.created(uri).build())
				.onItem()
				.transform(response -> Response.status(Response.Status.CREATED).entity(response.getEntity()).build());

	}

	@DELETE
	@Path("{id}")
	public Uni<Response> delete(Long id) {
		return DiscountCoupon.delete(client, id)
				.onItem()
				.transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
				.onItem().transform(status -> Response.status(status).build());
	}

	@PUT
	@Path("/{id}/{idLoyaltyCard}/{discountType}/{expiryDate}")
	public Uni<Response> update(Long id, Long idLoyaltyCard, Integer discountType, String expiryDate) {
		return DiscountCoupon.update(client, id, idLoyaltyCard, discountType, expiryDate)
				.onItem()
				.transform(updated -> updated ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
				.onItem().transform(status -> Response.status(status).build());
	}

}
