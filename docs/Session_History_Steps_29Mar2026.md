# OMS SDLC Demo - Complete Session History & Steps Executed
**Date:** March 26-29, 2026  
**Project:** SDLC_Demo (Order Management System)  
**VM:** AMS-AIOPS-UBU01 (Ubuntu)  
**Retrieved:** March 30, 2026

---

## Table of Contents
1. [Session 1: Deployment Docs Comparison & VM Deployment (Mar 27-29)](#session-1-deployment-documentation-comparison--vm-deployment)
2. [Session 2: Azure DevOps Self-Hosted Agent Setup (Mar 29)](#session-2-azure-devops-self-hosted-agent-setup)
3. [Session 3: Pay-I Integration with Factory.ai (Mar 27)](#session-3-pay-i-integration-with-factoryai)
4. [Session 4: Pay-i Integration Help (Mar 26)](#session-4-pay-i-integration-help)
5. [Session 5: Meeting Transcript Analysis (Mar 26)](#session-5-meeting-transcript-analysis)
6. [Session 6: OMS Deployment Planning (Mar 26)](#session-6-oms-deployment-planning)

---

## Session 1: Deployment Documentation Comparison & VM Deployment

### Overview
Compared `OMS_SDLC_Deployment_Plan.md` with `11.docx` and executed deployment steps on Azure VM.

### Steps Executed

#### 1. Repository Setup on VM
```bash
# Cloned repository from Azure DevOps
sudo git clone https://HOLMES-APPS@dev.azure.com/HOLMES-APPS/WINGS-POC/_git/SDLC-POC --branch develop
cd /data/CUE-POC/SDLC-POC
```

#### 2. Frontend Build Issues & Fixes
- Encountered npm/expo issues with package.json
- Fixed frontend build errors locally in `C:\trainings\factoryAI\SDLC_Demo\frontend`
- Ran `npm install` and `npm run build:web`
- Pushed fixes to Git and Azure DevOps

#### 3. Frontend Deployment to Nginx
```bash
# Pulled latest code on VM
cd /data/CUE-POC/SDLC-POC/frontend
git pull origin develop

# Build frontend
npm install
npm run build:web

# Deploy to nginx web root
sudo rm -rf /var/www/html/cue-sdlc-demo/*
sudo cp -r dist/* /var/www/html/cue-sdlc-demo/
```

#### 4. Nginx Configuration
- Configured `/etc/nginx/sites-enabled/default` for the OMS app
- Added location blocks for API proxy:
```nginx
location /cue-sdlc-demo/api/ {
    proxy_pass http://127.0.0.1:8090/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

```bash
# Restart nginx
sudo systemctl restart nginx
```

#### 5. Backend Microservices Deployment (Docker Compose)
```bash
cd /data/CUE-POC/SDLC-POC

# Start all services
sudo docker-compose -f docker-compose.vm.yml up -d

# Check running containers
sudo docker ps

# Check service logs
sudo docker logs oms-api-gateway --tail 100
sudo docker logs oms-auth-service --tail 100
```

**Services Deployed:**
- auth-db, catalog-db, order-db, inventory-db, payment-db, shipping-db
- auth-service, catalog-service, inventory-service, order-service
- payment-service, shipping-service, notification-service
- api-gateway (port 8090)

#### 6. API Testing
```bash
# Test login endpoint
curl -X POST http://localhost:8090/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@oms.com","password":"admin123"}'

# Test registration
curl -X POST http://localhost:8090/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"name":"Test User","email":"test@test.com","password":"password123"}'
```

#### 7. CORS Issue Resolution
- Encountered 403 errors from browser
- Investigated CORS configuration in api-gateway
- **Resolution:** Removed CORS config as not needed for same-origin requests (app served from same domain)
- Git commits made:
  - `fix: add CORS allowed origins for production domain`
  - `revert: remove CORS config - not needed for same-origin requests`

#### 8. Docker Container Issues Fixed
- Encountered `ContainerConfig` error when recreating api-gateway
- Fixed by removing orphan containers:
```bash
sudo docker-compose -f docker-compose.vm.yml down --remove-orphans
sudo docker-compose -f docker-compose.vm.yml up -d
```

#### 9. Application URL
- **Production URL:** https://wingsdemo.waip.wiprocms.com/cue-sdlc-demo/
- API Gateway exposed on port 8090

---

## Session 2: Azure DevOps Self-Hosted Agent Setup

### Overview
Discussed setting up Azure DevOps self-hosted agent pool due to inability to create service connection with Azure Portal (company policy restrictions).

### Planned Steps (For Future Implementation)

#### 1. Create Agent Pool in Azure DevOps
1. Go to **Organization Settings** → **Agent pools** → **Add pool**
2. Choose "Self-hosted" and name it (e.g., `UbuntuVM-Pool`)

#### 2. Install Agent on Ubuntu VM
```bash
# Create directory for agent
mkdir ~/azagent && cd ~/azagent

# Download agent
curl -O https://vstsagentpackage.azureedge.net/agent/3.248.0/vsts-agent-linux-x64-3.248.0.tar.gz

# Extract
tar zxvf vsts-agent-linux-x64-3.248.0.tar.gz

# Configure (requires PAT token)
./config.sh
```

#### 3. Run Agent as Service
```bash
sudo ./svc.sh install
sudo ./svc.sh start
```

#### 4. Update Pipeline YAML
```yaml
pool:
  name: 'UbuntuVM-Pool'

steps:
  - script: |
      cd /path/to/your/app
      docker-compose down
      docker-compose pull
      docker-compose up -d
    displayName: 'Deploy with Docker Compose'
```

### Lineaje Integration Discussion
- Discussed integrating Lineaje for vulnerability scanning
- Options: ADO Pipeline integration, Factory.ai custom droid, MCP integration
- Workflow: Developer Code → Factory.ai → Git Push → ADO Pipeline → Lineaje Scan → Deploy

---

## Key Git Commits During Sessions
```
0f2deeb revert: remove CORS config - not needed for same-origin requests
7b5d941 fix: add CORS allowed origins for production domain
7dc3094 fix: use /poc/cue-sdlc-demo/api - IIS requires /poc/ prefix in browser requests
0f68035 fix: revert to /cue-sdlc-demo/api - IIS strips /poc/ prefix
26ce115 fix: use /poc/cue-sdlc-demo/api - IIS keeps /poc/ prefix
```

---

## Issues Encountered & Resolutions

| Issue | Resolution |
|-------|------------|
| Git clone asking for password | Generate PAT token from Azure DevOps User Settings |
| npm/expo build errors | Fixed package.json dependencies locally and pushed |
| 404 errors on frontend | Corrected nginx configuration and rebuilt frontend |
| 403 CORS errors | Removed unnecessary CORS config from api-gateway |
| Docker ContainerConfig error | Used `docker-compose down --remove-orphans` |
| API validation errors | Fixed request payload (used `name` instead of `fullName`) |
| Services not starting | Verified all containers running with `docker ps` |

---

## Important Paths

| Component | Path |
|-----------|------|
| Project on VM | `/data/CUE-POC/SDLC-POC` |
| Frontend dist | `/data/CUE-POC/SDLC-POC/frontend/dist` |
| Nginx web root | `/var/www/html/cue-sdlc-demo` |
| Nginx config | `/etc/nginx/sites-enabled/default` |
| Docker compose | `docker-compose.vm.yml` |

---

---

## Session 3: Pay-I Integration with Factory.ai

**Date:** March 27, 2026  
**Session Title:** Pay-I Integration with Factory.ai Guide

### Overview
Explored how to integrate Pay-I (GenAI FinOps platform) with Factory.ai for AI cost tracking and observability.

### Key Topics Discussed

#### What is Pay-I?
- **Pay-I** is an enterprise GenAI FinOps and observability platform
- Tracks AI costs, token usage, and ROI across multiple LLM providers
- Available at: https://pay-i.com/

#### Pay-I Key Features
1. **Real-time FinOps** - Budget control, unit economics, spend forecasting
2. **GenAI Observability** - Track performance, latency, throughput
3. **ROI Alignment** - Connect AI spend to business KPIs
4. **Multi-provider Support** - OpenAI, Anthropic, Azure OpenAI, AWS Bedrock

#### Factory.ai + Pay-I Integration Approach

**Option 1: OTEL Telemetry Export**
Factory.ai supports OpenTelemetry export for metrics:
```bash
export OTEL_TELEMETRY_ENDPOINT="https://your-collector.example.com:4318"
export OTEL_TELEMETRY_HEADERS="Authorization=Bearer <token>"
```

**Option 2: Pay-I Python SDK**
```python
from payi import Payi
from payi.lib.instrument import payi_instrument

# Initialize instrumentation
payi_instrument()

# Track AI usage
client = Payi()
response = client.ingest.units(
    category="system.openai",
    resource="gpt-4o-mini",
    units={"text": {"input": 156, "output": 1746}}
)
```

#### Integration Use Cases
1. Track Factory.ai Droid token consumption
2. Monitor BYOK API key usage across models
3. Implement spending limits and alerts
4. Correlate AI costs with productivity metrics

---

## Session 4: Pay-i Integration Help

**Date:** March 26, 2026  
**Session Title:** Pay-i Integration Help

### Overview
Quick follow-up session clarifying Pay-i integration scope for full-stack implementation.

### Key Points
- Integration scope: Full-stack (backend + frontend)
- Use case: AI cost tracking/billing service for usage monitoring
- Backend: Java Spring Boot microservices
- Frontend: React Native/Expo

---

## Session 5: Meeting Transcript Analysis

**Date:** March 26, 2026  
**Session Title:** Pay-I Platform Training and Integration Demo

### Overview
Analysis of a training session transcript about Pay-I platform.

### Key Topics from Meeting
1. **Platform Overview** - Pay-I tracks spend, value generation, and KPIs across AI initiatives
2. **Deployment Model** - Deployed into client environments (AWS/Azure) with a single line of code integration
3. **Cost Tracking** - Real-time visibility into GenAI costs
4. **Value Alignment** - Connecting AI investments to business outcomes
5. **WINGS Platform Integration** - How Pay-I fits into the broader WINGS platform architecture

### Training Goals Discussed
1. Enable team to demo and articulate Pay-I value proposition
2. Explain alignment between Pay-I and WINGS platform
3. Build joint capabilities for the integrated platform

---

## Session 6: OMS Deployment Planning

**Date:** March 26, 2026  
**Session Title:** Deploy Order Management Services to Azure Ubuntu VM

### Overview
Initial deployment planning session for the OMS application to Azure Ubuntu VM.

### Topics Explored

#### 1. Understanding Existing VM Deployment Strategy
- Investigated how existing 20+ apps are deployed on the VM
- Found mix of: Static files in `/var/www/html/`, PM2 for Node.js apps, Docker containers

#### 2. Docker vs Docker Compose
- **Decision:** Use Docker Compose to align with existing deployment patterns
- Docker Compose manages multiple containers as a single application
- Each docker-compose.yml is independent and won't affect other apps

#### 3. OMS Container Architecture
15 containers for OMS app:
- 6 databases (auth-db, catalog-db, order-db, inventory-db, payment-db, shipping-db)
- 8 services (auth, catalog, inventory, order, payment, shipping, notification, api-gateway)
- 1 frontend (optional, or use nginx)

#### 4. Nginx Role
- Reverse proxy for all applications
- Routes traffic based on URL paths
- Frontend: Serve static files from `/var/www/html/`
- Backend API: Proxy to api-gateway on port 8090

#### 5. Frontend Deployment Strategy
**Option A (Chosen):** Copy built files to `/var/www/html/`
- Matches existing deployment pattern
- Nginx serves static files directly
- Build command: `npm run build:web`
- Output: `dist/` folder

#### 6. Deployment Plan Created
1. Clone repository to VM
2. Build frontend (`npm run build:web`)
3. Copy `dist/` to `/var/www/html/cue-sdlc-demo/`
4. Configure nginx location blocks
5. Run `docker-compose up -d` for backend services
6. Test endpoints

### Questions Answered
- PM2 is for Node.js apps, not Java Spring Boot
- Docker Compose doesn't require licensing (Apache 2.0)
- Each docker-compose.yml runs independently

---

## Summary: All Sessions Overview

| Session | Date | Topic | Key Outcome |
|---------|------|-------|-------------|
| 1 | Mar 27-29 | VM Deployment Execution | OMS deployed successfully |
| 2 | Mar 29 | Azure DevOps Agent | Self-hosted agent setup planned |
| 3 | Mar 27 | Pay-I + Factory.ai | Integration approaches documented |
| 4 | Mar 26 | Pay-i Scope | Full-stack integration confirmed |
| 5 | Mar 26 | Meeting Analysis | Pay-I training summary |
| 6 | Mar 26 | Deployment Planning | Architecture and steps defined |

---

## Technologies Used

| Component | Technology |
|-----------|------------|
| Backend | Java, Spring Boot, Spring Cloud Gateway |
| Frontend | React Native, Expo, TypeScript |
| Database | PostgreSQL (per microservice) |
| Containerization | Docker, Docker Compose |
| Web Server | Nginx (reverse proxy) |
| CI/CD | Azure DevOps Pipelines |
| Version Control | Git, Azure Repos |
| AI Coding | Factory.ai Droids |
| Cost Tracking | Pay-I (planned) |

---

*Document generated from Factory.ai Droid session history on March 30, 2026*
