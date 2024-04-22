# module "quarkus" {
#   source = "../services"
# }

module "camunda" {
  source = "./EC2CamundaEngine"
}
