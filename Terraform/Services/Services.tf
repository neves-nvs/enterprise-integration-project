
variable "rds_dns" {
  description = "The DNS of the RDS instance"
  type        = string
}

variable "dockerhub_user" {
  description = "The username of the DockerHub account"
  type        = string
}

variable "kafka_broker_url" {
  description = "The URL of the Kafka broker"
  type        = string
}

variable "security_group_name" {
  description = "The name of the security group"
  type        = string
  default     = "terraform-quarkus"
}

resource "aws_security_group" "instance" {
  name = var.security_group_name
  ingress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
}

output "security_group_id" {
  value = aws_security_group.instance.id
}

module "shop" {
  source = "./Shop"
  #   security_group_name = var.security_group_name
  rds_dns           = var.rds_dns
  dockerhub_user    = var.dockerhub_user
  security_group_id = aws_security_group.instance.id
}

module "customer" {
  source = "./Customer"
  #   security_group_name = var.security_group_name
  rds_dns        = var.rds_dns
  dockerhub_user = var.dockerhub_user
  #   kafka_broker_url = var.kafka_broker_url
}

module "loyalty-card" {
  source = "./LoyaltyCard"
  #   security_group_name = var.security_group_name
  rds_dns          = var.rds_dns
  dockerhub_user   = var.dockerhub_user
  kafka_broker_url = var.kafka_broker_url
}

module "purchase" {
  source = "./Purchase"
  #   security_group_name = var.security_group_name
  rds_dns          = var.rds_dns
  dockerhub_user   = var.dockerhub_user
  kafka_broker_url = var.kafka_broker_url
}

module "discount-coupon" {
  source = "./DiscountCoupon"
  #   security_group_name = var.security_group_name
  rds_dns          = var.rds_dns
  dockerhub_user   = var.dockerhub_user
  kafka_broker_url = var.kafka_broker_url
}

module "cross-selling-recommendation" {
  source = "./CrossSellingRecommendation"
  #   security_group_name = var.security_group_name
  rds_dns          = var.rds_dns
  dockerhub_user   = var.dockerhub_user
  kafka_broker_url = var.kafka_broker_url
}

module "selled-product" {
  source = "./SelledProduct"
  #   security_group_name = var.security_group_name
  rds_dns          = var.rds_dns
  dockerhub_user   = var.dockerhub_user
  kafka_broker_url = var.kafka_broker_url
}
