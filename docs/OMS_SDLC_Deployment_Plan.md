# OMS SDLC Application - Deployment Plan

## Document Information

| Field | Value |
|-------|-------|
| **Document Title** | OMS SDLC Deployment Plan |
| **Version** | 1.0 |
| **Date** | March 26, 2026 |
| **Target Environment** | Azure Ubuntu VM (AMS-AIOPS-UBU01) |
| **Author** | Factory.ai Droid |

---

## 1. Executive Summary

This document outlines the deployment plan for the Order Management System (OMS) SDLC Demo application to the Azure Ubuntu VM. The application showcases SDLC automation using Factory.ai and consists of Spring Boot microservices backend with a React Native frontend.

---

## 2. Pre-Deployment Verification Results

### 2.1 Port Availability Check

| Port | Status | Used By |
|------|--------|---------|
| 8080 | **IN USE** | docker-proxy (react-app) |
| 8081 | FREE | - |
| 8082 | FREE | - |
| 8083 | FREE | - |
| 8084 | FREE | - |
| 8085 | **IN USE** | python (Noogler) |
| 8086 | FREE | - |
| 8087 | FREE | - |
| 8090 | FREE | - |
| 5432 | FREE | - |

**Decision**: Use port **8090** for OMS API Gateway to avoid conflict with existing apps.

### 2.2 System Verification

| Component | Version/Status | Requirement Met |
|-----------|----------------|-----------------|
| Docker | 28.2.2 | ✅ Yes |
| Docker Compose | 1.29.2 | ✅ Yes |
| /data disk space | 471GB free (9% used) | ✅ Yes |
| /var/www/html disk | 96GB free (23% used) | ✅ Yes |

---

## 3. Application Architecture

### 3.1 Components Overview

| Layer | Components | Count |
|-------|------------|-------|
| **Frontend** | React Native Web (Static) | 1 |
| **API Gateway** | Spring Cloud Gateway | 1 |
| **Microservices** | Spring Boot Services | 7 |
| **Databases** | PostgreSQL 15 | 6 |
| **Total Containers** | | **14** |

### 3.2 Microservices Details

| Service | Internal Port | Function |
|---------|---------------|----------|
| api-gateway | 8080 → 8090 (external) | Routes all API requests, JWT validation |
| auth-service | 8081 | User authentication, token management |
| catalog-service | 8082 | Product catalog management |
| order-service | 8083 | Order processing |
| inventory-service | 8084 | Stock management |
| payment-service | 8085 | Payment processing |
| shipping-service | 8086 | Shipment tracking |
| notification-service | 8087 | Email/SMS notifications |

### 3.3 Database Details

| Database | Service | Container Name |
|----------|---------|----------------|
| authdb | auth-service | oms-auth-db |
| catalogdb | catalog-service | oms-catalog-db |
| orderdb | order-service | oms-order-db |
| inventorydb | inventory-service | oms-inventory-db |
| paymentdb | payment-service | oms-payment-db |
| shippingdb | shipping-service | oms-shipping-db |

---

## 4. Deployment Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              INTERNET                                        │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    wingsdemo.waip.wiprocms.com                               │
│                         Azure Ubuntu VM                                      │
│                       (AMS-AIOPS-UBU01)                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                         NGINX (Port 80)                                 │ │
│  │                       Reverse Proxy Server                              │ │
│  ├────────────────────────────────────────────────────────────────────────┤ │
│  │  Route: /oms-sdlc/       → /var/www/html/oms-sdlc/ (Static Files)      │ │
│  │  Route: /oms-sdlc/api/*  → http://127.0.0.1:8090/api/* (API Gateway)   │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                          │                           │                       │
│            ┌─────────────┘                           └─────────────┐         │
│            │                                                       │         │
│            ▼                                                       ▼         │
│  ┌──────────────────────┐                    ┌─────────────────────────────┐│
│  │      FRONTEND        │                    │      DOCKER COMPOSE         ││
│  │   (Static Files)     │                    │    (14 Containers)          ││
│  │                      │                    │                             ││
│  │ /var/www/html/       │                    │ ┌─────────────────────────┐ ││
│  │   oms-sdlc/          │                    │ │    API GATEWAY          │ ││
│  │   ├── index.html     │                    │ │  (oms-api-gateway)      │ ││
│  │   ├── assets/        │                    │ │    Port: 8090           │ ││
│  │   └── ...            │                    │ └───────────┬─────────────┘ ││
│  └──────────────────────┘                    │             │               ││
│                                              │             ▼               ││
│                                              │ ┌─────────────────────────┐ ││
│                                              │ │    MICROSERVICES        │ ││
│                                              │ │  (Internal Network)     │ ││
│                                              │ │                         │ ││
│                                              │ │ ┌─────────┐ ┌─────────┐ │ ││
│                                              │ │ │  Auth   │ │ Catalog │ │ ││
│                                              │ │ │  :8081  │ │  :8082  │ │ ││
│                                              │ │ └────┬────┘ └────┬────┘ │ ││
│                                              │ │ ┌─────────┐ ┌─────────┐ │ ││
│                                              │ │ │  Order  │ │Inventory│ │ ││
│                                              │ │ │  :8083  │ │  :8084  │ │ ││
│                                              │ │ └────┬────┘ └────┬────┘ │ ││
│                                              │ │ ┌─────────┐ ┌─────────┐ │ ││
│                                              │ │ │ Payment │ │Shipping │ │ ││
│                                              │ │ │  :8085  │ │  :8086  │ │ ││
│                                              │ │ └────┬────┘ └────┬────┘ │ ││
│                                              │ │ ┌─────────────────────┐ │ ││
│                                              │ │ │   Notification      │ │ ││
│                                              │ │ │      :8087          │ │ ││
│                                              │ │ └─────────────────────┘ │ ││
│                                              │ └───────────┬─────────────┘ ││
│                                              │             │               ││
│                                              │             ▼               ││
│                                              │ ┌─────────────────────────┐ ││
│                                              │ │   POSTGRESQL DATABASES  │ ││
│                                              │ │    (6 Containers)       │ ││
│                                              │ │                         │ ││
│                                              │ │ ┌───────┐ ┌───────┐     │ ││
│                                              │ │ │authdb │ │catalogdb    │ ││
│                                              │ │ └───────┘ └───────┘     │ ││
│                                              │ │ ┌───────┐ ┌───────┐     │ ││
│                                              │ │ │orderdb│ │inventorydb  │ ││
│                                              │ │ └───────┘ └───────┘     │ ││
│                                              │ │ ┌───────┐ ┌───────┐     │ ││
│                                              │ │ │paymentdb│shippingdb  │ ││
│                                              │ │ └───────┘ └───────┘     │ ││
│                                              │ └─────────────────────────┘ ││
│                                              └─────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Deployment Steps

### Step 1: Create Project Directory

```bash
# SSH to VM
ssh aiopslinuxadmin@AMS-AIOPS-UBU01

# Create directory
sudo mkdir -p /data/oms-sdlc
sudo chown aiopslinuxadmin:aiopslinuxadmin /data/oms-sdlc
```

**Expected Result**: Directory `/data/oms-sdlc` created with correct ownership.

---

### Step 2: Copy Project Files to VM

```bash
# From LOCAL Windows machine
scp -r C:\trainings\factoryAI\SDLC_Demo\backend aiopslinuxadmin@AMS-AIOPS-UBU01:/data/oms-sdlc/
scp C:\trainings\factoryAI\SDLC_Demo\docker-compose.vm.yml aiopslinuxadmin@AMS-AIOPS-UBU01:/data/oms-sdlc/
```

**Expected Result**: Backend folder and docker-compose.vm.yml copied to VM.

---

### Step 3: Build Frontend Locally

```bash
# On LOCAL Windows machine
cd C:\trainings\factoryAI\SDLC_Demo\frontend

# Install dependencies
npm install

# Build for web
npm run build:web
```

**Expected Result**: `frontend/dist/` folder created with static files.

---

### Step 4: Deploy Frontend to Nginx

```bash
# Create frontend directory on VM
ssh aiopslinuxadmin@AMS-AIOPS-UBU01 "sudo mkdir -p /var/www/html/oms-sdlc"

# Copy dist files from LOCAL to VM
scp -r C:\trainings\factoryAI\SDLC_Demo\frontend\dist\* aiopslinuxadmin@AMS-AIOPS-UBU01:/tmp/oms-frontend/

# SSH to VM and move files
ssh aiopslinuxadmin@AMS-AIOPS-UBU01

# Move to nginx directory
sudo mv /tmp/oms-frontend/* /var/www/html/oms-sdlc/
sudo chown -R www-data:www-data /var/www/html/oms-sdlc/
sudo chmod -R 755 /var/www/html/oms-sdlc/
```

**Expected Result**: Frontend files deployed to `/var/www/html/oms-sdlc/`.

---

### Step 5: Start Docker Containers

```bash
# On VM
cd /data/oms-sdlc

# Build and start all containers
sudo docker-compose -f docker-compose.vm.yml up -d --build

# Watch startup logs (wait for all services to be healthy)
sudo docker-compose -f docker-compose.vm.yml logs -f
```

**Expected Result**: 14 containers running (6 databases + 8 microservices).

**Note**: First build takes 10-15 minutes as Docker builds Java images.

---

### Step 6: Verify Container Status

```bash
# Check all containers are running
sudo docker-compose -f docker-compose.vm.yml ps

# Expected output:
#        Name                     State          Ports
# oms-api-gateway               Up       0.0.0.0:8090->8080/tcp
# oms-auth-db                   Up       5432/tcp
# oms-auth-service              Up       
# oms-catalog-db                Up       5432/tcp
# oms-catalog-service           Up       
# ... (all 14 containers showing "Up")
```

**Expected Result**: All 14 containers showing "Up" status.

---

### Step 7: Configure Nginx

```bash
# Backup existing config
sudo cp /etc/nginx/sites-enabled/default /etc/nginx/sites-enabled/default.backup.$(date +%Y%m%d)

# Edit nginx config
sudo nano /etc/nginx/sites-enabled/default
```

**Add the following inside the `server { }` block:**

```nginx
# ============================================
# OMS SDLC Demo Application
# Added: [DATE]
# ============================================

# Frontend - Static files
location /oms-sdlc/ {
    root /var/www/html;
    index index.html;
    try_files $uri $uri/ /oms-sdlc/index.html;
}

# API Gateway - Backend proxy
location /oms-sdlc/api/ {
    proxy_pass http://127.0.0.1:8090/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_redirect off;
    proxy_http_version 1.1;
    proxy_set_header Connection '';
    proxy_buffering off;
    proxy_cache off;
    proxy_read_timeout 3600;
    proxy_send_timeout 3600;
}

# Swagger UI - API Documentation (Optional)
location /oms-sdlc/swagger-ui/ {
    proxy_pass http://127.0.0.1:8090/swagger-ui/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

location /oms-sdlc/v3/api-docs {
    proxy_pass http://127.0.0.1:8090/v3/api-docs;
    proxy_set_header Host $host;
}
```

---

### Step 8: Test and Reload Nginx

```bash
# Test nginx configuration
sudo nginx -t

# Expected output:
# nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
# nginx: configuration file /etc/nginx/nginx.conf test is successful

# Reload nginx
sudo systemctl reload nginx
```

**Expected Result**: Nginx reloaded without errors.

---

### Step 9: Verify Deployment

```bash
# Test API Gateway health directly
curl http://localhost:8090/actuator/health

# Test API via nginx
curl http://localhost/oms-sdlc/api/actuator/health

# Expected response:
# {"status":"UP"}
```

**Expected Result**: Health endpoint returns `{"status":"UP"}`.

---

## 6. Post-Deployment Verification

### 6.1 Access URLs

| Component | URL |
|-----------|-----|
| **Frontend** | http://wingsdemo.waip.wiprocms.com/oms-sdlc/ |
| **API Health** | http://wingsdemo.waip.wiprocms.com/oms-sdlc/api/actuator/health |
| **Swagger UI** | http://wingsdemo.waip.wiprocms.com/oms-sdlc/swagger-ui/index.html |
| **API Docs** | http://wingsdemo.waip.wiprocms.com/oms-sdlc/v3/api-docs |

### 6.2 Health Check Commands

```bash
# All containers status
sudo docker-compose -f docker-compose.vm.yml ps

# Container resource usage
sudo docker stats --no-stream

# Check specific service logs
sudo docker-compose -f docker-compose.vm.yml logs oms-api-gateway --tail=100

# Test API endpoints
curl http://localhost:8090/api/auth/health
curl http://localhost:8090/api/products
```

### 6.3 Verification Checklist

| Check | Command | Expected Result |
|-------|---------|-----------------|
| All containers running | `docker-compose ps` | 14 containers "Up" |
| API Gateway accessible | `curl localhost:8090/actuator/health` | `{"status":"UP"}` |
| Frontend loads | Browser: `/oms-sdlc/` | OMS login page |
| Nginx routing works | `curl localhost/oms-sdlc/api/actuator/health` | `{"status":"UP"}` |
| No port conflicts | `sudo ss -tlnp \| grep 8090` | Only oms-api-gateway |

---

## 7. Rollback Plan

### 7.1 Stop OMS Application

```bash
cd /data/oms-sdlc
sudo docker-compose -f docker-compose.vm.yml down
```

### 7.2 Remove with Data (if needed)

```bash
# WARNING: This deletes all database data
sudo docker-compose -f docker-compose.vm.yml down -v
```

### 7.3 Restore Nginx Config

```bash
sudo cp /etc/nginx/sites-enabled/default.backup.YYYYMMDD /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
```

### 7.4 Remove Frontend Files

```bash
sudo rm -rf /var/www/html/oms-sdlc/
```

### 7.5 Remove Project Directory

```bash
sudo rm -rf /data/oms-sdlc/
```

---

## 8. Troubleshooting Guide

| Issue | Possible Cause | Solution |
|-------|----------------|----------|
| Container won't start | Port conflict | Check `sudo ss -tlnp`, change port in docker-compose |
| 502 Bad Gateway | Backend not running | `docker-compose ps`, restart if needed |
| Frontend blank page | Wrong API URL | Check browser console, verify API base URL |
| Database connection error | DB not ready | Wait for health check, check logs |
| Build fails | Missing dependencies | Check Dockerfile, ensure context paths correct |
| Nginx config error | Syntax error | Run `sudo nginx -t` to identify issue |

### Useful Debug Commands

```bash
# View all container logs
sudo docker-compose -f docker-compose.vm.yml logs

# View specific service log
sudo docker-compose -f docker-compose.vm.yml logs oms-auth-service

# Enter container shell
sudo docker exec -it oms-api-gateway /bin/sh

# Check nginx error log
sudo tail -f /var/log/nginx/error.log

# Check network connectivity
sudo docker network ls
sudo docker network inspect oms-sdlc_oms-network
```

---

## 9. File Locations Summary

| Component | Location |
|-----------|----------|
| Project Root | `/data/oms-sdlc/` |
| Docker Compose | `/data/oms-sdlc/docker-compose.vm.yml` |
| Backend Source | `/data/oms-sdlc/backend/` |
| Frontend Static | `/var/www/html/oms-sdlc/` |
| Nginx Config | `/etc/nginx/sites-enabled/default` |
| Nginx Logs | `/var/log/nginx/` |
| Docker Volumes | `/var/lib/docker/volumes/oms-sdlc_*` |

---

## 10. Approval Sign-off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Developer | | | |
| DevOps Lead | | | |
| QA Lead | | | |
| Project Manager | | | |

---

## Appendix A: docker-compose.vm.yml

See file: `/data/oms-sdlc/docker-compose.vm.yml`

## Appendix B: Nginx Configuration Block

See Step 7 in this document.

## Appendix C: Architecture Diagram

See file: `OMS_SDLC_Architecture.drawio`
