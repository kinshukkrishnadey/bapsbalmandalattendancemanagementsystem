# GitHub Secrets Checklist

Use this checklist to ensure all required secrets are configured in your GitHub repository.

## How to Add Secrets

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret from the list below

## Required Secrets

### Container Registry
- [ ] `DOCKER_USERNAME` - Your Docker Hub/registry username
- [ ] `DOCKER_PASSWORD` - Your Docker Hub/registry password or access token

### Kubernetes Access
- [ ] `KUBECONFIG` - Base64 encoded kubeconfig file
  ```bash
  # Generate:
  cat ~/.kube/config | base64 -w 0
  # Or on Windows PowerShell:
  [Convert]::ToBase64String([System.IO.File]::ReadAllBytes("$env:USERPROFILE\.kube\config"))
  ```

### Database
- [ ] `DB_PASSWORD` - MySQL root password (secure, min 12 characters)

### Application
- [ ] `JWT_SECRET` - JWT signing secret (min 32 characters, use random string)

### Twilio SMS
- [ ] `TWILIO_ACCOUNT_SID` - Your Twilio Account SID (e.g., ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx)
- [ ] `TWILIO_AUTH_TOKEN` - Your Twilio Auth Token
- [ ] `TWILIO_FROM_NUMBER` - Your Twilio phone number (e.g., +15551234567)

### GCP (for photo storage)
- [ ] `GCP_CREDENTIALS_JSON` - Base64 encoded GCP service account JSON
  ```bash
  # Generate:
  cat src/main/resources/gcs-service-account.json | base64 -w 0
  ```

### Frontend Configuration (for CORS)
- [ ] `CORS_ALLOWED_ORIGINS` - Comma-separated frontend URLs (e.g., https://app.your-domain.com,http://localhost:3000)
  - This allows the frontend to make API requests to the backend
  - Update this after frontend is deployed

## Optional: Environment-Specific Secrets

If using GitHub Environments (test, staging, production), add environment-specific secrets:

### Test Environment
- [ ] `DB_PASSWORD` (test)
- [ ] `JWT_SECRET` (test)
- [ ] `CORS_ALLOWED_ORIGINS` (test - frontend URL)

### Production Environment
- [ ] `DB_PASSWORD` (production)
- [ ] `JWT_SECRET` (production)
- [ ] `CORS_ALLOWED_ORIGINS` (production - frontend URL)

## Verification

After adding all secrets, verify:

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Confirm all secrets are listed
3. Test workflow with manual trigger
4. Check workflow logs for any missing secret errors

## Security Notes

- ✅ Secrets are encrypted at rest
- ✅ Secrets are masked in logs
- ✅ Never commit secrets to code
- ✅ Rotate secrets regularly
- ✅ Use different secrets for test/production

## Quick Test

After adding secrets, trigger a workflow manually:

1. Go to **Actions** tab
2. Select a workflow
3. Click **Run workflow**
4. Check logs for any missing secret errors
