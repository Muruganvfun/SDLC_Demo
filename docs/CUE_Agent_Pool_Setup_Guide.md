# CUE Program - Azure DevOps Self-Hosted Agent Pool Setup Guide

## Overview

This guide provides step-by-step instructions to create and configure a self-hosted agent pool for CUE Program pipeline integration with Azure DevOps.

---

## Prerequisites

| Requirement | Details |
|-------------|---------|
| Azure DevOps Organization | `HOLMES-APPS` |
| Azure DevOps Project | `WINGS-POC` |
| Repository | `SDLC-POC` |
| VM | Ubuntu Linux (Azure Portal) |
| VM User | SSH access with sudo privileges |
| Required Tools on VM | Docker, Docker Compose |

---

## Architecture

```
Organization (HOLMES-APPS)
│
├── Organization Agent Pools  ← Agents register HERE (Step 1)
│   └── "CUE-VM-POOL"
│
└── Project: WINGS-POC
    └── Project Agent Pools   ← Pipelines reference from here (auto-linked)
        └── "CUE-VM-POOL"
    └── Pipeline: SDLC-POC    ← Uses CUE-VM-POOL (Step 6)
```

---

## Step 1: Create Agent Pool at Organization Level

> **IMPORTANT**: Agent pools must be created at **Organization level**, not Project level. Agents register at the Organization level, and Projects reference them.

1. Navigate to: **https://dev.azure.com/HOLMES-APPS/_settings/agentpools**
   
   *(Note: No project name in URL - this is Organization Settings)*

2. Click **"Add pool"**

3. Configure the pool:
   | Setting | Value |
   |---------|-------|
   | Pool type | `Self-hosted` |
   | Name | `CUE-VM-POOL` |
   | Grant access permission to all pipelines | ✅ Check (or configure per-project) |
   | Auto-provision in all projects | Optional |

4. Click **Create**

---

## Step 2: Generate Personal Access Token (PAT)

1. Navigate to: **https://dev.azure.com/HOLMES-APPS/_usersSettings/tokens**

2. Click **"+ New Token"**

3. Configure the token:
   | Setting | Value |
   |---------|-------|
   | Name | `CUE-Agent-Token` |
   | Expiration | 90 days (or as per security policy) |
   | Scopes | Custom defined |

4. **Required Permissions**:
   - ✅ **Agent Pools**: `Read & manage`
   - ✅ **Deployment Groups**: `Read & manage` (optional)

5. Click **Create** and **copy the token immediately** (you won't see it again!)

---

## Step 3: Download and Configure Agent on Ubuntu VM

### 3.1 SSH into the VM

```bash
ssh <username>@<vm-public-ip>
```

### 3.2 Create Agent Directory

```bash
mkdir ~/azagent && cd ~/azagent
```

### 3.3 Download the Agent

Get the latest download URL from:
- Azure DevOps: **Organization Settings → Agent Pools → CUE-VM-POOL → New Agent → Linux**
- Or GitHub: **https://github.com/microsoft/azure-pipelines-agent/releases**

```bash
# Download latest agent (verify version from official source)
curl -O https://vstsagentpackage.azureedge.net/agent/4.265.0/vsts-agent-linux-x64-4.265.0.tar.gz

# Verify SHA256 hash (get expected hash from download page)
sha256sum vsts-agent-linux-x64-4.265.0.tar.gz

# Extract
tar zxvf vsts-agent-linux-x64-4.265.0.tar.gz
```

### 3.4 Install Dependencies

```bash
sudo ./bin/installdependencies.sh
```

### 3.5 Configure the Agent

```bash
./config.sh
```

**Configuration Prompts:**

| Prompt | Value |
|--------|-------|
| Server URL | `https://dev.azure.com/HOLMES-APPS` |
| Authentication type | `PAT` (press Enter) |
| Personal access token | (paste your token from Step 2) |
| Agent pool | `CUE-VM-POOL` |
| Agent name | `CUE-Ubuntu-Agent` (or your preference) |
| Work folder | `_work` (press Enter for default) |

---

## Step 4: Install Agent as Systemd Service

```bash
# Navigate to agent directory
cd ~/azagent

# Install as service
sudo ./svc.sh install

# Start the service
sudo ./svc.sh start

# Verify status
sudo ./svc.sh status
```

### Service Management Commands

| Action | Command |
|--------|---------|
| Start | `sudo ./svc.sh start` |
| Stop | `sudo ./svc.sh stop` |
| Status | `sudo ./svc.sh status` |
| Uninstall | `sudo ./svc.sh uninstall` |

---

## Step 5: Verify VM Prerequisites

Since the pipeline uses Docker containers for builds, verify these tools are installed:

```bash
# Check Docker
docker --version

# Check Docker Compose
docker compose version

# Verify agent user has Docker access
docker ps
```

### If Docker Access Denied

```bash
# Add agent user to docker group
sudo usermod -aG docker $USER

# Apply changes (or logout/login)
newgrp docker
```

### Tools NOT Required on VM (handled by Docker containers)

- ❌ Java/JDK
- ❌ Maven
- ❌ Node.js (unless needed outside containers)

---

## Step 6: Update Pipeline YAML

Update your pipeline to use the self-hosted agent pool:

### File: `azure-pipelines.yml`

**Replace:**
```yaml
pool:
  vmImage: 'ubuntu-latest'
```

**With:**
```yaml
pool:
  name: 'CUE-VM-POOL'
```

### For Multiple Jobs

```yaml
stages:
- stage: Build
  jobs:
  - job: BackendBuild
    pool:
      name: 'CUE-VM-POOL'
    steps:
      # ... your steps

  - job: FrontendBuild
    pool:
      name: 'CUE-VM-POOL'
    steps:
      # ... your steps
```

### Commit and Push Changes

```bash
git add azure-pipelines.yml
git commit -m "feat: use CUE-VM-POOL self-hosted agent"
git push origin develop
```

---

## Step 7: Verify Agent Connection

1. Navigate to: **https://dev.azure.com/HOLMES-APPS/_settings/agentpools**

2. Click on **CUE-VM-POOL**

3. Go to **Agents** tab

4. Verify agent shows as **Online** (green status)

---

## Troubleshooting

### Error: VS30063 - Not Authorized

**Cause**: PAT token doesn't have required permissions or pool doesn't exist at Organization level.

**Solution**:
1. Verify pool exists at Organization level (not just Project level)
2. Create new PAT with `Agent Pools: Read & manage` scope
3. Re-run `./config.sh`

### Error: Pool Not Found

**Cause**: Pool created at Project level instead of Organization level.

**Solution**: Create pool at **https://dev.azure.com/HOLMES-APPS/_settings/agentpools** (no project name in URL)

### Agent Shows Offline

**Cause**: Service not running or network issues.

**Solution**:
```bash
cd ~/azagent
sudo ./svc.sh status
sudo ./svc.sh start
```

### Docker Permission Denied

**Cause**: Agent user not in docker group.

**Solution**:
```bash
sudo usermod -aG docker $USER
# Restart agent service
sudo ./svc.sh stop
sudo ./svc.sh start
```

---

## Security Best Practices

1. **PAT Token Expiration**: Set appropriate expiration (90 days max recommended)
2. **Minimal Scopes**: Only grant `Agent Pools: Read & manage`
3. **Pool Permissions**: Restrict pool access to specific projects if needed
4. **Agent User**: Run agent as non-root user with minimal privileges
5. **Network**: Ensure VM has outbound access to `dev.azure.com` and `vstsagentpackage.azureedge.net`

---

## Quick Reference

| Resource | URL |
|----------|-----|
| Organization Agent Pools | https://dev.azure.com/HOLMES-APPS/_settings/agentpools |
| PAT Tokens | https://dev.azure.com/HOLMES-APPS/_usersSettings/tokens |
| Project Settings | https://dev.azure.com/HOLMES-APPS/WINGS-POC/_settings/ |
| Agent Releases | https://github.com/microsoft/azure-pipelines-agent/releases |
| Official Docs | https://learn.microsoft.com/en-us/azure/devops/pipelines/agents/linux-agent |

---

## Summary Checklist

- [ ] Step 1: Create `CUE-VM-POOL` at **Organization level**
- [ ] Step 2: Generate PAT with `Agent Pools: Read & manage` scope
- [ ] Step 3: Download and configure agent on Ubuntu VM
- [ ] Step 4: Install agent as systemd service
- [ ] Step 5: Verify Docker and Docker Compose installed
- [ ] Step 6: Update `azure-pipelines.yml` to use `CUE-VM-POOL`
- [ ] Step 7: Verify agent shows Online in Azure DevOps

---

*Document Version: 1.0*  
*Last Updated: March 2026*  
*Project: CUE Program - SDLC Pipeline Integration*
