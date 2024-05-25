# Development

Running in for development purposes

## MySQL

```sh
docker run -p 3306:3306 \
    -e MYSQL_ROOT_PASSWORD=testeteste \
    -e MYSQL_USER=teste \
    -e MYSQL_PASSWORD=testeteste
    -e MYSQL_DATABASE=quarkus_test_all_operations \
    mysql:latest
```

## Kafka (single test node)

```sh
docker run -p 9092:9092 apache:kafka
```

## Services

```sh
export MYSQL_DATABASE_URL='localhost'
export KAFKA_BROKER_URL='localhost'
```

then for each service run

```sh
mvn quarkus:dev
```

## Camunda

```sh
docker run -p 8080:8080 camunda/camunda-bpm-platform:latest
```

Since camunda is running inside docker, and the services are not, it is needed to change the url in the BPMN files to `host.docker.internal` instead of `localhost`. This works os MacOS, to be tested on other OS.
