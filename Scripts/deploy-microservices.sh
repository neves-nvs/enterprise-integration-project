#!/bin/bash

esc=$'\e'

(
    cd Terraform/Services || exit

    (
        cd Customer || exit
        terraform init
        terraform apply -auto-approve
        #terraform state show 'aws_instance.exampleDeployQuarkus' |grep public_dns
        echo "MICROSERVICE customer IS AVAILABLE HERE:"
        addressMS="$(terraform state show aws_instance.exampleDeployQuarkus | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
        echo "http://${addressMS}:8080/q/swagger-ui/"
        echo
    )

    (
        cd Shop || exit
        terraform init
        terraform apply -auto-approve
        #terraform state show 'aws_instance.exampleDeployQuarkus' |grep public_dns
        echo "MICROSERVICE shop IS AVAILABLE HERE:"
        addressMS="$(terraform state show aws_instance.exampleDeployQuarkus | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
        echo "http://${addressMS}:8080/q/swagger-ui/"
        echo
    )

    (
        cd LoyaltyCard || exit
        terraform init
        terraform apply -auto-approve
        #terraform state show 'aws_instance.exampleDeployQuarkus' |grep public_dns
        echo "MICROSERVICE loyaltycard IS AVAILABLE HERE:"
        addressMS="$(terraform state show aws_instance.exampleDeployQuarkus | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
        echo "http://${addressMS}:8080/q/swagger-ui/"
        echo
    )

    (
        cd Purchase || exit
        terraform init
        terraform apply -auto-approve
        #terraform state show 'aws_instance.exampleDeployQuarkus' |grep public_dns
        echo "MICROSERVICE purchase IS AVAILABLE HERE:"
        addressMS="$(terraform state show aws_instance.exampleDeployQuarkus | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
        echo "http://${addressMS}:8080/q/swagger-ui/"
        echo
    )

    (
        cd DiscountCoupon || exit
        terraform init
        terraform apply -auto-approve
        #terraform state show 'aws_instance.exampleDeployQuarkus' |grep public_dns
        echo "MICROSERVICE dicsountCoupon IS AVAILABLE HERE:"
        addressMS="$(terraform state show aws_instance.exampleDeployQuarkus | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
        echo "http://${addressMS}:8080/q/swagger-ui/"
        echo
    )

    (
        cd CrossSellingRecommendation || exit
        terraform init
        terraform apply -auto-approve
        #terraform state show 'aws_instance.exampleDeployQuarkus' |grep public_dns
        echo "MICROSERVICE selledProduct IS AVAILABLE HERE:"
        addressMS="$(terraform state show aws_instance.exampleDeployQuarkus | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
        echo "http://${addressMS}:8080/q/swagger-ui/"
        echo
    )

    (
        cd SelledProduct || exit
        terraform init
        terraform apply -auto-approve
        #terraform state show 'aws_instance.exampleDeployQuarkus' |grep public_dns
        echo "MICROSERVICE selledProduct IS AVAILABLE HERE:"
        addressMS="$(terraform state show aws_instance.exampleDeployQuarkus | grep public_dns | sed "s/public_dns//g" | sed "s/=//g" | sed "s/\"//g" | sed "s/ //g" | sed "s/$esc\[[0-9;]*m//g")"
        echo "http://${addressMS}:8080/q/swagger-ui/"
        echo
    )
)
