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

resource "aws_instance" "customer" {
  # ARM
  ami           = "ami-0cd7323ab3e63805f"
  instance_type = "c6g.medium"

  #  x86
  #  ami                     = "ami-0d7a109bf30624c99"
  #  instance_type           = "t2.micro"

  vpc_security_group_ids = [aws_security_group.instance.id]
  key_name               = "vockey"

  user_data                   = file("quarkus.sh")
  user_data_replace_on_change = true

  # TODO
  #   user_data = templatefile("${path.module}/user_data.tpl", {
  #   instance_ip = aws_instance.instance_a.private_ip
  # })

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

variable "security_group_name" {
  description = "The name of the security group"
  type        = string
  default     = "terraform-quarkus-customer"
}
