#!/bin/bash

cd "$(dirname "$0")" || exit
cd .. # Back to the root directory
echo "Runnind script from: $(pwd)"
dirname "$0"
cd ..

find . -name "*.tf" -type f -delete
find . -name "*.tfvars" -type f -delete
find . -name "*.tfstate*" -type f -delete

find . -name "*.terraform" -type d -exec rm -rf {} \;
