# Quick Start: Deploy to Kubernetes Test Environment

## Prerequisites Checklist

- [ ] Kubernetes cluster running (GKE, EKS, AKS, Minikube, etc.)
- [ ] `kubectl` installed and configured
- [ ] Docker installed
- [ ] Container registry account (Docker Hub, GCR, ECR, etc.)
- [ ] Domain name (optional, can use LoadBalancer IP)

## Quick Deployment Steps

### 1. Build and Push Docker Image

```bash
# Build
docker build -t your-registry/balbalika-management-system:v1.0.0 .

# Push (replace with your registry)
docker push your-registry/balbalika-management-system:v1.0.0
```

**Registry Examples:**
- Docker Hub: `docker tag ... yourusername/balbalika-management-system:v1.0.0`
- GCR: `docker tag ... gcr.io/your-project/balbalika-management-system:v1.0.0`
- ECR: `docker tag ... your-account.dkr.ecr.region.amazonaws.com/balbalika-management-system:v1.0.0`

### 2. Create Secrets

```bash
# Database
kubectl create secret generic db-credentials \
  --from-literal=url=jdbc:mysql://mysql:3306/bapsdelhibalmandal \
  --from-literal=username=root \
  --from-literal=password=YOUR_SECURE_PASSWORD

# JWT Secret
kubectl create secret generic app-secrets \
  --from-literal=jwt-secret=YOUR_SECURE_JWT_SECRET_MIN_32_CHARS

# Twilio
kubectl create secret generic twilio-credentials \
  --from-literal=account-sid=YOUR_TWILIO_ACCOUNT_SID \
  --from-literal=auth-token=YOUR_TWILIO_AUTH_TOKEN \
  --from-literal=from-number=YOUR_TWILIO_FROM_NUMBER

# GCP (encode JSON first)
cat src/main/resources/gcs-service-account.json | base64 -w 0 | \
  kubectl create secret generic gcp-service-account \
  --from-file=credentials.json=/dev/stdin
```

### 3. Update ConfigMap

Edit `k8s/configmap.yaml`:
- Update `CORS_ALLOWED_ORIGINS` with your frontend URL
- Apply: `kubectl apply -f k8s/configmap.yaml`

### 4. Update Deployment Image

Edit `k8s/deployment.yaml`:
- Replace `your-registry/balbalika-management-system:latest` with your actual image

### 5. Deploy Everything

```bash
# Deploy MySQL
kubectl apply -f k8s/mysql-deployment.yaml

# Wait for MySQL (optional)
kubectl wait --for=condition=ready pod -l app=mysql --timeout=300s

# Deploy Backend
kubectl apply -f k8s/deployment.yaml

# Deploy Ingress (optional - for domain access)
kubectl apply -f k8s/ingress.yaml
```

### 6. Get Access URL

**Option A: LoadBalancer (Simplest)**
```bash
kubectl get service balbalika-management-system
# Use EXTERNAL-IP from output
```

**Option B: Ingress (Domain-based)**
```bash
kubectl get ingress balbalika-ingress
# Use ADDRESS from output
```

**Option C: Port Forward (Testing)**
```bash
kubectl port-forward service/balbalika-management-system 3900:80
# Access via http://localhost:3900
```

### 7. Test

```bash
# Test API
curl http://<YOUR_URL>/auth/request-otp?phone=7303764977
```

## Verify Deployment

```bash
# Check pods
kubectl get pods

# Check services
kubectl get services

# View logs
kubectl logs -f deployment/balbalika-management-system

# Check ingress
kubectl get ingress
```

## Troubleshooting

```bash
# Pod issues
kubectl describe pod <pod-name>
kubectl logs <pod-name>

# Service issues
kubectl get endpoints balbalika-management-system

# Database issues
kubectl logs -l app=mysql
```

## Next Steps

1. Update frontend to use the new API URL
2. Configure DNS to point to your LoadBalancer IP or Ingress
3. Set up SSL/TLS certificates
4. Configure monitoring and logging

For detailed instructions, see `k8s/K8S_DEPLOYMENT_GUIDE.md`
