# GitHub Actions CI/CD Setup Guide

This guide explains how to set up GitHub Actions workflows for automated build and deployment of the BAPS Bal Mandal Attendance Management System.

## Overview

The repository includes GitHub Actions workflows for:
1. **Backend** - Build Docker image, push to registry, deploy to Kubernetes
2. **MySQL** - Deploy MySQL database to Kubernetes
3. **Backend and MySQL** - Deploy both in sequence

**Note:** Frontend is in a separate repository and should have its own CI/CD workflow.

## Prerequisites

1. **GitHub Repository** with code pushed
2. **Kubernetes Cluster** accessible from GitHub Actions
3. **Container Registry** (Docker Hub, GHCR, GCR, ECR, etc.)
4. **kubectl** access configured

## Step 1: Configure GitHub Secrets

Go to your GitHub repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

### Required Secrets

#### Container Registry
- `DOCKER_USERNAME` - Your Docker/registry username
- `DOCKER_PASSWORD` - Your Docker/registry password or access token

#### Kubernetes Access
- `KUBECONFIG` - Base64 encoded kubeconfig file
  ```bash
  # Get your kubeconfig and encode it
  cat ~/.kube/config | base64 -w 0
  # Paste the output as KUBECONFIG secret
  ```

#### Database
- `DB_PASSWORD` - MySQL root password

#### Application Secrets
- `JWT_SECRET` - JWT signing secret (min 32 characters)

#### Twilio SMS
- `TWILIO_ACCOUNT_SID` - Your Twilio Account SID
- `TWILIO_AUTH_TOKEN` - Your Twilio Auth Token
- `TWILIO_FROM_NUMBER` - Your Twilio phone number (+14352654195)

#### GCP (for photo storage)
- `GCP_CREDENTIALS_JSON` - Base64 encoded GCP service account JSON
  ```bash
  cat src/main/resources/gcs-service-account.json | base64 -w 0
  ```

#### Frontend Configuration (for CORS)
- `CORS_ALLOWED_ORIGINS` - Comma-separated frontend URLs (e.g., `https://app.your-domain.com,http://localhost:3000`)
  - This is used by the backend to allow frontend requests
  - Update this when you know your frontend deployment URL

## Step 2: Configure GitHub Environments (Optional)

For environment-specific configurations (test, staging, production):

1. Go to **Settings** → **Environments**
2. Create environments: `test`, `staging`, `production`
3. Add environment-specific secrets
4. Update workflow files to use `environment: test` (or staging/production)

## Step 3: Update Workflow Files

### Backend Workflow (`.github/workflows/backend-deploy.yml`)

Update these values:
```yaml
env:
  REGISTRY: docker.io  # Change to ghcr.io, gcr.io, etc.
  IMAGE_NAME: your-org/balbalika-backend  # Your image name
```

### Frontend Deployment

**Note:** Frontend is in a separate repository. Create a similar workflow in the frontend repository that:
1. Builds the frontend application
2. Deploys to Kubernetes
3. Updates the frontend service/deployment

You can reference this backend workflow as a template.

## Step 4: Workflow Triggers

Workflows trigger on:
- **Push to main/develop** - Full deployment
- **Pull Request** - Build and test only (no deployment)
- **Manual trigger** - Via `workflow_dispatch`

## Workflow Files

### 1. Backend Deploy (`backend-deploy.yml`)

**What it does:**
1. Runs Maven tests
2. Builds Docker image
3. Pushes to container registry
4. Creates/updates Kubernetes secrets
5. Deploys MySQL (if not exists)
6. Deploys backend application
7. Updates ingress

**Triggers:** Push to main/develop, PR (test only)

### 2. MySQL Deploy (`mysql-deploy.yml`)

**What it does:**
1. Creates database secrets
2. Deploys MySQL with persistent storage
3. Initializes database (first time only)
4. Runs seed scripts

**Triggers:** Push to main/develop, manual

### 3. Backend and MySQL Deploy (`full-deploy.yml`)

**What it does:**
1. Deploys MySQL first
2. Deploys backend (waits for MySQL)

**Triggers:** Push to main, manual

**Note:** Frontend deployment should be handled in the frontend repository's CI/CD pipeline.

## Step 5: First Deployment

### Option A: Manual Trigger

1. Go to **Actions** tab in GitHub
2. Select **Full Stack Deploy** workflow
3. Click **Run workflow**
4. Select branch and click **Run workflow**

### Option B: Push to Main

```bash
git checkout main
git push origin main
```

Workflows will automatically trigger.

## Monitoring Deployments

### View Workflow Runs

1. Go to **Actions** tab
2. Click on a workflow run
3. View logs for each step

### Check Kubernetes Deployment

```bash
# After deployment, check status
kubectl get pods
kubectl get services
kubectl get ingress

# View logs
kubectl logs -f deployment/balbalika-management-system
kubectl logs -f deployment/frontend
kubectl logs -f deployment/mysql
```

## Troubleshooting

### Workflow Fails at Build

- Check Maven/Node.js versions
- Verify dependencies in `pom.xml`/`package.json`
- Check build logs for errors

### Workflow Fails at Docker Push

- Verify `DOCKER_USERNAME` and `DOCKER_PASSWORD` secrets
- Check registry permissions
- Verify image name format

### Workflow Fails at Kubernetes Deploy

- Verify `KUBECONFIG` secret is correct
- Check Kubernetes cluster accessibility
- Verify namespace exists
- Check resource quotas

### MySQL Deployment Fails

- Check if PVC already exists
- Verify storage class is available
- Check MySQL pod logs

### Backend Can't Connect to MySQL

- Verify MySQL service is running: `kubectl get pods -l app=mysql`
- Check database credentials in secrets
- Verify network policies allow connection

## Customization

### Change Container Registry

Update `REGISTRY` in workflow files:
- **Docker Hub**: `docker.io`
- **GitHub Container Registry**: `ghcr.io`
- **Google Container Registry**: `gcr.io`
- **Amazon ECR**: `your-account.dkr.ecr.region.amazonaws.com`

### Change Deployment Branch

Update `branches` in workflow triggers:
```yaml
on:
  push:
    branches:
      - main
      - production  # Add your production branch
```

### Add Environment-Specific Configs

1. Create GitHub Environments (test, staging, production)
2. Add environment-specific secrets
3. Update workflow to use environment:
   ```yaml
   environment: production
   ```

### Frontend in Separate Repo

Create a composite workflow that:
1. Checks out backend repo
2. Checks out frontend repo
3. Builds both
4. Deploys both

## Security Best Practices

1. ✅ **Use GitHub Secrets** - Never hardcode credentials
2. ✅ **Use Environments** - Separate test/staging/production
3. ✅ **Limit Permissions** - Use least privilege for GitHub tokens
4. ✅ **Rotate Secrets** - Regularly update passwords/tokens
5. ✅ **Review Workflows** - Audit workflow files before merging
6. ✅ **Use OIDC** - For cloud provider authentication (GCP, AWS, Azure)

## Advanced: Using OIDC for Cloud Authentication

Instead of storing `KUBECONFIG`, use OIDC:

### For GKE:
```yaml
- name: Authenticate to Google Cloud
  uses: google-github-actions/auth@v1
  with:
    credentials_json: ${{ secrets.GCP_SA_KEY }}

- name: Set up Cloud SDK
  uses: google-github-actions/setup-gcloud@v1

- name: Configure kubectl
  run: gcloud container clusters get-credentials CLUSTER_NAME --region REGION
```

### For EKS:
```yaml
- name: Configure AWS credentials
  uses: aws-actions/configure-aws-credentials@v2
  with:
    role-to-assume: ${{ secrets.AWS_ROLE_ARN }}

- name: Configure kubectl
  run: aws eks update-kubeconfig --name CLUSTER_NAME --region REGION
```

## Next Steps

1. Set up GitHub Secrets
2. Test workflow with manual trigger
3. Configure environments for test/staging/production
4. Set up monitoring and alerts
5. Configure branch protection rules
6. Set up automated testing

## Support

- GitHub Actions Docs: https://docs.github.com/en/actions
- Kubernetes Docs: https://kubernetes.io/docs/
- Workflow syntax: https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions
