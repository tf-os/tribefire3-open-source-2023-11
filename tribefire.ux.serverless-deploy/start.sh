#!/bin/bash
source .env
export $(cut -d= -f1 .env)

cd serverless-template

if [ -z ${1} ]; 
then echo "action is unset. serverless.yml file is ready to use."; 
else 
    echo "run yarn '$1'"
    yarn $1
fi
