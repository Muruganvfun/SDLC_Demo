# Webhook CD Setup for Azure Ubuntu VM

## Overview

This guide sets up automatic deployment triggered by Azure DevOps pipeline.

```
CI Pipeline (Azure DevOps) ──webhook──> VM (pulls & deploys)
```

## Prerequisites

- SSH access to Azure Ubuntu VM
- Git configured with Azure DevOps access
- Docker and Docker Compose installed

---

## Step 1: Install Webhook Tool

SSH into your Azure Ubuntu VM:

```bash
ssh aiopslinuxadmin@4.240.70.81
```

Install webhook:

```bash
sudo apt-get update
sudo apt-get install -y webhook
```

---

## Step 2: Copy Scripts to VM

From your local machine (or clone from repo):

```bash
# On the VM, navigate to SDLC_Demo
cd ~/SDLC_Demo

# Pull latest code (includes webhook scripts)
git pull origin develop

# Make deploy script executable
chmod +x scripts/deploy.sh

# Create webhook directory if needed
mkdir -p scripts/webhook
```

---

## Step 3: Setup Systemd Service

```bash
# Copy service file
sudo cp scripts/webhook/webhook.service /etc/systemd/system/

# Create log file
sudo touch /var/log/webhook.log
sudo touch /var/log/sdlc-deploy.log
sudo chown aiopslinuxadmin:aiopslinuxadmin /var/log/webhook.log
sudo chown aiopslinuxadmin:aiopslinuxadmin /var/log/sdlc-deploy.log

# Reload systemd
sudo systemctl daemon-reload

# Enable and start webhook service
sudo systemctl enable webhook
sudo systemctl start webhook

# Check status
sudo systemctl status webhook
```

---

## Step 4: Open Firewall Port

```bash
# Allow port 9000 for webhook
sudo ufw allow 9000/tcp

# Or if using Azure NSG, add inbound rule:
# Port: 9000
# Protocol: TCP
# Source: Any (or restrict to Azure DevOps IPs)
```

---

## Step 5: Test Webhook

From any machine:

```bash
curl -X POST http://4.240.70.81:9000/hooks/deploy-sdlc \
  -H "Content-Type: application/json" \
  -H "X-Deploy-Token: sdlc-deploy-secret-2026" \
  -d '{"test": "true"}'
```

Expected response: `SDLC-POC deployment triggered!`

---

## Step 6: Verify Deployment

Check logs:

```bash
# Webhook logs
tail -f /var/log/webhook.log

# Deployment logs
tail -f /var/log/sdlc-deploy.log

# Docker container status
docker-compose -f docker-compose.vm.yml ps
```

---

## Security Notes

1. **Change the deploy token** in production:
   - Update `hooks.json` on VM
   - Update `DEPLOY_TOKEN` variable in Azure DevOps pipeline

2. **Restrict webhook access**:
   - Use Azure NSG to allow only Azure DevOps IPs
   - Or use HTTPS with Let's Encrypt

3. **Use secrets in Azure DevOps**:
   - Store `DEPLOY_TOKEN` as a secret variable in pipeline

---

## Troubleshooting

### Webhook not responding

```bash
# Check if service is running
sudo systemctl status webhook

# Check logs
sudo journalctl -u webhook -f

# Restart service
sudo systemctl restart webhook
```

### Deployment failing

```bash
# Check deploy script logs
tail -100 /var/log/sdlc-deploy.log

# Check Docker status
docker-compose -f docker-compose.vm.yml ps
docker-compose -f docker-compose.vm.yml logs
```

### Permission issues

```bash
# Ensure user can run docker
sudo usermod -aG docker aiopslinuxadmin

# Re-login or run
newgrp docker
```

---

## File Locations

| File | Location |
|------|----------|
| Deploy script | `/home/aiopslinuxadmin/SDLC_Demo/scripts/deploy.sh` |
| Webhook config | `/home/aiopslinuxadmin/SDLC_Demo/scripts/webhook/hooks.json` |
| Systemd service | `/etc/systemd/system/webhook.service` |
| Webhook log | `/var/log/webhook.log` |
| Deploy log | `/var/log/sdlc-deploy.log` |
