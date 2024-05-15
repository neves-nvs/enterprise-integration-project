terraform {
  required_version = ">= 1.0.0, < 2.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

variable "dockerhub_user" {
  description = "The username of the DockerHub account"
  type        = string
}

variable "key_name" {
  description = "The name of the key pair"
  type        = string
}

module "relational_database" {
  source = "./RDS"
}

module "kafka" {
  source = "./Kafka"
}

resource "null_resource" "check_kafka_cluster" {
  # Triggers the script when the Kafka broker URL changes
  triggers = {
    kafka_broker_url = module.kafka.kafka_broker_url
  }

  provisioner "local-exec" {
    command = "${path.module}/check_kafka.sh ${self.triggers.kafka_broker_url}"
  }

  depends_on = [
    module.kafka
  ]
}

module "services" {
  depends_on = [
    module.relational_database,
    null_resource.check_kafka_cluster,
  ]
  source           = "./Services"
  rds_dns          = module.relational_database.rds_dns
  kafka_broker_url = module.kafka.kafka_broker_url
  dockerhub_user   = var.dockerhub_user
  key_name         = var.key_name
}
