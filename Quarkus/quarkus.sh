#!/bin/bash
echo "Starting..."

sudo yum install -y docker

sudo service docker start

sudo docker login -u "neves_nvs" -p "vfQ8ms9G&##e0%D\$t0&0*"

sudo docker pull neves_nvs/scenario1:1.0.0-SNAPSHOT

sudo docker run -d --name scenario1 -p 8080:8080 neves_nvs/scenario1:1.0.0-SNAPSHOT

echo "Finished."
