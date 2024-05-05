#!/bin/bash
echo "Starting..."

echo "DOCKERHUB_USER=${dockerhub_user}" >>/etc/environment
export DOCKERHUB_USER=${dockerhub_user}

echo "MYSQL_DATABASE_URL=${rds_dns}" >>/etc/environment
export MYSQL_DATABASE_URL=${rds_dns}

echo "KAFKA_BROKER_URL=${kafka_broker_url}" >>/etc/environment
export KAFKA_BROKER_URL=${kafka_broker_url}

# sudo yum update -y

sudo yum install -y docker

sudo service docker start

sudo docker pull "$DOCKERHUB_USER"/selledproduct:1.0.0-SNAPSHOT

sudo docker run -d --name selledproduct \
    -p 8080:8080 \
    -e MYSQL_DATABASE_URL="$MYSQL_DATABASE_URL" \
    -e KAFKA_BROKER_URL="$KAFKA_BROKER_URL" \
    "$DOCKERHUB_USER"/selledproduct:1.0.0-SNAPSHOT

echo "Finished."
