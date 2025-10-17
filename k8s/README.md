# Kubernetes Deployment

Kubernetes manifests for deploying the Incident Response Coordinator application.

## Quick Start

### Deploy Everything
```bash
# Deploy all resources at once
kubectl apply -f k8s/all-in-one.yaml

# Wait for pods to be ready
kubectl get pods -n incident-response -w

# Get service external IP (for cloud providers)
kubectl get service -n incident-response
```

### Access the Application

**For Cloud (AWS EKS, Azure AKS, Google GKE):**
```bash
# Get LoadBalancer external IP
kubectl get service incident-coordinator-service -n incident-response

# Access at: http://<EXTERNAL-IP>/swagger-ui.html
```

**For Local (minikube):**
```bash
# Change service type to NodePort first
# Edit k8s/service.yaml: type: NodePort

# Get URL
minikube service incident-coordinator-service -n incident-response --url
```

## Individual Deployment
```bash
# Apply manifests individually
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

## Verify Deployment
```bash
# Check namespace
kubectl get namespace incident-response

# Check all resources
kubectl get all -n incident-response

# Check pods
kubectl get pods -n incident-response

# Check pod logs
kubectl logs -f deployment/incident-coordinator -n incident-response

# Describe pod (for troubleshooting)
kubectl describe pod <pod-name> -n incident-response
```

## Scaling
```bash
# Scale to 3 replicas
kubectl scale deployment incident-coordinator --replicas=3 -n incident-response

# Verify
kubectl get pods -n incident-response
```

## Update Deployment
```bash
# After building new Docker image
kubectl set image deployment/incident-coordinator \
  incident-coordinator=incident-coordinator:1.1.0 \
  -n incident-response

# Check rollout status
kubectl rollout status deployment/incident-coordinator -n incident-response
```

## Delete Everything
```bash
# Delete all resources
kubectl delete -f k8s/all-in-one.yaml

# Or delete namespace (deletes everything inside)
kubectl delete namespace incident-response
```

## Files

- **namespace.yaml** - Creates isolated namespace
- **configmap.yaml** - Application configuration
- **deployment.yaml** - Pod specification and replicas
- **service.yaml** - Load balancer and networking
- **all-in-one.yaml** - Combined manifest (easiest for demo)

## Production Considerations

For production deployment:

1. **Use external database** (not H2 in-memory)
   - AWS RDS PostgreSQL
   - Azure Database for PostgreSQL
   - Google Cloud SQL

2. **Add Ingress** for HTTPS and domain routing

3. **Configure resource limits** based on load testing

4. **Set up monitoring** (Prometheus, Grafana)

5. **Enable auto-scaling** (HorizontalPodAutoscaler)

6. **Use secrets** for sensitive data (not ConfigMap)

7. **Implement rolling updates** with zero downtime