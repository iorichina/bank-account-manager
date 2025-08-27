#!/bin/bash
# 一键删除所有K8S资源
echo "Deleting Ingress..."
kubectl delete -f k8s-ingress.yaml
echo "Deleting Service..."
kubectl delete -f k8s-service.yaml
echo "Deleting Deployment..."
kubectl delete -f k8s-deployment.yaml
echo "Deleting Secret..."
kubectl delete -f k8s-secret.yaml
echo "Deleting ConfigMap..."
kubectl delete -f k8s-configmap.yaml
echo "All resources deleted."

