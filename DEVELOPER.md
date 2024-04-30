# Developer Instructions

For development running local Kafka and mySQL instances is recommended for ease of development

## Using Docker

### KAFKA

kafka container

https://github.com/bitnami/containers/blob/main/bitnami/kafka/README.md#connecting-to-other-containers

### MySQL

- Start a mySQL docker container
  - port -> 3306:3306
  - enviroment variables
    - MYSQL_USER:teste
    - MYSQL_PASSWORD:testeteste
    - MYSQL_ROOTPASSWORD:testeteste
    - MYSQL_DATABASE:quarkus_test_all_operations

```sh
docker run mysql -e MYSQL_USER:teste -e MYSQL_PASSWORD:testeteste -e MYSQL_ROOTPASSWORD:testeteste -e MYSQL_DATABASE:quarkus_test_all_operations
```

quarkus cli

https://quarkus.io/guides/cli-tooling
