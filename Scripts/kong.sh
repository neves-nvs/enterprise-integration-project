#!/usr/bin/env bash

set -e
set -u
set -o pipefail

# run from file location
cd "$(dirname "$0")"
cd ..

cd Terraform

# source environment variables

KONG_KONGA_DNS="$(terraform output -raw kong)"

KONG_GATEWAY="http://$KONG_KONGA_DNS:8000"
echo "Kong Gateway: $KONG_GATEWAY"
echo
export KONG_GATEWAY

KONG_ADMIN="http://$KONG_KONGA_DNS:8001"
echo "Kong Admin API: $KONG_ADMIN"
echo
export KONG_ADMIN

KONGA="http://$KONG_KONGA_DNS:1337"
echo "KONGA IS AVAILABLE HERE: $KONGA"
echo

CUSTOMER_SHOP_LOYALTYCARD_DNS=$(terraform output -raw customer_shop_loyaltycard_public_dns)
CUSTOMER_RAW_URL="http://${CUSTOMER_SHOP_LOYALTYCARD_DNS}:8080/"
CUSTOMER_URL="http://${CUSTOMER_SHOP_LOYALTYCARD_DNS}:8080/Customer"
SHOP_RAW_URL="http://${CUSTOMER_SHOP_LOYALTYCARD_DNS}:8081/"
SHOP_URL="http://${CUSTOMER_SHOP_LOYALTYCARD_DNS}:8081/Shop"
LOYALTYCARD_RAW_URL="http://${CUSTOMER_SHOP_LOYALTYCARD_DNS}:8082/"
LOYALTYCARD_URL="http://${CUSTOMER_SHOP_LOYALTYCARD_DNS}:8082/LoyaltyCard"

CROSSSELINGRECOMMENDATION_DNS=$(terraform output -raw crosssellingrecommendation_public_dns)
CROSSSELLINGRECOMMENDATION_URL="http://${CROSSSELINGRECOMMENDATION_DNS}:8080/CrossSellingRecommendation"

DISCOUNTCOUPON_DNS=$(terraform output -raw discountcoupon_public_dns)
DISCOUNTCOUPON_URL="http://${DISCOUNTCOUPON_DNS}:8080/DiscountCoupon"

PURCHASE_DNS=$(terraform output -raw purchase_public_dns)
PURCHASE_RAW_URL="http://${PURCHASE_DNS}:8080/"
PURCHASE_URL="http://${PURCHASE_DNS}:8080/Purchase"

SELLEDPRODUCT_DNS=$(terraform output -raw selledproduct_public_dns)
SELLEDPRODUCT_URL="http://${SELLEDPRODUCT_DNS}:8080/SelledProduct"

KONG_ADMIN_SERVICES="${KONG_ADMIN}/services"

# ---------------------------------------------------------------------------- #
#                                   CUSTOMER                                   #
# ---------------------------------------------------------------------------- #
createCustomer() {
    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-customer-create-service" \
        --data "url=${CUSTOMER_URL}"
    echo
    echo

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-customer-getAll-service" \
        --data "url=${CUSTOMER_URL}"
    echo
    echo

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-customer-getById-service" \
        --data "url=${CUSTOMER_RAW_URL}"
    echo
    echo

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-customer-delete-service" \
        --data "url=${CUSTOMER_RAW_URL}"
    echo
    echo

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-customer-update-service" \
        --data "url=${CUSTOMER_RAW_URL}"
    echo
    echo

    #create routes
    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-customer-create-service/routes" \
        --data "hosts[]=servercustomercreate.com"
    echo
    echo

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-customer-getAll-service/routes" \
        --data "hosts[]=servercustomergetall.com"
    echo
    echo

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-customer-getById-service/routes" \
        --data-urlencode "paths[]=~/Customer/(?<id>\d+)" \
        --data "strip_path=false"
    echo
    echo

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-customer-delete-service/routes" \
        --data-urlencode "paths[]=~/Customer/(?<id>\d+)" \
        --data "strip_path=false"
    echo
    echo

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-customer-update-service/routes" \
        --data-urlencode "paths[]=~/Customer/(?<id>\d+)/(?<name>\S+)/(?<FiscalNumber>\S+)/(?<location>\S+)" \
        --data "strip_path=false"
    echo
    echo

}
testCustomer() {
    echo

    curl -i -X GET \
        --url "$KONG_GATEWAY" \
        --header "Host: servercustomergetall.com"
    echo
    echo

    curl -v -i -H "accept: application/json" \
        --url "$KONG_GATEWAY/Customer/3"
    echo
    echo

    curl -v -i -X DELETE -H "accept: application/json" \
        --url "$KONG_GATEWAY/Customer/2"
    echo
    echo

    curl -v -i -X PUT -H "accept: application/json" \
        --url "$KONG_GATEWAY/Customer/3/teste/12345/teste/"
    echo
    echo
}

# ---------------------------------------------------------------------------- #
#                                 LOYALTY CARD                                 #
# ---------------------------------------------------------------------------- #

createLoyaltyCard() {
    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-loyaltyCard-create-service" \
        --data "url=${LOYALTYCARD_URL}"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-loyaltyCard-getAll-service" \
        --data "url=${LOYALTYCARD_URL}"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-loyaltyCard-getById-service" \
        --data "url=${LOYALTYCARD_RAW_URL}"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-loyaltyCard-delete-service" \
        --data "url=${LOYALTYCARD_RAW_URL}"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-loyaltyCard-update-service" \
        --data "url=${LOYALTYCARD_RAW_URL}"

    # #create routes

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-loyaltyCard-create-service/routes" \
        --data "hosts[]=serverloyaltyCardcreate.com"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-loyaltyCard-getAll-service/routes" \
        --data "hosts[]=serverloyaltyCardgetall.com"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-loyaltyCard-getById-service/routes" \
        --data-urlencode "paths[]=~/Loyaltycard/(?<id>\d+)" \
        --data "strip_path=false"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-loyaltyCard-delete-service/routes" \
        --data-urlencode "paths[]=~/Loyaltycard/(?<id>\d+)" \
        --data "strip_path=false"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-loyaltyCard-update-service/routes" \
        --data-urlencode "paths[]=~/Loyaltycard/(?<id>\d+)/(?<idCustomer>\d+)/(?<idShop>\d+)" \
        --data "strip_path=false"

}

testLoyaltyCard() {
    echo

    curl -i -X GET \
        --url "$KONG_GATEWAY"
    # --header "Host: serverloyaltyCardgetall.com"
    echo
    echo

    curl -i -H "accept: application/json" \
        --url "$KONG_GATEWAY/Loyaltycard/1"
    echo
    echo

    curl -i -X DELETE -H "accept: application/json" \
        --url "$KONG_GATEWAY/Loyaltycard/2"
    echo
    echo

    curl -i -X PUT -H "accept: application/json" \
        --url "$KONG_GATEWAY/Loyaltycard/1/1/1"
    echo
    echo

}

# ---------------------------------------------------------------------------- #
#                                     SHOP                                     #
# ---------------------------------------------------------------------------- #

createShop() {
    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-shop-create-service" \
        --data "url=${SHOP_URL}"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-shop-getAll-service" \
        --data "url=${SHOP_URL}"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-shop-getById-service" \
        --data "url=${SHOP_RAW_URL}"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-shop-delete-service" \
        --data "url=${SHOP_RAW_URL}"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-shop-update-service" \
        --data "url=${SHOP_RAW_URL}"

    #create routes

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-shop-create-service/routes" \
        --data "hosts[]=servershopcreate.com"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/services/invoke-shop-getAll-service/routes" \
        --data "hosts[]=servershopgetall.com"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/services/invoke-shop-getById-service/routes" \
        --data-urlencode "paths[]=~/Shop/(?<id>\d+)" \
        --data "strip_path=false"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/services/invoke-shop-delete-service/routes" \
        --data-urlencode "paths[]=~/Shop/(?<id>\d+)" \
        --data "strip_path=false"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-shop-update-service/routes" \
        --data-urlencode "paths[]=~/Shop/(?<id>\d+)\(?<idCustomer>\d+)\(?<idShop>\d+)" \
        --data "strip_path=false"
}

testShop() {
    curl -i -X GET \
        --url "$KONG_GATEWAY" \
        --header "Host: servershopgetall.com"

    curl -v -i -X GET -H "accept: application/json" \
        --url "$KONG_GATEWAY/Shop/1"

    curl -v -i -X DELETE -H "accept: application/json" \
        --url "$KONG_GATEWAY/Shop/2"

    curl -v -i -X PUT -H "accept: application/json" \
        --url "$KONG_GATEWAY/Shop/1/1/1"
}

# ---------------------------------------------------------------------------- #
#                                   PURCHASE                                   #
# ---------------------------------------------------------------------------- #

createPurchase() {
    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-purchase-getAll-service" \
        --data "url=${PURCHASE_URL}"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-purchase-consumer" \
        --data "url=${PURCHASE_URL}/Consume"

    # get by id
    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-purchase-getById-service" \
        --data "url=${PURCHASE_RAW_URL}"

    #create routes

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-purchase-getAll-service/routes" \
        --data "hosts[]=serverpurchasegetall.com"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-purchase-consumer/routes" \
        --data "hosts[]=serverpurchaseconsume.com"

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-purchase-getById-service/routes" \
        --data-urlencode "paths[]=~/Purchase/(?<id>\d+)" \
        --data "strip_path=false"

}

testPurchase() {
    echo

    curl -i -X GET \
        --url "$KONG_GATEWAY" \
        --header "Host: serverpurchasegetall.com"
    echo
    echo

    curl -i -X POST \
        --url "$KONG_GATEWAY/Consume" \
        --header "Host: serverpurchaseconsume.com"
    echo
    echo

    curl -v -i -H "accept: application/json" \
        --url "$KONG_GATEWAY/Purchase/1"
    echo
    echo

}

# ---------------------------------------------------------------------------- #
#                         CROSS-SELLING RECOMMENDATION                         #
# ---------------------------------------------------------------------------- #

createCrossSellingRecommendation() {
    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-crossSellingRecommendation-create-service" \
        --data "url=${CROSSSELLINGRECOMMENDATION_URL}"

    # routes

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-crossSellingRecommendation-create-service/routes" \
        --data "hosts[]=servercrossSellingRecommendationcreate.com"
}

testCrossSellingRecommendation() {
    curl -i -X GET \
        --url "$KONG_GATEWAY" \
        --header "Host: servercrossSellingRecommendation.com"
}

# ---------------------------------------------------------------------------- #
#                                SELLED PRODUCT                                #
# ---------------------------------------------------------------------------- #

createSelledProduct() {
    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-selledProduct-create-service" \
        --data "url=${SELLEDPRODUCT_URL}"

    #create routes

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-selledProduct-create-service/routes" \
        --data "hosts[]=serverselledProductcreate.com"
}

testSelledProduct() {
    curl -i -X GET \
        --url "$KONG_GATEWAY" \
        --header "Host: serverselledProduct.com"
}

# ---------------------------------------------------------------------------- #
#                                DISCOUNT COUPON                               #
# ---------------------------------------------------------------------------- #

createDiscountCoupon() {
    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES" \
        --data "name=invoke-discountCoupon-create-service" \
        --data "url=${DISCOUNTCOUPON_URL}"

    #create routes

    curl -i -X POST \
        --url "$KONG_ADMIN_SERVICES/invoke-discountCoupon-create-service/routes" \
        --data "hosts[]=serverdiscountCouponcreate.com"
}

testDiscountCoupon() {
    curl -i -X GET \
        --url "$KONG_GATEWAY" \
        --header "Host: serverdiscountCoupon.com"
}

# ---------------------------------------------------------------------------- #
#                                    PROGRAM                                   #
# ---------------------------------------------------------------------------- #

deleteAll() {
    services=$(curl -s "$KONG_ADMIN_SERVICES" | jq -r '.data[].id')

    for service_id in $services; do
        echo "Processing service $service_id"

        # Get all routes for the service
        routes=$(curl -s "$KONG_ADMIN_SERVICES/$service_id/routes" | jq -r '.data[].id')

        # Delete each route
        for route_id in $routes; do
            echo "Deleting route $route_id"
            curl -s -X DELETE "$KONG_ADMIN/routes/$route_id"
            echo "Route $route_id deleted"
        done

        # Delete the service
        echo "Deleting service $service_id"
        curl -s -X DELETE "$KONG_ADMIN_SERVICES/$service_id"
        echo "Service $service_id deleted"
    done

    echo "All services and routes have been deleted."
}

# Manage actions for each entity
manage_entity() {
    local entity=$1
    while true; do
        echo
        echo "Manage $entity:"
        echo "[c] Create"
        echo "[t] Test"
        echo "[b] Go back"
        read -r -p "Select an option: " action

        case $action in
        c)
            case $entity in
            "Customer")
                createCustomer
                ;;
            "Loyalty Card")
                createLoyaltyCard
                ;;
            "Shop")
                createShop
                ;;
            "Purchase")
                createPurchase
                ;;
            "Cross Selling Recommendation")
                createCrossSellingRecommendation
                ;;
            "Selled Product")
                createSelledProduct
                ;;
            "Discount Coupon")
                createDiscountCoupon
                ;;
            "All")
                createCustomer
                createLoyaltyCard
                createShop
                createPurchase
                createCrossSellingRecommendation
                createSelledProduct
                createDiscountCoupon
                ;;

            *)
                echo "Invalid entity selected. Please try again."
                ;;
            esac
            ;;
        t)
            case $entity in
            "Customer")
                testCustomer
                ;;
            "Loyalty Card")
                testLoyaltyCard
                ;;
            "Shop")
                testShop
                ;;
            "Purchase")
                testPurchase
                ;;
            "Cross Selling Recommendation")
                testCrossSellingRecommendation
                ;;
            "Selled Product")
                testSelledProduct
                ;;
            "Discount Coupon")
                testDiscountCoupon
                ;;
            "All")
                testCustomer
                testLoyaltyCard
                testShop
                testPurchase
                testCrossSellingRecommendation
                testSelledProduct
                testDiscountCoupon
                ;;

            *)
                echo "Invalid entity selected. Please try again."
                ;;
            esac
            ;;
        b)
            return
            ;;
        *)
            echo "Invalid option selected. Please try again."
            ;;
        esac
    done
}

main_menu() {
    while true; do
        echo
        echo "Select the entity you want to manage:"
        echo "[1] Customer"
        echo "[2] Loyalty Card"
        echo "[3] Shop"
        echo "[4] Purchase"
        echo "[5] Cross Selling Recommendation"
        echo "[6] Selled Product"
        echo "[7] Discount Coupon"
        echo "-------------------------"
        echo "[a] All"
        echo "[d] Delete all services and routes"
        echo "[q] Quit"
        read -r -p "Enter your choice: " choice

        case $choice in
        1)
            manage_entity "Customer"
            ;;
        2)
            manage_entity "Loyalty Card"
            ;;
        3)
            manage_entity "Shop"
            ;;
        4)
            manage_entity "Purchase"
            ;;
        5)
            manage_entity "Cross Selling Recommendation"
            ;;
        6)
            manage_entity "Selled Product"
            ;;
        7)
            manage_entity "Discount Coupon"
            ;;
        a)
            manage_entity "All"
            ;;
        d)
            deleteAll
            ;;
        q)
            echo "Exiting..."
            break
            ;;
        *)
            echo "Invalid choice. Please enter 1, 2, 3, or q."
            ;;
        esac
    done
}

main_menu
