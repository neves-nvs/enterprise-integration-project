#!/bin/bash
echo "Starting..."

sudo yum install -y docker

sudo service docker start

sudo docker pull "$GITHUB_USERNAME"/purchase:1.0.0-SNAPSHOT

sudo docker run -d --name purchase -p 8080:8080 "$GITHUB_USERNAME"/purchase:1.0.0-SNAPSHOT

echo "Finished."
