# Kubernetes Deployment Guide for Test Environment

This guide will help you deploy the BAPS Bal Mandal Attendance Management System to Kubernetes for testing with multiple users.

## Prerequisites

1. **Kubernetes Cluster** (one of the following):
   - Google Kubernetes Engine (GKE)
   - Amazon EKS
   - Azure AKS
   - Minikube (for local testing)
   - Kind (Kubernetes in Docker)

2. **kubectl** installed and configured
3. **Docker** for building images
4. **Container Registry** (Docker Hub, GCR, ECR, etc.)

## Architecture

```
┌─────────────────┐
│   Ingress       │  (External access via URL)
│   (nginx/cloud) │
└────────┬────────┘
         │
┌────────▼────────┐
│  Backend API    │  (Spring Boot on port 3900)
│  Service        │
└────────┬────────┘
         │
┌────────▼────────┐
│  MySQL          │  (Database on port 3306)
│  Service        │
└─────────────────┘
```

## Step 1: Build and Push Docker Image

### Build the image:

```bash
# Build the Docker image
docker build -t balbalika-management-system:latest .

# Tag for your registry (replace with your registry)
docker tag balbalika-management-system:latest your-registry/balbalika-management-system:v1.0.0

# Push to registry
docker push your-registry/balbalika-management-system:v1.0.0
```

**Examples:**
- **Docker Hub**: `docker tag ... yourusername/balbalika-management-system:v1.0.0`
- **Google Container Registry**: `docker tag ... gcr.io/your-project/balbalika-management-system:v1.0.0`
- **Amazon ECR**: `docker tag ... your-account.dkr.ecr.region.amazonaws.com/balbalika-management-system:v1.0.0`

## Step 2: Create Secrets

### Option A: Using kubectl (Recommended)

```bash
# Database credentials
kubectl create secret generic db-credentials \
  --from-literal=url=jdbc:mysql://mysql:3306/bapsdelhibalmandal \
  --from-literal=username=root \
  --from-literal=password=YOUR_SECURE_PASSWORD

# Application secrets (JWT)
kubectl create secret generic app-secrets \
  --from-literal=jwt-secret=YOUR_SECURE_JWT_SECRET_MIN_32_CHARACTERS

# Twilio credentials
kubectl create secret generic twilio-credentials \
  --from-literal=account-sid=YOUR_TWILIO_ACCOUNT_SID \
  --from-literal=auth-token=YOUR_TWILIO_AUTH_TOKEN \
  --from-literal=from-number=YOUR_TWILIO_FROM_NUMBER

# GCP Service Account (base64 encode first)
cat src/main/resources/gcs-service-account.json | base64 -w 0 > /tmp/gcp-encoded.txt
kubectl create secret generic gcp-service-account \
  --from-file=credentials.json=/tmp/gcp-encoded.txt
```

### Option B: Using YAML file

1. Copy the example:
   ```bash
   cp k8s/secrets.yaml.example k8s/secrets.yaml
   ```

2. Edit `k8s/secrets.yaml` and replace all placeholders:
   - Database password
   - JWT secret
   - Twilio credentials
   - GCP credentials (base64 encoded)

3. Apply:
   ```bash
   kubectl apply -f k8s/secrets.yaml
   ```

## Step 3: Create ConfigMap

```bash
# Edit k8s/configmap.yaml and update:
# - CORS_ALLOWED_ORIGINS with your frontend URL
# - Other configuration as needed

kubectl apply -f k8s/configmap.yaml
```

## Step 4: Deploy MySQL

```bash
kubectl apply -f k8s/mysql-deployment.yaml
```

Wait for MySQL to be ready:
```bash
kubectl wait --for=condition=ready pod -l app=mysql --timeout=300s
```

## Step 5: Update Deployment Image

Edit `k8s/deployment.yaml` and update the image:

```yaml
image: your-registry/balbalika-management-system:v1.0.0
```

Replace `your-registry` with your actual container registry.

## Step 6: Deploy Backend Application

```bash
kubectl apply -f k8s/deployment.yaml
```

Check deployment status:
```bash
kubectl get pods
kubectl get services
```

## Step 7: Set Up Ingress (External Access)

### Option A: Cloud Provider Ingress (GKE, EKS, AKS)

1. Edit `k8s/ingress.yaml`:
   - Update `host` with your domain
   - Update annotations for your cloud provider
   - Configure TLS if needed

2. Apply:
   ```bash
   kubectl apply -f k8s/ingress.yaml
   ```

3. Get external IP:
   ```bash
   # GKE
   kubectl get ingress balbalika-ingress
   
   # Or check LoadBalancer service
   kubectl get service balbalika-management-system
   ```

### Option B: NGINX Ingress Controller (Minikube/On-prem)

1. Install NGINX Ingress:
   ```bash
   # Minikube
   minikube addons enable ingress
   
   # Or install via Helm
   helm install ingress-nginx ingress-nginx/ingress-nginx
   ```

2. Edit `k8s/ingress.yaml` and update host

3. Apply:
   ```bash
   kubectl apply -f k8s/ingress.yaml
   ```

4. Get IP:
   ```bash
   # Minikube
   minikube service balbalika-management-system
   
   # Or
   kubectl get ingress
   ```

### Option C: LoadBalancer Service (Simplest for Testing)

If you just need quick access, the deployment already creates a LoadBalancer service:

```bash
# Get external IP
kubectl get service balbalika-management-system

# Access via: http://<EXTERNAL_IP>
```

## Step 8: Verify Deployment

```bash
# Check pods
kubectl get pods

# Check services
kubectl get services

# Check ingress
kubectl get ingress

# View logs
kubectl logs -f deployment/balbalika-management-system

# Test API
curl http://<YOUR_EXTERNAL_IP>/auth/request-otp?phone=7303764977
```

## Step 9: Database Initialization

The application will auto-create tables on first startup (due to `spring.jpa.hibernate.ddl-auto=update`).

To seed initial data:

1. Port-forward to MySQL:
   ```bash
   kubectl port-forward service/mysql 3306:3306
   ```

2. Connect and run seed script:
   ```bash
   mysql -h 127.0.0.1 -u root -p < sql/seed-data.sql
   ```

## Configuration Summary

### Environment Variables (from ConfigMap):
- `CORS_ALLOWED_ORIGINS` - Frontend URLs
- `GCP_BUCKET_NAME` - GCS bucket name
- `SMS_PROVIDER_ENABLED` - Enable/disable SMS
- `APP_OTP_EXPIRY_SECONDS` - OTP expiration time

### Secrets:
- Database credentials
- JWT secret
- Twilio credentials
- GCP service account

## Updating Configuration

### Update ConfigMap:
```bash
kubectl edit configmap app-config
# Or
kubectl apply -f k8s/configmap.yaml
```

### Update Secrets:
```bash
kubectl edit secret <secret-name>
# Or recreate using kubectl create secret
```

### Rolling Update:
```bash
# Update image
kubectl set image deployment/balbalika-management-system \
  app=your-registry/balbalika-management-system:v1.0.1

# Or edit deployment
kubectl edit deployment balbalika-management-system
```

## Troubleshooting

### Pods not starting:
```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

### Database connection issues:
```bash
# Check MySQL pod
kubectl logs -l app=mysql

# Test connection
kubectl exec -it deployment/mysql -- mysql -u root -p
```

### Service not accessible:
```bash
# Check service endpoints
kubectl get endpoints balbalika-management-system

# Port forward for testing
kubectl port-forward service/balbalika-management-system 3900:80
```

### Ingress not working:
```bash
# Check ingress
kubectl describe ingress balbalika-ingress

# Check ingress controller logs
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
```

## Scaling

```bash
# Scale backend
kubectl scale deployment balbalika-management-system --replicas=3

# Auto-scaling (requires metrics server)
kubectl autoscale deployment balbalika-management-system \
  --min=2 --max=10 --cpu-percent=80
```

## Cleanup

```bash
# Delete all resources
kubectl delete -f k8s/

# Or delete individually
kubectl delete deployment balbalika-management-system
kubectl delete service balbalika-management-system
kubectl delete ingress balbalika-ingress
kubectl delete deployment mysql
kubectl delete service mysql
kubectl delete pvc mysql-pvc
kubectl delete configmap app-config
kubectl delete secret db-credentials app-secrets twilio-credentials gcp-service-account
```

## Production Considerations

1. **Use managed database** (Cloud SQL, RDS, etc.) instead of MySQL in K8s
2. **Enable TLS/SSL** for ingress
3. **Set up monitoring** (Prometheus, Grafana)
4. **Configure resource limits** appropriately
5. **Use Horizontal Pod Autoscaler**
6. **Set up backup** for database
7. **Use secrets management** (Vault, AWS Secrets Manager, etc.)
8. **Enable network policies** for security
9. **Use persistent volumes** with backup
10. **Set up CI/CD** pipeline

## Quick Start Commands

```bash
# 1. Build and push
docker build -t your-registry/balbalika-management-system:v1.0.0 .
docker push your-registry/balbalika-management-system:v1.0.0

# 2. Create secrets
kubectl create secret generic db-credentials --from-literal=password=YOUR_PASSWORD
kubectl create secret generic app-secrets --from-literal=jwt-secret=YOUR_JWT_SECRET
kubectl create secret generic twilio-credentials \
  --from-literal=account-sid=YOUR_SID \
  --from-literal=auth-token=YOUR_TOKEN \
  --from-literal=from-number=YOUR_NUMBER

# 3. Deploy
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/ingress.yaml

# 4. Get URL
kubectl get ingress
# Or
kubectl get service balbalika-management-system
```
