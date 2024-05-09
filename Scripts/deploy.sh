#!/bin/bash

# warn user before changing directory
echo "Changing directory to the script location"
cd "$(dirname "$0")" || exit
echo "Runnind script from: $(pwd)"
echo

source ./access.sh
cd .. # Back to the root directory

# ---------------------------------------------------------------------------- #
#                                     Data                                     #
# ---------------------------------------------------------------------------- #

cd Terraform/ || exit

cd RDS || exit
echo "Creating RDS..."
terraform init >/dev/null
terraform apply -auto-approve
MYSQL_DATABASE_URL="$(terraform output -raw rds_dns)"
cd ..

cd Kafka || exit
echo "Creating Kafka..."
terraform init >/dev/null
terraform apply -auto-approve
esc=$'\e'
KAFKA_BROKER_URL="$(terraform state show 'aws_instance.kafka_broker[0]' | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
cd ..

echo
cd .. # Back to the root directory

# ---------------------------------------------------------------------------- #
#                      Set Required Environment Variables                      #
# ---------------------------------------------------------------------------- #
cd Scripts || exit
source ./variables.sh
cd ..

if [ -z "$DOCKERHUB_USER" ]; then
    echo "DOCKERHUB_USER is not set"
    exit 1
fi
export TF_VAR_dockerhub_user=$DOCKERHUB_USER
echo "DOCKERHUB USER: $TF_VAR_dockerhub_user"

if [ -z "$KAFKA_BROKER_URL" ]; then
    echo "KAFKA_BROKER_URL is not set"
    exit 1
fi
export TF_VAR_kafka_broker_url=$KAFKA_BROKER_URL
echo "KAFKA BROKER URL: $TF_VAR_kafka_broker_url"

if [ -z "$MYSQL_DATABASE_URL" ]; then
    echo "MYSQL_DATABASE_URL is not set"
    exit 1
fi
export TF_VAR_rds_dns=$MYSQL_DATABASE_URL
echo "RDS DNS: $TF_VAR_rds_dns"

echo

# ---------------------------------------------------------------------------- #
#                             Package Microservices                            #
# ---------------------------------------------------------------------------- #

cd Scripts || exit
source ./package.sh
echo
cd ..

# ---------------------------------------------------------------------------- #
#                             Deploy Microservices                             #
# ---------------------------------------------------------------------------- #

# terraform apply --var 'inputname=asdf'
cd Terraform || exit

cd Services/ || exit
(
    cd Customer || exit
    terraform init
    terraform apply -auto-approve
    #terraform state show 'aws_instance.exampleDeployQuarkus' |grep public_dns
    echo "MICROSERVICE customer IS AVAILABLE HERE:"
    addressMS="$(terraform output -raw customer_dns)"
    echo "http://${addressMS}:8080/q/swagger-ui/"
    echo
)

(
    cd Shop || exit
    terraform init
    terraform apply -auto-approve
    echo "MICROSERVICE shop IS AVAILABLE HERE:"
    addressMS="$(terraform output -raw shop_dns)"
    echo "http://${addressMS}:8080/q/swagger-ui/"
    echo
)

(
    cd LoyaltyCard || exit
    terraform init
    terraform apply -auto-approve
    echo "MICROSERVICE loyaltycard IS AVAILABLE HERE:"
    addressMS="$(terraform output -raw loyaltycard_dns)"
    echo "http://${addressMS}:8080/q/swagger-ui/"
    echo
)

(
    cd Purchase || exit
    terraform init
    terraform apply -auto-approve
    echo "MICROSERVICE purchase IS AVAILABLE HERE:"
    addressMS="$(terraform output -raw purchase_dns)"
    echo "http://${addressMS}:8080/q/swagger-ui/"
    echo
)

(
    cd DiscountCoupon || exit
    terraform init >/dev/null
    terraform apply -auto-approve
    echo "MICROSERVICE discountcoupon IS AVAILABLE HERE:"
    addressMS="$(terraform output -raw discountcoupon_dns)"
    echo "http://${addressMS}:8080/q/swagger-ui/"
    echo
)

(
    cd CrossSellingRecommendation || exit
    terraform init
    terraform apply -auto-approve
    echo "MICROSERVICE crosssellingrecommendation IS AVAILABLE HERE:"
    addressMS="$(terraform output -raw crosssellingrecommendation_dns)"
    echo "http://${addressMS}:8080/q/swagger-ui/"
    echo
)

(
    cd SelledProduct || exit
    terraform init
    terraform apply -auto-approve
    echo "MICROSERVICE selledproduct IS AVAILABLE HERE:"
    addressMS="$(terraform output -raw selledproduct_dns)"
    echo "http://${addressMS}:8080/q/swagger-ui/"
    echo
)

cd ../..

# ---------------------------------------------------------------------------- #
#                               Display All Info                               #
# ---------------------------------------------------------------------------- #

(
    cd Terraform || exit

    (
        cd RDS || exit
        echo "RDS IS AVAILABLE HERE:"
        terraform output -raw rds_dns
        echo
    )

    (
        cd Kafka || exit
        echo "KAFKA IS AVAILABLE HERE:"
        terraform state show 'aws_instance.kafka_broker[0]' | grep public_dns
        echo
    )

    (

        cd Services || exit
        (
            cd Shop || exit
            echo "SHOP IS AVAILABLE HERE:"
            echo "http://$(terraform output -raw shop_dns):8080/q/swagger-ui/"
        )

        (
            cd Customer || exit
            echo "CUSTOMER IS AVAILABLE HERE:"
            echo "http://$(terraform output -raw customer_dns):8080/q/swagger-ui/"
        )

        (
            cd LoyaltyCard || exit
            echo "LOYALTYCARD IS AVAILABLE HERE:"
            echo "http://$(terraform output -raw loyaltycard_dns):8080/q/swagger-ui/"
        )

        (
            cd Purchase || exit
            echo "PURCHASE IS AVAILABLE HERE:"
            echo "http://$(terraform output -raw purchase_dns):8080/q/swagger-ui/"
        )

        (
            cd DiscountCoupon || exit
            echo "DISCOUNTCOUPON IS AVAILABLE HERE:"
            echo "http://$(terraform output -raw discountcoupon_dns):8080/q/swagger-ui/"
        )

        (
            cd CrossSellingRecommendation || exit
            echo "CROSSSELLINGRECOMMENDATION IS AVAILABLE HERE:"
            echo "http://$(terraform output -raw crosssellingrecommendation_dns):8080/q/swagger-ui/"
        )

        (
            cd SelledProduct || exit
            echo "SELLEDPRODUCT IS AVAILABLE HERE:"
            echo "http://$(terraform output -raw selledproduct_dns):8080/q/swagger-ui/"
        )
    )
)
