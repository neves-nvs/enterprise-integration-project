#!/bin/bash
echo "Starting..."

echo "MYSQL_DATABASE_URL=${rds_dns}" >>/etc/environment

export MYSQL_DATABASE_URL=${rds_dns}

echo "DOCKERHUB_USER=${dockerhub_user}" >>/etc/environment

export DOCKERHUB_USER=${dockerhub_user}

# sudo yum update -y

sudo yum install -y docker

sudo service docker start

sudo docker pull "$DOCKERHUB_USER"/loyaltycard:1.0.0-SNAPSHOT

sudo docker run -d --name loyaltycard \
    -p 8080:8080 \
    -e MYSQL_DATABASE_URL="$MYSQL_DATABASE_URL" \
    "$DOCKERHUB_USER"/loyaltycard:1.0.0-SNAPSHOT

echo "Finished."
