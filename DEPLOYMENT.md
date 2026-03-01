# Deployment Guide

This guide explains how to deploy the BAPS Bal Mandal Attendance Management System to Docker and Kubernetes while securely handling GCP service account credentials.

## Overview

The application supports multiple methods for providing GCP credentials (in priority order):

1. **`GCP_CREDENTIALS_JSON`** - JSON as environment variable (recommended for Docker Compose)
2. **`GCP_CREDENTIALS_PATH`** - Path to mounted secret file (recommended for Kubernetes)
3. **`GOOGLE_APPLICATION_CREDENTIALS`** - Standard GCP environment variable
4. **`gcp.credentials.path`** property - Classpath or file path (for local development)
5. **Application Default Credentials** - For GCP environments (Cloud Run, GKE with Workload Identity)

## Local Development

Keep using `src/main/resources/gcs-service-account.json` (already in `.gitignore`):

```properties
# application.properties
gcp.credentials.path=classpath:gcs-service-account.json
```

## Docker Deployment

### Option 1: Environment Variable (JSON String)

```bash
# Build image
docker build -t balbalika-management-system:latest .

# Run with credentials as environment variable
docker run -d \
  -p 3900:3900 \
  -e GCP_CREDENTIALS_JSON='{"type":"service_account",...}' \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=yourpassword \
  balbalika-management-system:latest
```

**Note:** For JSON with special characters, use a `.env` file or escape properly.

### Option 2: Mount Secret File

```bash
# Create secrets directory
mkdir -p secrets
cp src/main/resources/gcs-service-account.json secrets/

# Run with mounted file
docker run -d \
  -p 3900:3900 \
  -v $(pwd)/secrets:/app/secrets:ro \
  -e GCP_CREDENTIALS_PATH=/app/secrets/gcs-service-account.json \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=yourpassword \
  balbalika-management-system:latest
```

### Using Docker Compose

1. Edit `docker-compose.yml`
2. Uncomment and set `GCP_CREDENTIALS_JSON` OR mount the file (see volumes section)
3. Run: `docker-compose up -d`

## Kubernetes Deployment

### Step 1: Create GCP Service Account Secret

```bash
# Base64 encode your service account JSON
cat src/main/resources/gcs-service-account.json | base64 -w 0

# Create secret.yaml (copy from k8s/secret.yaml.example)
cp k8s/secret.yaml.example k8s/secret.yaml

# Edit k8s/secret.yaml and replace <BASE64_ENCODED_JSON>
# Then apply:
kubectl apply -f k8s/secret.yaml
```

### Step 2: Create Other Secrets

```bash
# Database credentials
kubectl create secret generic db-credentials \
  --from-literal=url=jdbc:mysql://your-mysql-host:3306/bapsdelhibalmandal \
  --from-literal=username=root \
  --from-literal=password=your-secure-password

# Application secrets
kubectl create secret generic app-secrets \
  --from-literal=jwt-secret=your-secure-jwt-secret-key
```

### Step 3: Update Deployment

Edit `k8s/deployment.yaml`:
- Update `image` with your container registry
- Update `CORS_ALLOWED_ORIGINS`
- Adjust resource limits

### Step 4: Deploy

```bash
kubectl apply -f k8s/deployment.yaml
```

### Step 5: Verify

```bash
kubectl get pods
kubectl get services
kubectl logs -f deployment/balbalika-management-system
```

## GKE Workload Identity (Recommended)

For Google Kubernetes Engine, use Workload Identity instead of service account keys:

1. Create GCP Service Account with Storage permissions
2. Enable Workload Identity on your GKE cluster
3. Bind Kubernetes Service Account to GCP Service Account
4. Remove `GCP_CREDENTIALS_PATH`/`GCP_CREDENTIALS_JSON` from deployment
5. Application will use Application Default Credentials automatically

See: https://cloud.google.com/kubernetes-engine/docs/how-to/workload-identity

## Security Best Practices

1. ✅ **Never commit secrets to git** - Already in `.gitignore`
2. ✅ **Use Kubernetes Secrets** - Never hardcode in deployment YAML
3. ✅ **Use Workload Identity on GKE** - Avoids managing keys
4. ✅ **Rotate secrets regularly**
5. ✅ **Use RBAC** - Restrict access to secrets
6. ✅ **Enable encryption at rest** - For etcd (Kubernetes secret storage)

## Troubleshooting

### "Credentials not found" error

1. Check environment variables: `kubectl exec <pod-name> -- env | grep GCP`
2. Verify secret is mounted: `kubectl exec <pod-name> -- ls -la /app/secrets`
3. Check secret exists: `kubectl get secret gcp-service-account`
4. Verify JSON format: `kubectl exec <pod-name> -- cat /app/secrets/credentials.json | jq .`

### "Permission denied" error

1. Verify service account has Storage permissions in GCP
2. Check bucket name is correct: `GCP_BUCKET_NAME` env var
3. Verify service account email matches the one in credentials
