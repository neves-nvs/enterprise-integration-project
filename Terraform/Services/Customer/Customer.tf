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

variable "rds_dns" {
  description = "The DNS of the RDS instance"
  type        = string
}

variable "dockerhub_user" {
  description = "The username of the DockerHub account"
  type        = string
}

variable "security_group_name" {
  description = "The name of the security group"
  type        = string
  default     = "terraform-quarkus-customer"
}

resource "aws_instance" "customer" {
  # ARM
  ami           = "ami-0cd7323ab3e63805f"
  instance_type = "c6g.medium"

  #  x86
  #  ami                     = "ami-0d7a109bf30624c99"
  #  instance_type           = "t2.micro"

  vpc_security_group_ids = [aws_security_group.instance.id]
  key_name               = "vockey"

  user_data = templatefile("${path.module}/quarkus.sh", {
    rds_dns        = var.rds_dns
    dockerhub_user = var.dockerhub_user
  })
  user_data_replace_on_change = true

  tags = {
    Name = "terraform-instance-quarkus-customer"
  }
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

output "customer_dns" {
  value = aws_instance.customer.public_dns
}
