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
# go through Services/ doing terraform destroy with a for loop
cd Services || exit

for d in */; do
    echo "Destroying $d"
    cd "$d" || exit
    # terraform init -upgrade
    terraform destroy -auto-approve
    cd ..
done

cd ..

# Data
# ask if user wants to procedd with the deletion of the data
read -p "Do you want to delete the data? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Data deletion aborted"
    exit 1
fi

(
    cd RDS || exit
    terraform destroy -auto-approve
)

(
    cd Kafka || exit
    terraform destroy -auto-approve
)
