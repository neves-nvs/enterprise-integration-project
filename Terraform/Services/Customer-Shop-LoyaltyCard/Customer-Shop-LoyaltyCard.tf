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
  default     = "terraform-quarkus-customershop-loyaltycard-instance"
}

variable "key_name" {
  description = "The name of the key pair"
  type        = string
}

resource "aws_instance" "customer-shop-loyaltycard" {
  # ARM
  ami           = "ami-0cd7323ab3e63805f"
  instance_type = "c6g.medium"

  #  x86
  #  ami                     = "ami-0d7a109bf30624c99"
  #  instance_type           = "t2.micro"

  vpc_security_group_ids = [aws_security_group.instance.id]
  key_name               = var.key_name

  user_data = templatefile("${path.module}/services.sh", {
    rds_dns        = var.rds_dns
    dockerhub_user = var.dockerhub_user
  })
  user_data_replace_on_change = true

  tags = {
    Name = "terraform-instance-quarkus-customer-shop-loyaltycard"
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

output "public_dns" {
  value = aws_instance.customer-shop-loyaltycard.public_dns
}
