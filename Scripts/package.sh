#!/bin/bash

if [ -z "$KAFKA_BROKER_URL" ]; then
    echo "KAFKA_BROKER_URL is not set"
    exit 1
fi
if [ -z "$MYSQL_DATABASE_URL" ]; then
    echo "MYSQL_DATABASE_URL is not set"
    exit 1
fi
if [ -z "$DOCKERHUB_USER" ]; then
    echo "DOCKERHUB_USER is not set"
    exit 1
fi

cd ../Services/ || exit

for d in */; do
    # ask user for (Y/n) input defualt is Y
    read -r -p "Do you want to package $d? (Y/n) " response
    if [[ $response =~ ^(no|n|N|No|NO)$ ]]; then
        continue
    fi
    echo "Packaging $d"
    cd "$d" || exit
    mvn clean package
    cd ..
done
