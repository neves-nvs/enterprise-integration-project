#!/bin/bash

cd "$(dirname "$0")" || exit

# Now you can safely run commands that rely on relative paths
echo "Running in directory: $(pwd)"

source ./access.sh

cd ../Terraform || exit

# Top Level Infra
# (
#     cd Camunda || exit
#     terraform destroy -auto-approve
# )

# (
#     cd KongTerraform || exit
#     terraform destroy -auto-approve
#     cd ..
# )

# (
#     cd KongaTerraform || exit
#     terraform destroy -auto-approve
# )

# Microservices

(
    cd Services/Purchase || exit
    terraform destroy -auto-approve
)

(
    cd Services/Customer || exit
    terraform destroy -auto-approve
)

(
    cd Services/Shop || exit
    terraform destroy -auto-approve
)

(
    cd Services/LoyaltyCard || exit
    terraform destroy -auto-approve
    cd ../..
)

(
    cd Services/DiscountCoupon || exit
    terraform destroy -auto-approve
)

(
    cd Services/Crosssellingrecommendation || exit
    terraform destroy -auto-approve
)

(
    cd Services/Selledproduct || exit
    terraform destroy -auto-approve
)

# Data

(
    cd RDS-Terraform || exit
    terraform destroy -auto-approve
)

(
    cd Kafka || exit
    terraform destroy -auto-approve
)
