#!/usr/bin/env bash

cluster=tribefire
account_id=1234567890

aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file://iam_policy.json

eksctl utils associate-iam-oidc-provider --region=eu-west-1 --cluster=${cluster}--approve
eksctl create iamserviceaccount \
  --cluster=${cluster} \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --role-name "AmazonEKSLoadBalancerControllerRole" \
  --attach-policy-arn=arn:aws:iam::${account_id}:policy/AWSLoadBalancerControllerIAMPolicy \
  --approve

helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm upgrade aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=${cluster} \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller