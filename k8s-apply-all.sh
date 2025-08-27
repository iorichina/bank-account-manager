#!/bin/bash
# 一键部署所有K8S资源
echo "Applying ConfigMap..."
kubectl apply -f k8s-configmap.yaml
echo "Applying Secret..."
kubectl apply -f k8s-secret.yaml
echo "Applying Deployment..."
kubectl apply -f k8s-deployment.yaml
echo "Applying Service..."
kubectl apply -f k8s-service.yaml
echo "Applying Ingress..."
kubectl apply -f k8s-ingress.yaml
echo "All resources applied."

