# Azure DevOps Setup Guide - Order Management System

## Overview

This guide covers the complete setup of Azure DevOps for end-to-end SDLC automation of the OMS application.

**Organization:** HOLMES-APPS  
**Project:** WINGS-POC  
**URL:** https://dev.azure.com/HOLMES-APPS/WINGS-POC

---

## Table of Contents

1. [Azure DevOps Project Setup](#1-azure-devops-project-setup)
2. [Azure Repos Configuration](#2-azure-repos-configuration)
3. [Branch Policies](#3-branch-policies)
4. [Azure Pipelines Setup](#4-azure-pipelines-setup)
5. [Service Connections](#5-service-connections)
6. [Environments & Approvals](#6-environments--approvals)
7. [Azure Boards Integration](#7-azure-boards-integration)
8. [Variable Groups & Secrets](#8-variable-groups--secrets)
9. [Dashboard Setup](#9-dashboard-setup)

---

## 1. Azure DevOps Project Setup

### Access the Project
```
https://dev.azure.com/HOLMES-APPS/WINGS-POC
```

### Project Settings to Configure
1. Navigate to **Project Settings** (bottom left gear icon)
2. Configure the following:

| Setting | Value |
|---------|-------|
| Version Control | Git |
| Work Item Process | Agile (or Scrum) |
| Visibility | Private |

---

## 2. Azure Repos Configuration

### Option A: Import from GitHub
1. Go to **Repos** > **Files**
2. Click **Import Repository**
3. Enter GitHub URL: `https://github.com/your-org/SDLC_Demo.git`
4. Click **Import**

### Option B: Push Existing Code
```bash
# Add Azure DevOps as remote
git remote add azure https://HOLMES-APPS@dev.azure.com/HOLMES-APPS/WINGS-POC/_git/OMS

# Push all branches
git push azure --all

# Push tags
git push azure --tags
```

### Repository Structure
```
OMS/
├── .azure/
│   ├── pipelines/
│   │   ├── ci-pipeline.yml
│   │   ├── cd-pipeline.yml
│   │   └── infrastructure-pipeline.yml
│   └── k8s/
│       ├── namespace.yml
│       ├── configmap.yml
│       ├── secrets.yml
│       └── *-deployment.yml
├── azure-pipelines.yml          # Main pipeline
├── backend/                     # 8 microservices
├── frontend/                    # React Native Web
├── docs/                        # Documentation
└── docker-compose.yml
```

---

## 3. Branch Policies

### Configure for `main` Branch

1. Go to **Repos** > **Branches**
2. Click `...` on `main` branch > **Branch policies**
3. Enable:

| Policy | Configuration |
|--------|--------------|
| Require minimum reviewers | 2 reviewers |
| Check for linked work items | Required |
| Check for comment resolution | Required |
| Build validation | Add `OMS-CI-Pipeline` |
| Automatically include reviewers | Add tech lead |

### Configure for `develop` Branch

| Policy | Configuration |
|--------|--------------|
| Require minimum reviewers | 1 reviewer |
| Build validation | Add `OMS-CI-Pipeline` |

---

## 4. Azure Pipelines Setup

### Step 1: Create CI Pipeline

1. Go to **Pipelines** > **Pipelines** > **New Pipeline**
2. Select **Azure Repos Git**
3. Select your repository
4. Choose **Existing Azure Pipelines YAML file**
5. Path: `/azure-pipelines.yml`
6. Click **Run**

### Step 2: Create Additional Pipelines

Create separate pipelines for:

| Pipeline Name | YAML File |
|--------------|-----------|
| OMS-CI-Pipeline | `.azure/pipelines/ci-pipeline.yml` |
| OMS-CD-Pipeline | `.azure/pipelines/cd-pipeline.yml` |
| OMS-Infrastructure | `.azure/pipelines/infrastructure-pipeline.yml` |

### Pipeline Variables

Set these in Pipeline settings > Variables:

```yaml
# Azure Resources
azureSubscription: WINGS-POC-ServiceConnection
azureContainerRegistry: wingspocacr.azurecr.io
resourceGroup: WINGS-POC-RG

# SonarQube (if configured)
sonarQubeServiceConnection: SonarQube-Connection
```

---

## 5. Service Connections

### Create Azure Resource Manager Connection

1. Go to **Project Settings** > **Service connections**
2. Click **New service connection**
3. Select **Azure Resource Manager**
4. Choose **Service principal (automatic)**
5. Configure:

| Field | Value |
|-------|-------|
| Subscription | Your Azure Subscription |
| Resource Group | WINGS-POC-RG |
| Service connection name | WINGS-POC-ServiceConnection |
| Grant access to all pipelines | Yes |

### Create Docker Registry Connection

1. Click **New service connection**
2. Select **Docker Registry**
3. Configure:

| Field | Value |
|-------|-------|
| Registry type | Azure Container Registry |
| Azure subscription | Your Subscription |
| Azure container registry | wingspocacr |
| Service connection name | ACR-Connection |

### Create SonarQube Connection (Optional)

1. Click **New service connection**
2. Select **SonarQube**
3. Configure with your SonarQube server URL and token

---

## 6. Environments & Approvals

### Create Environments

1. Go to **Pipelines** > **Environments**
2. Create these environments:

| Environment | Approvals | Checks |
|-------------|-----------|--------|
| OMS-Development | None | Business hours only (optional) |
| OMS-QA | 1 approver | All tests must pass |
| OMS-Production | 2 approvers | Change management approval |

### Configure Production Approvals

1. Click on **OMS-Production** environment
2. Click **...** > **Approvals and checks**
3. Add **Approvals**:
   - Required approvers: 2
   - Allow approvers to approve their own runs: No
   - Timeout: 72 hours

4. Add **Business Hours** check (optional):
   - Time zone: Your timezone
   - Days: Monday-Friday
   - Hours: 6 AM - 6 PM

---

## 7. Azure Boards Integration

### Import User Stories from Backlog

Create work items from `docs/02-backlog.md`:

#### Epics
1. User Authentication & Authorization
2. Product Catalog Management
3. Order Management
4. Payment Processing
5. Inventory Management
6. Shipping & Delivery
7. Notifications

#### Sample User Story Structure

```
Epic: Order Management
├── Feature: Create Order
│   ├── User Story: As a customer, I can add items to cart
│   │   ├── Task: Create cart API endpoint
│   │   ├── Task: Implement cart UI component
│   │   └── Task: Add unit tests
│   └── User Story: As a customer, I can checkout
│       ├── Task: Create checkout flow
│       └── Task: Integrate payment service
```

### Link Commits to Work Items

Use work item ID in commit messages:
```bash
git commit -m "feat(order): add cart functionality #123"
```

Or use Azure DevOps linking:
```bash
git commit -m "feat(order): add cart functionality

AB#123
```

---

## 8. Variable Groups & Secrets

### Create Variable Groups

1. Go to **Pipelines** > **Library**
2. Click **+ Variable group**

#### OMS-Common-Variables
| Variable | Value |
|----------|-------|
| JAVA_VERSION | 17 |
| NODE_VERSION | 18 |
| azureContainerRegistry | wingspocacr.azurecr.io |

#### OMS-Dev-Secrets (Link to Azure Key Vault)
| Secret | Key Vault Secret Name |
|--------|----------------------|
| JWT_SECRET | oms-jwt-secret-dev |
| DB_PASSWORD | oms-db-password-dev |

#### OMS-Prod-Secrets
| Secret | Key Vault Secret Name |
|--------|----------------------|
| JWT_SECRET | oms-jwt-secret-prod |
| DB_PASSWORD | oms-db-password-prod |

### Link to Azure Key Vault

1. In Variable Group, toggle **Link secrets from an Azure key vault**
2. Select subscription and key vault
3. Add secrets to include

---

## 9. Dashboard Setup

### Create Project Dashboard

1. Go to **Overview** > **Dashboards**
2. Click **New Dashboard**
3. Name: "OMS Pipeline Dashboard"

### Recommended Widgets

| Widget | Configuration |
|--------|--------------|
| Build History | OMS-Main-Pipeline, last 10 builds |
| Code Coverage | Backend coverage chart |
| Test Results Trend | Unit test pass/fail trend |
| Work Item Query | Active bugs, Priority 1-2 |
| Pull Request | Open PRs for review |
| Deployment Status | All environments |

---

## Quick Start Commands

### Clone and Setup
```bash
# Clone from Azure DevOps
git clone https://HOLMES-APPS@dev.azure.com/HOLMES-APPS/WINGS-POC/_git/OMS

# Set upstream
cd OMS
git remote add github https://github.com/your-org/SDLC_Demo.git
```

### Trigger Pipeline Manually
```bash
# Using Azure CLI
az pipelines run --name "OMS-Main-Pipeline" --organization https://dev.azure.com/HOLMES-APPS --project WINGS-POC
```

### Check Pipeline Status
```bash
az pipelines runs list --organization https://dev.azure.com/HOLMES-APPS --project WINGS-POC --top 5
```

---

## SDLC Flow with Azure DevOps

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         AZURE DEVOPS SDLC FLOW                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐  │
│  │   BOARDS    │───▶│    REPOS    │───▶│  PIPELINES  │───▶│   DEPLOY    │  │
│  │             │    │             │    │             │    │             │  │
│  │ • Epics     │    │ • Branches  │    │ • CI Build  │    │ • Dev       │  │
│  │ • Features  │    │ • PRs       │    │ • Tests     │    │ • QA        │  │
│  │ • Stories   │    │ • Reviews   │    │ • Quality   │    │ • Prod      │  │
│  │ • Tasks     │    │ • Policies  │    │ • Security  │    │             │  │
│  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘  │
│        │                  │                  │                  │          │
│        │                  │                  │                  │          │
│        ▼                  ▼                  ▼                  ▼          │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         FEEDBACK LOOP                                │   │
│  │  Test Results ◀── Code Coverage ◀── Quality Gate ◀── Monitoring    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Checklist for Demo

- [ ] Azure DevOps project accessible
- [ ] Repository imported with all branches
- [ ] Branch policies configured
- [ ] CI pipeline running successfully
- [ ] CD pipeline with environment approvals
- [ ] Service connections configured
- [ ] Variable groups with secrets
- [ ] Azure Boards with work items
- [ ] Dashboard showing pipeline status

---

## Troubleshooting

### Pipeline Fails with Maven Error
```yaml
# Ensure Java is installed
- task: JavaToolInstaller@0
  inputs:
    versionSpec: '17'
    jdkArchitectureOption: 'x64'
    jdkSourceOption: 'PreInstalled'
```

### Docker Push Fails
- Verify service connection has push permissions
- Check ACR admin credentials enabled

### Test Results Not Publishing
- Ensure test report paths match:
  ```yaml
  testResultsFiles: '**/surefire-reports/TEST-*.xml'
  ```

---

## Next Steps

1. **Azure Infrastructure**: Deploy ACR, AKS/Container Apps, PostgreSQL
2. **Secrets Management**: Set up Azure Key Vault integration
3. **Monitoring**: Configure Application Insights
4. **Security**: Enable Microsoft Defender for Cloud
