# Terraform (Infrastructure as Code)

## Deploy

```sh
terraform apply -auto-aprove
```

## Undeploy

- Everything

```sh
terraform destroy
```

- Targeted

```sh
terraform state list
```

```sh
terraform destroy -target=<destroy_target>
```
