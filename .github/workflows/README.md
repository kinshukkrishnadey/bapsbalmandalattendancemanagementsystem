# GitHub Actions Workflows

This directory contains CI/CD workflows for automated build and deployment.

## Workflows

### 1. `backend-deploy.yml`
- Builds Spring Boot backend
- Runs Maven tests
- Builds and pushes Docker image
- Deploys to Kubernetes
- Updates MySQL if needed

### 2. `mysql-deploy.yml`
- Deploys MySQL database
- Creates persistent storage
- Initializes database schema

### 3. `full-deploy.yml` (Backend and MySQL)
- Orchestrates backend deployment
- Deploys MySQL → Backend in sequence

**Note:** Frontend is in a separate repository and should have its own CI/CD workflow.

## Setup

See **[GITHUB_ACTIONS_SETUP.md](../GITHUB_ACTIONS_SETUP.md)** for complete setup instructions.

## Quick Start

1. Configure GitHub Secrets (see setup guide)
2. Push to `main` branch to trigger deployment
3. Or manually trigger from Actions tab

## Workflow Triggers

- **Push to main/develop** - Automatic deployment
- **Pull Request** - Build and test only
- **Manual** - Via workflow_dispatch
