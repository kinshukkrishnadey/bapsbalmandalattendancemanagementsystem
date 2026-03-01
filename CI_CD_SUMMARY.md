# CI/CD Pipeline Summary

## Overview

GitHub Actions workflows have been created for automated build and deployment of:
- ✅ **Backend** (Spring Boot)
- ✅ **MySQL** (Database)

**Note:** Frontend is in a separate repository and should have its own CI/CD workflow.

## Workflow Files Created

### 1. `.github/workflows/backend-deploy.yml`
- Builds Spring Boot application
- Runs Maven tests
- Builds Docker image
- Pushes to container registry
- Deploys to Kubernetes
- Creates/updates secrets and configs

### 2. `.github/workflows/mysql-deploy.yml`
- Deploys MySQL database
- Creates persistent storage
- Initializes database schema
- Runs seed scripts

### 3. `.github/workflows/full-deploy.yml`
- Orchestrates backend deployment
- Deploys: MySQL → Backend

## Quick Setup Steps

### 1. Configure GitHub Secrets

See `.github/SECRETS_CHECKLIST.md` for complete list.

**Minimum required:**
- `DOCKER_USERNAME` / `DOCKER_PASSWORD`
- `KUBECONFIG` (base64 encoded)
- `DB_PASSWORD`
- `JWT_SECRET`
- `TWILIO_ACCOUNT_SID` / `TWILIO_AUTH_TOKEN` / `TWILIO_FROM_NUMBER`
- `GCP_CREDENTIALS_JSON` (base64 encoded)
- `CORS_ALLOWED_ORIGINS` (for allowing frontend requests)

### 2. Update Workflow Configuration

Edit `.github/workflows/backend-deploy.yml`:
```yaml
env:
  REGISTRY: docker.io  # Change to your registry
  IMAGE_NAME: your-org/balbalika-backend  # Your image name
```

### 3. Frontend Deployment

Since frontend is in a separate repository:
- Create a similar CI/CD workflow in the frontend repository
- The frontend workflow should deploy to the same Kubernetes cluster
- Update `CORS_ALLOWED_ORIGINS` secret with the frontend URL after deployment

### 4. First Deployment

**Option A: Manual Trigger**
1. Go to **Actions** tab
2. Select **Full Stack Deploy**
3. Click **Run workflow**

**Option B: Push to Main**
```bash
git push origin main
```

## What Happens on Deployment

1. **MySQL Deploys First**
   - Creates persistent volume
   - Initializes database
   - Runs seed scripts

2. **Backend Deploys**
   - Builds Docker image
   - Pushes to registry
   - Creates Kubernetes secrets
   - Deploys application
   - Connects to MySQL

3. **Frontend** (deployed separately from frontend repository)
   - Should be deployed from the frontend repository's CI/CD pipeline
   - After frontend is deployed, update `CORS_ALLOWED_ORIGINS` secret with the frontend URL

## Access Your Application

After deployment:

```bash
# Get external IP/URL
kubectl get service balbalika-management-system
kubectl get ingress balbalika-ingress

# Or port-forward for testing
kubectl port-forward service/balbalika-management-system 3900:80
```

## Documentation

- **Setup Guide**: `.github/GITHUB_ACTIONS_SETUP.md`
- **Secrets Checklist**: `.github/SECRETS_CHECKLIST.md`
- **Kubernetes Guide**: `k8s/K8S_DEPLOYMENT_GUIDE.md`
- **Quick Start**: `k8s/QUICK_START.md`

## Next Steps

1. ✅ Add all GitHub Secrets (see checklist)
2. ✅ Update workflow registry/image names
3. ✅ Configure frontend path (if different)
4. ✅ Test with manual workflow trigger
5. ✅ Set up branch protection rules
6. ✅ Configure environments (test/staging/production)

## Support

- GitHub Actions: https://docs.github.com/en/actions
- Kubernetes: https://kubernetes.io/docs/
- Docker: https://docs.docker.com/
