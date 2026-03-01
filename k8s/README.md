# Kubernetes Deployment

This directory contains Kubernetes manifests for deploying the BAPS Bal Mandal Attendance Management System to a test/production environment.

## Files

- `deployment.yaml` - Backend application deployment and service
- `mysql-deployment.yaml` - MySQL database deployment
- `configmap.yaml` - Non-sensitive configuration
- `secrets.yaml` - Template for sensitive data (DO NOT commit actual secrets!)
- `ingress.yaml` - Ingress for external access via domain
- `secret.yaml.example` - Example GCP secret (template)

## Quick Start

See **[QUICK_START.md](./QUICK_START.md)** for a fast deployment guide.

## Detailed Guide

See **[K8S_DEPLOYMENT_GUIDE.md](./K8S_DEPLOYMENT_GUIDE.md)** for comprehensive deployment instructions.

## Architecture

```
Internet → Ingress → Backend Service → MySQL Service
                ↓
         Backend Pods (2+ replicas)
```

## Key Components

1. **MySQL Deployment** - Database with persistent storage
2. **Backend Deployment** - Spring Boot application with:
   - Database connection
   - GCP credentials for photo storage
   - Twilio credentials for SMS OTP
   - JWT secret for authentication
3. **Services** - Internal networking
4. **Ingress** - External access via domain name

## Security Notes

- ✅ Secrets are stored in Kubernetes Secrets (not in YAML files)
- ✅ `secrets.yaml` is in `.gitignore` - never commit actual secrets
- ✅ Use environment variables or secret management tools in production
- ✅ Consider using Workload Identity on GKE instead of service account keys
