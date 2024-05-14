variable "rds_dns" {
  description = "The DNS of the RDS instance"
  type        = string
}

variable "kafka_broker_url" {
  description = "The URL of the Kafka broker"
  type        = string
  default     = ""
}

variable "dockerhub_user" {
  description = "The username of the DockerHub account"
  type        = string
}

variable "docker_image" {
  description = "The Docker image to be used"
  type        = string
  default     = "shop"

}

variable "security_group_id" {
  description = "value of the security group"
  type        = string
}

resource "aws_instance" "shop" {
  # ARM
  ami           = "ami-0cd7323ab3e63805f" #arm
  instance_type = "c6g.medium"

  # alternative instance_type for Amazon Linux x86 built by Amazon Web Services
  #  ami                     = "ami-0d7a109bf30624c99"
  #  instance_type           = "t2.micro"

  vpc_security_group_ids = [var.security_group_id]
  key_name               = "ei2024Sprint1"

  user_data = templatefile("${path.module}/../quarkus.sh.tf.tpl", {
    rds_dns          = var.rds_dns
    dockerhub_user   = var.dockerhub_user
    kafka_broker_url = var.kafka_broker_url
    docker_image     = var.docker_image
  })
  user_data_replace_on_change = true

  tags = {
    Name = "terraform-instance-quarkus-shop"
  }
}

output "shop_dns" {
  value = aws_instance.shop.public_dns
}

