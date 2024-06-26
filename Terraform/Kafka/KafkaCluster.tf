resource "aws_instance" "kafka_broker" {
  ami                    = "ami-0cf10cdf9fcd62d37"
  instance_type          = "t2.small"
  count                  = 1
  vpc_security_group_ids = [aws_security_group.instance.id]
  key_name               = "vockey"

  user_data = file("${path.module}/creation.sh")

  user_data_replace_on_change = true

  tags = {
    Name = "terraform-kafka"
  }
}

resource "aws_security_group" "instance" {
  name = var.security_group_name
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 2181
    to_port     = 2181
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 9092
    to_port     = 9092
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
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
  default     = "terraform-kafka-broker"
}

output "kafka_broker_url" {
  value = aws_instance.kafka_broker[0].public_dns
}
