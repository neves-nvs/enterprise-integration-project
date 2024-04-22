variable "camunda_folder" {
  description = "The source code of the Quarkus project"
  type        = string
  default     = "../Camunda/"
}

resource "aws_instance" "exampleInstallCamundaEngine" {
  ami                    = "ami-0b5eea76982371e91"
  instance_type          = "t2.small"
  vpc_security_group_ids = [aws_security_group.instance.id]
  key_name               = "vockey"

  user_data = file("${var.camunda_folder}deploy.sh")

  user_data_replace_on_change = true

  tags = {
    Name = "terraform-example-Camunda"
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
  default     = "terraform-Camunda-instance2"
}

output "address" {
  value       = aws_instance.exampleInstallCamundaEngine.public_dns
  description = "Connect to the database at this endpoint"
}
