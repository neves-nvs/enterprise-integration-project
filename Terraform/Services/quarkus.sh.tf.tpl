#!/bin/bash
echo "Starting..."

echo "DOCKERHUB_USER=${dockerhub_user}" >>/etc/environment
export DOCKERHUB_USER=${dockerhub_user}

echo "MYSQL_DATABASE_URL=${rds_dns}" >>/etc/environment
export MYSQL_DATABASE_URL=${rds_dns}

%{ if kafka_broker_url != "" ~}
echo "KAFKA_BROKER_URL=${kafka_broker_url}" >>/etc/environment
export KAFKA_BROKER_URL=${kafka_broker_url}
%{ endif ~}

# sudo yum update -y
sudo yum install -y docker

sudo service docker start

sudo docker pull "$DOCKERHUB_USER"/${docker_image}:1.0.0-SNAPSHOT

sudo docker run -d --name ${docker_image} \ ### Name of container does not have to be the same as the image
    -p 8080:8080 \
    -e MYSQL_DATABASE_URL="$MYSQL_DATABASE_URL" \
    %{ if kafka_broker_url != "" ~}
    -e KAFKA_BROKER_URL="$KAFKA_BROKER_URL" \
    %{ endif ~}
    "$DOCKERHUB_USER"/${docker_image}:1.0.0-SNAPSHOT

echo "Finished."
