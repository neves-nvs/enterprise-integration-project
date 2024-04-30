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

public class CrossSellingRecomendationResource {

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
				.query("DROP TABLE IF EXISTS CrossSellingRecommendations")
				.execute()
				.flatMap(r -> client.query(
						"CREATE TABLE CrossSellingRecommendations (id SERIAL PRIMARY KEY, LoyaltyCardId BIGINT UNSIGNED)")
						.execute())
				.flatMap(r -> client.query(
						"INSERT INTO CrossSellingRecommendations (LoyaltyCardId) VALUES(1)")
						.execute())
				.await()
				.indefinitely();
	}

	@GET
	public Multi<CrossSellingRecommendation> get() {
		return CrossSellingRecommendation.findAll(client);
	}

	@GET
	@Path("{id}")
	public Uni<Response> getSingle(Long id) {
		return CrossSellingRecommendation.findById(client, id)
				.onItem()
				.transform(discountCoupon -> discountCoupon != null ? Response.ok(discountCoupon)
						: Response.status(Response.Status.NOT_FOUND))
				.onItem().transform(ResponseBuilder::build);
	}

	@POST
	public Uni<Response> create(CrossSellingRecommendation discountCoupon) {
		return discountCoupon
				.save(client, discountCoupon.idLoyaltyCard)
				.onItem().transform(id -> URI.create("/discountCoupon/" + id))
				.onItem().transform(uri -> Response.created(uri).build());
	}

	@DELETE
	@Path("{id}")
	public Uni<Response> delete(Long id) {
		return CrossSellingRecommendation.delete(client, id)
				.onItem()
				.transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
				.onItem().transform(status -> Response.status(status).build());
	}

	@PUT
	@Path("/{id}/{idLoyaltyCard}")
	public Uni<Response> update(Long id, Long idLoyaltyCard) {
		return CrossSellingRecommendation.update(client, id, idLoyaltyCard)
				.onItem()
				.transform(updated -> updated ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
				.onItem().transform(status -> Response.status(status).build());
	}

}