# variable "quarkus_src_folder" {
#   description = "The source code of the Quarkus project"
#   type        = string
#   default     = "../../quarkus"
# }

# resource "aws_instance" "Quarkus" {
#   # Amazon Linux ARM AMI built by Amazon Web Services - FOR DOCKER image COMPATIBILITY
#   ami                    = "ami-0cd7323ab3e63805f"
#   instance_type          = "c6g.medium"
#   vpc_security_group_ids = [aws_security_group.instance.id]
#   key_name               = "vockey"

#   user_data                   = file("${var.quarkus_src_folder}/quarkus.sh")
#   user_data_replace_on_change = true

#   tags = {
#     Name = "terraform-deploy-QuarkusProject"
#   }
# }

# resource "aws_security_group" "instance" {
#   name = var.security_group_name
#   ingress {
#     from_port        = 0
#     to_port          = 0
#     protocol         = "-1"
#     cidr_blocks      = ["0.0.0.0/0"]
#     ipv6_cidr_blocks = ["::/0"]
#   }
#   egress {
#     from_port        = 0
#     to_port          = 0
#     protocol         = "-1"
#     cidr_blocks      = ["0.0.0.0/0"]
#     ipv6_cidr_blocks = ["::/0"]
#   }
# }

# variable "security_group_name" {
#   description = "The name of the security group"
#   type        = string
#   default     = "terraform-Quarkus-instance"
# }

# output "address" {
#   value       = aws_instance.exampleDeployQuarkus.public_dns
#   description = "Address of the Quarkus EC2 machine"
# }
