#!/bin/bash

source ./access.sh

# ---------------------------------------------------------------------------- #
#                                     Data                                     #
# ---------------------------------------------------------------------------- #

cd RDS-Terraform || exit
terraform init
terraform apply -auto-approve
esc=$'\e'
addressDB="$(terraform state show aws_db_instance.rds | grep address | sed "s/address//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
cd ..

cd Kafka || exit
terraform init
terraform apply -auto-approve
esc=$'\e'
addresskafka="$(terraform state show 'aws_instance.KafkaConfiguration[0]' | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
cd ..

# ---------------------------------------------------------------------------- #
#                             Update Microservices                             #
# ---------------------------------------------------------------------------- #

(
    cd microservices/purchase/src/main/resources || exit
    sed -i '' "s/kafka.bootstrap.servers/#kafka.bootstrap.servers/g" application.properties
    echo "" >>application.properties
    echo "kafka.bootstrap.servers=$addresskafka:9092" >>application.properties
    sed -i '' "s/quarkus.datasource.reactive.url/#quarkus.datasource.reactive.url/g" application.properties
    echo "" >>application.properties
    echo "quarkus.datasource.reactive.url=mysql://$addressDB:3306/quarkus_test_all_operations" >>application.properties
    cd ../../..
    ./mvnw clean package
)

(
    cd microservices/loyaltycard/src/main/resources || exit
    sed -i '' "s/quarkus.datasource.reactive.url/#quarkus.datasource.reactive.url/g" application.properties
    echo "" >>application.properties
    echo "quarkus.datasource.reactive.url=mysql://$addressDB:3306/quarkus_test_all_operations" >>application.properties
    cd ../../..
    ./mvnw clean package
)

(
    cd microservices/customer/src/main/resources || exit
    sed -i '' "s/quarkus.datasource.reactive.url/#quarkus.datasource.reactive.url/g" application.properties
    echo "" >>application.properties
    echo "quarkus.datasource.reactive.url=mysql://$addressDB:3306/quarkus_test_all_operations" >>application.properties
    cd ../../..
    ./mvnw clean package
)

(
    cd microservices/shop/src/main/resources || exit
    sed -i '' "s/quarkus.datasource.reactive.url/#quarkus.datasource.reactive.url/g" application.properties
    echo "" >>application.properties
    echo "quarkus.datasource.reactive.url=mysql://$addressDB:3306/quarkus_test_all_operations" >>application.properties
    cd ../../..
    ./mvnw clean package
)

(
    cd Kafka || exit
    echo "KAFKA IS AVAILABLE HERE:"
    echo "$addresskafka"
    echo
)

# ---------------------------------------------------------------------------- #
#                                 Microservices                                #
# ---------------------------------------------------------------------------- #

source ./deploy-microservices.sh

# ---------------------------------------------------------------------------- #
#                                Top Level Infra                               #
# ---------------------------------------------------------------------------- #

# Terraform 1 - Kong
# cd KongTerraform || exit
# terraform init
# terraform apply -auto-approve
# cd ..

# Terraform 2 - Konga
# cd KongaTerraform || exit
# terraform init
# terraform apply -auto-approve
# cd ..

# Terraform - Camunda
# cd Camunda-Terraform
# terraform init
# terraform apply -auto-approve
# cd ..

# # Showing all the PUBLIC_DNSs
# #echo CAMUNDA -
# cd Camunda-Terraform || exit
# #terraform state show aws_instance.camunda_engine |grep public_dns
# echo "CAMUNDA IS AVAILABLE HERE:"
# addressCamunda="$(terraform state show aws_instance.camunda_engine | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
# echo "http://${addressCamunda}:8080/camunda"
# echo
# cd ..

(
    cd RDS || exit
    echo "RDS IS AVAILABLE HERE:"
    terraform state show aws_db_instance.example | grep address
    terraform state show aws_db_instance.example | grep port
    echo
)

# echo "KONG IS AVAILABLE HERE:"
# cd KongTerraform || exit
# addressKong="$(terraform state show aws_instance.exampleInstallKong | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
# echo "http://${addressKong}:8080/"
# echo
# cd ..

# echo "KONGA IS AVAILABLE HERE:"
# cd KongaTerraform || exit
# addressKonga="$(terraform state show aws_instance.exampleInstallKonga | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
# echo "http://${addressKonga}:1337/"
# echo
# cd ..
