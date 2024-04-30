#!/bin/bash

source ./access.sh

# #Terraform - Quarkus purchase
cd Quarkus-Terraform/Purchase || exit
terraform destroy -auto-approve
cd ../..

# #Terraform - Quarkus customer
cd Quarkus-Terraform/customer || exit
terraform destroy -auto-approve
cd ../..

# #Terraform - Quarkus shop
cd Quarkus-Terraform/shop
terraform destroy -auto-approve
cd ../..

# #Terraform - Quarkus loyaltycard
cd Quarkus-Terraform/loyaltycard
terraform destroy -auto-approve
cd ../..

# #Terraform - RDS
cd RDS-Terraform
terraform destroy -auto-approve
cd ..

# #Terraform - Camunda
cd Camunda-Terraform
terraform destroy -auto-approve
cd ..

# # #Terraform - Kafka
cd Kafka
terraform destroy -auto-approve
cd ..

# # #Terraform - Kong
cd KongTerraform
terraform destroy -auto-approve
cd ..

# # #Terraform - Konga
cd KongaTerraform
terraform destroy -auto-approve
cd ..
