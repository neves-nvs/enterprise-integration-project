package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import java.sql.Timestamp;

public class SelledProduct {

    private Long id;
    private Long idPurchase;
    private Long idCustomer;
    private Long idShop;
    private Long idLoyaltycard;
    private String product;
    private Float price;
    private String location;

    public SelledProduct(Long id, Long idPurchase, Long idCustomer, Long idShop, Long idLoyaltycard, String product,
            Float price, String location) {
        this.id = id;
        this.idPurchase = idPurchase;
        this.idCustomer = idCustomer;
        this.idShop = idShop;
        this.idLoyaltycard = idLoyaltycard;
        this.product = product;
        this.price = price;
        this.location = location;
    }

    @Override
    public String toString() {
        return "{id=" + id + ", idPurchase=" + idPurchase + ", idCustomer=" + idCustomer + ", idShop=" + idShop
                + ", idLoyaltycard=" + idLoyaltycard + ", product=" + product + ", price=" + price
                + ", location=" + location + "}";
    }

    public static Uni<SelledProduct> findById(MySQLPool client, Long id) {
        return client.preparedQuery(
                "SELECT id, idPurchase, idCustomer, idShop, idLoyaltycard, product, price, location FROM SelledProduct WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Boolean> save(MySQLPool client, Long idPurchase_R, Long idCustomer_R, Long idShop_R,
            Long idLoyaltycard_R, String product_R, Float price_R, String location_R) {
        return client.preparedQuery(
                "INSERT INTO SelledProduct(idPurchase, idCustomer, idShop, idLoyaltycard, product, price, location) VALUES (?,?,?,?,?,?,?)")
                .execute(
                        Tuple.of(idPurchase_R, idCustomer_R, idShop_R, idLoyaltycard_R, product_R, price_R, location_R))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> delete(MySQLPool client, Long id) {
        return client.preparedQuery("DELETE FROM SelledProduct WHERE id = ?").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> update(MySQLPool client, Long id, Long idPurchase, Long idCustomer, Long idShop,
            Long idLoyaltycard, String product, Float price, String location) {
        return client.preparedQuery(
                "UPDATE SelledProduct SET idPurchase = ?, idCustomer = ?, idShop = ?, idLoyaltycard = ?, product = ?, price = ?, location = ? WHERE id = ?")
                .execute(Tuple.of(idPurchase, idCustomer, idShop, idLoyaltycard, product, price, location,
                        id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    private static SelledProduct from(Row row) {
        return new SelledProduct(
                row.getLong("id"),
                row.getLong("idPurchase"),
                row.getLong("idCustomer"),
                row.getLong("idShop"),
                row.getLong("idLoyaltycard"),
                row.getString("product"),
                row.getFloat("price"),
                row.getString("location"));
    }

}
