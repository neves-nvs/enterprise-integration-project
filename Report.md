# Report Enterprise Integration Project - Sprint 2

<!-- TODO -->

In this report, we will test all microservices to assess the effectiveness of their methods by accessing each virtual machine. We will utilize Swagger-UI flags to correspond with each service, enabling thorough examination of their functionalities.

## Group

| Nome               | Número |
| ------------------ | ------ |
| **Diogo Ferreira** | 95554  |
| **Diogo Borges**   | 99203  |

## Table of Contents

- [1. Business Processes](#1-business-processes)
- [2. Test Documentation](#2-test-documentation)
- [3. Other Documentation](#3-other-documentation)
  - [3.1. Source Code](#31-source-code)
  - [3.2. Scripting](#32-scripting)
  - [3.3. BPMN Files](#33-bpmn-files)
  - [3.4. Installation](#34-installation)
  - [3.5. Configuration (Parametrization)](#35-configuration-parametrization)

## 1. Business Processes

### Customer management

### Shop management

### Loyalty Card management

### Discount Coupon emission

### Cross Selling Recommendation

### Selled Product Analytics

## 2. Test Documentation

## 3. Other Documentation

### 3.1. Source Code

### 3.2. Scripting

### 3.3. BPMN Files

### 3.4. Installation

Terraform will be used to deploy the whole infrastructure directly. Given that we only need to run the following commands to deploy the whole infrastructure:

```bash
cd Terraform
Terraform init
Terraform apply --auto-approve
```

---

In need to destroy just a part of the infrastructure, now that everything is coupled under a single terraform, we must leverage terraform destroy in a more granular way.

If you have access to `fzf`, you can use the following script to destroy a specific resource:

> Be careful to not destroy a resource that is being used by another resource, as they may not be able to recover (this is recommended to destroy microservices and kong/konga)

```bash
terraform destroy -target=$(terraform state list | fzf)
```

if you don't have `fzf`, you can use the following script to destroy a specific resource:

```bash
terraform state list
# copy the resource you want to destroy
terraform destroy -target=<resource>
```

### 3.5. Configuration (Parametrization)
