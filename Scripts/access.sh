#!/bin/bash

AWS_ACCESS_KEY_ID=''
AWS_SECRET_ACCESS_KEY=''
AWS_SESSION_TOKEN=''

if [ ! -f ~/.aws/credentials ]; then
    echo "AWS credentials file not found. Setting environment variables from access.sh"
    export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
    export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
    export AWS_SESSION_TOKEN=$AWS_SESSION_TOKEN
else
    echo "AWS credentials file found. Using environment variables from ~/.aws/credentials"
fi
