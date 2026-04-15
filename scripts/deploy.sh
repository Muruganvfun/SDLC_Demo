#!/bin/bash
# ============================================
# SDLC-POC Deployment Script
# ============================================
# This script is triggered by webhook after CI passes
# It pulls latest code and redeploys Docker containers
# ============================================

set -e  # Exit on any error

# Configuration
APP_DIR="/data/CUE-POC/SDLC-POC"
FRONTEND_DIR="/var/www/html/cue-sdlc-demo"
COMPOSE_FILE="docker-compose.vm.yml"
BRANCH="develop"
LOG_FILE="/var/log/sdlc-deploy.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

log "${GREEN}========================================${NC}"
log "${GREEN}SDLC-POC Deployment Started${NC}"
log "${GREEN}========================================${NC}"

# Navigate to app directory
cd "$APP_DIR" || {
    log "${RED}ERROR: Cannot access $APP_DIR${NC}"
    exit 1
}

log "Working directory: $(pwd)"

# Pull latest code
log "${YELLOW}Pulling latest code from $BRANCH...${NC}"
git fetch origin
git checkout "$BRANCH"
git pull origin "$BRANCH"

log "Latest commit: $(git log -1 --oneline)"

# Build and deploy backend containers
log "${YELLOW}Building Docker images...${NC}"
docker-compose -f "$COMPOSE_FILE" build --parallel

log "${YELLOW}Stopping existing containers...${NC}"
docker-compose -f "$COMPOSE_FILE" down

log "${YELLOW}Starting new containers...${NC}"
docker-compose -f "$COMPOSE_FILE" up -d

# Deploy frontend to Nginx
log "${YELLOW}Deploying frontend to Nginx...${NC}"
if [ -d "frontend/dist" ]; then
    cp -r frontend/dist/* "$FRONTEND_DIR/"
    log "Frontend deployed to $FRONTEND_DIR"
elif [ -d "frontend/build" ]; then
    cp -r frontend/build/* "$FRONTEND_DIR/"
    log "Frontend deployed to $FRONTEND_DIR"
else
    log "${YELLOW}No frontend build found, skipping frontend deployment${NC}"
fi

# Wait for services to be healthy
log "${YELLOW}Waiting for services to be healthy...${NC}"
sleep 30

# Check container status
log "${YELLOW}Container Status:${NC}"
docker-compose -f "$COMPOSE_FILE" ps

# Cleanup old images
log "${YELLOW}Cleaning up unused Docker images...${NC}"
docker image prune -f

log "${GREEN}========================================${NC}"
log "${GREEN}Deployment Completed Successfully!${NC}"
log "${GREEN}========================================${NC}"

exit 0
