#!/usr/bin/env bash

# run from file location
cd "$(dirname "$0")" || exit
cd ..

cd Terraform || exit

address="$(terraform state show aws_instance.exampleInstallKong | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"

echo "KONG IS AVAILABLE HERE:"
echo "http://"$address":8080/"
echo

echo "KONGA IS AVAILABLE HERE:"
echo "http://"$addressKonga":1337/"
echo

export KONG_SERVER_ADDRESS=$address


CROSSSELLINGRECOMMENDATION_URL="$(terraform state.te)

CROSSSELLINGRECOMMENDATION_RAW_URL=http://${CROSSSELLINGRECOMMENDATION_INSTANCE}:8080/
CUSTOMER_RAW_URL=http://${CUSTOMER_INSTANCE}:8080/
DISCOUNTCOUPON_RAW_URL=http://${DISCOUNTCOUPON_INSTANCE}:8080/
LOYALTYCARD_RAW_URL=http://${LOYALTYCARD_INSTANCE}:8080/
PURCHASE_RAW_URL=http://${PURCHASE_INSTANCE}:8080/
SELLEDPRODUCT_RAW_URL=http://${SELLEDPRODUCT_INSTANCE}:8080/
SHOP_RAW_URL=http://${SHOP_INSTANCE}:8080/

CROSSSELLINGRECOMMENDATION_URL=http://${CROSSSELLINGRECOMMENDATION_INSTANCE}:8080/CrossSellingRecommendation
CUSTOMER_URL=http://${CUSTOMER_INSTANCE}:8080/Customer
DISCOUNTCOUPON_URL=http://${DISCOUNTCOUPON_INSTANCE}:8080/DiscountCoupon
LOYALTYCARD_URL=http://${LOYALTYCARD_INSTANCE}:8080/LoyaltyCard
PURCHASE_URL=http://${PURCHASE_INSTANCE}:8080/Purchase
SELLEDPRODUCT_URL=http://${SELLEDPRODUCT_INSTANCE}:8080/SelledProduct
SHOP_URL=http://${SHOP_INSTANCE}:8080/Shop
CONSUME_PURCHASE_URL=http://${PURCHASE_INSTANCE}:8080/Purchase/Consume

# -----------------------------------------------------------
#                     CUSTOMER
# -----------------------------------------------------------

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-customer-create-service" \
    --data "url=${CUSTOMER_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-customer-getAll-service" \
    --data "url=${CUSTOMER_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-customer-getById-service" \
    --data "url=${CUSTOMER_RAW_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-customer-delete-service" \
    --data "url=${CUSTOMER_RAW_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-customer-update-service" \
    --data "url=${CUSTOMER_RAW_URL}"

#create routes
curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-customer-create-service/routes" \
    --data "hosts[]=servercustomercreate.com"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-customer-getAll-service/routes" \
    --data "hosts[]=servercustomergetall.com"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-customer-getById-service/routes" \
    --data-urlencode "paths[]=~/Customer/(?<id>\d+)" \
    --data "strip_path=false"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-customer-delete-service/routes" \
    --data-urlencode "paths[]=~/Customer/(?<id>\d+)" \
    --data "strip_path=false"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-customer-update-service/routes" \
    --data-urlencode "paths[]=~/Customer/(?<id>\d+)/(?<name>\S+)/(?<FiscalNumber>\S+)/(?<location>\S+)" \
    --data "strip_path=false"

# invoke services to test (done individually on terminal to test report)

#curl -i -X GET \
#--url "http://${KONG_SERVER_ADDRESS}:8000/" \
#--header "Host: servercustomer.com"

#curl -v -i -X GET -H "accept: application/json" \
#--url "http://${KONG_SERVER_ADDRESS}:8000/Customer/ID"

#curl -v -i -X DELETE -H "accept: application/json" \
#--url "http://${KONG_SERVER_ADDRESS}:8000/Customer/ID"

#curl -v -i -X PUT -H "accept: application/json" \
#--url "http://${KONG_SERVER_ADDRESS}:8000/Customer/ID/name/number/location"

# -----------------------------------------------------------
#                     LOYALTY CARD
# -----------------------------------------------------------

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-loyaltyCard-create-service" \
    --data "url=${LOYALTYCARD_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-loyaltyCard-getAll-service" \
    --data "url=${LOYALTYCARD_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-loyaltyCard-getById-service" \
    --data "url=${LOYALTYCARD_RAW_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-loyaltyCard-delete-service" \
    --data "url=${LOYALTYCARD_RAW_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-loyaltyCard-update-service" \
    --data "url=${LOYALTYCARD_RAW_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-loyaltyCard-getDual-service" \
    --data "url=${LOYALTYCARD_RAW_URL}"

#create routes

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-loyaltyCard-create-service/routes" \
    --data "hosts[]=serverloyaltyCardcreate.com"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-loyaltyCard-getAll-service/routes" \
    --data "hosts[]=serverloyaltyCardgetall.com"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-loyaltyCard-getById-service/routes" \
    --data-urlencode "paths[]=~/Loyaltycard/(?<id>\d+)" \
    --data "strip_path=false"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-loyaltyCard-delete-service/routes" \
    --data-urlencode "paths[]=~/Loyaltycard/(?<id>\d+)" \
    --data "strip_path=false"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-loyaltyCard-update-service/routes" \
    --data-urlencode "paths[]=~/Loyaltycard/(?<id>\d+)\(?<idCustomer>\d+)\(?<idShop>\d+)" \
    --data "strip_path=false"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-loyaltyCard-getDual-service/routes" \
    --data-urlencode "paths[]=~/Loyaltycard/(?<idCustomer>\d+)/(?<idShop>\d+)"

# invoke services to test (done individually on terminal to test report)

#curl -i -X GET \
#--url "http://${KONG_SERVER_ADDRESS}:8000/" \
#--header "Host: serverloyaltyCard.com"

#curl -v -i -X GET -H "accept: application/json" \
#--url "http://${KONG_SERVER_ADDRESS}:8000/Loyaltycard/ID"

#curl -v -i -X DELETE -H "accept: application/json" \
#--url "http://${KONG_SERVER_ADDRESS}:8000/Loyaltycard/ID"

#curl -v -i -X PUT -H "accept: application/json" \
#--url "http://${KONG_SERVER_ADDRESS}:8000/Loyaltycard/ID/IDcustomer/IDshop"

# -----------------------------------------------------------
#                     SHOP
# -----------------------------------------------------------

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-shop-create-service" \
    --data "url=${SHOP_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-shop-getAll-service" \
    --data "url=${SHOP_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-shop-getById-service" \
    --data "url=${SHOP_RAW_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-shop-delete-service" \
    --data "url=${SHOP_RAW_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-shop-update-service" \
    --data "url=${SHOP_RAW_URL}"

#create routes

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-shop-create-service/routes" \
    --data "hosts[]=servershopcreate.com"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-shop-getAll-service/routes" \
    --data "hosts[]=servershopgetall.com"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-shop-getById-service/routes" \
    --data-urlencode "paths[]=~/Shop/(?<id>\d+)" \
    --data "strip_path=false"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-shop-delete-service/routes" \
    --data-urlencode "paths[]=~/Shop/(?<id>\d+)" \
    --data "strip_path=false"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-shop-update-service/routes" \
    --data-urlencode "paths[]=~/Shop/(?<id>\d+)\(?<idCustomer>\d+)\(?<idShop>\d+)" \
    --data "strip_path=false"

# invoke services to test (done individually on terminal to test report)

#curl -i -X GET \
#--url "http://${KONG_SERVER_ADDRESS}:8000/" \
#--header "Host: servershop.com"

#curl -v -i -X GET -H "accept: application/json" \
#--url "http://${KONG_SERVER_ADDRESS}:8000/Shop/ID"

#curl -v -i -X DELETE -H "accept: application/json" \
#--url "http://${KONG_SERVER_ADDRESS}:8000/Shop/ID"

#curl -v -i -X PUT -H "accept: application/json" \
#--url "http://${KONG_SERVER_ADDRESS}:8000/Shop/ID/IDcustomer/IDshop"

# -----------------------------------------------------------
#                     PURCHASE
# -----------------------------------------------------------

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-purchase-getAll-service" \
    --data "url=${PURCHASE_URL}"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-purchase-consume-service" \
    --data "url=${CONSUME_PURCHASE_URL}"

#create routes

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-purchase-getAll-service/routes" \
    --data "hosts[]=serverpurchasegetall.com"

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-purchase-consume-service/routes" \
    --data "serverpurchaseconsume.com"

# -----------------------------------------------------------
#                     CROSSSELLING RECOMMENDATION
# -----------------------------------------------------------

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-crossSellingRecommendation-create-service" \
    --data "url=${CROSSSELLINGRECOMMENDATION_URL}"

# routes

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-crossSellingRecommendation-create-service/routes" \
    --data "hosts[]=servercrossSellingRecommendationcreate.com"

# -----------------------------------------------------------
#                     SELLED PRODUCT
# -----------------------------------------------------------

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-selledProduct-create-service" \
    --data "url=${SELLEDPRODUCT_URL}"

#create routes

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-selledProduct-create-service/routes" \
    --data "hosts[]=serverselledProductcreate.com"

# -----------------------------------------------------------
#                     DISCOUNT COUPON
# -----------------------------------------------------------

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/" \
    --data "name=invoke-discountCoupon-create-service" \
    --data "url=${DISCOUNTCOUPON_URL}"

#create routes

curl -i -X POST \
    --url "http://${KONG_SERVER_ADDRESS}:8001/services/invoke-discountCoupon-create-service/routes" \
    --data "hosts[]=serverdiscountCouponcreate.com"
