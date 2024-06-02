
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

variable "key_name" {
  description = "The name of the key pair"
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

module "customer-shop-loyaltycard" {
  source         = "./Customer-Shop-LoyaltyCard"
  rds_dns        = var.rds_dns
  dockerhub_user = var.dockerhub_user
  key_name       = var.key_name
}

module "purchase" {
  source           = "./Purchase"
  rds_dns          = var.rds_dns
  dockerhub_user   = var.dockerhub_user
  kafka_broker_url = var.kafka_broker_url
  key_name         = var.key_name
}

module "discount-coupon" {
  source           = "./DiscountCoupon"
  rds_dns          = var.rds_dns
  dockerhub_user   = var.dockerhub_user
  kafka_broker_url = var.kafka_broker_url
  key_name         = var.key_name
}

module "cross-selling-recommendation" {
  source           = "./CrossSellingRecommendation"
  rds_dns          = var.rds_dns
  dockerhub_user   = var.dockerhub_user
  kafka_broker_url = var.kafka_broker_url
  key_name         = var.key_name
}

module "selled-product" {
  source           = "./SelledProduct"
  rds_dns          = var.rds_dns
  dockerhub_user   = var.dockerhub_user
  kafka_broker_url = var.kafka_broker_url
  key_name         = var.key_name
}

output "customer-shop-loyaltycard-public_dns" {
  value = module.customer-shop-loyaltycard.public_dns
}

output "purchase-public_dns" {
  value = module.purchase.public_dns
}

output "discountcoupon-public_dns" {
  value = module.discount-coupon.public_dns
}

output "cross-selling-recommendation-public_dns" {
  value = module.cross-selling-recommendation.public_dns
}

output "selled-product-public_dns" {
  value = module.selled-product.public_dns
}
