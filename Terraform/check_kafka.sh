#!/bin/bash

KAFKA_BROKER_URL=$1
if [ -z "$KAFKA_BROKER_URL" ]; then
    echo "Usage: $0 <KAFKA_BROKER_URL>"
    exit 1
fi

KAFKA_VERSION="3.6.2"
SCALA_VERSION="2.13"
KAFKA_HOME="kafka_exec"

download_kafka() {
    echo "Kafka not found. Downloading Kafka ${SCALA_VERSION}-${KAFKA_VERSION}..."
    wget https://downloads.apache.org/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz
    tar -zxf kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz
    mv kafka_2.13-3.6.2 kafka_exec
    rm "kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz"
    echo "Kafka downloaded into kafka_exec"
}

if [ ! -d "kafka_exec" ]; then
    download_kafka
else
    echo "Kafka is already downloaded at kafka_exec"
fi

if ! command -v kafka_exec/bin/kafka-topics.sh; then
    echo >&2 "kafka-topics.sh not found. Aborting."
    exit 1
fi

# Check Kafka availability, retry up to 5 times
max_attempts=5
attempt_counter=0

until $KAFKA_HOME/bin/kafka-topics.sh --bootstrap-server "${KAFKA_BROKER_URL}:9092" --list; do
    attempt_counter=$((attempt_counter + 1))
    echo "Waiting for Kafka cluster to be available... Attempt $attempt_counter of $max_attempts"
    if [ $attempt_counter -ge $max_attempts ]; then
        echo "Failed to connect to Kafka cluster after $max_attempts attempts. Aborting."
        exit 1
    fi
    sleep 10
done

echo "Kafka cluster is now operational"
