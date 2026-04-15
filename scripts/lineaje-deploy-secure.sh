#!/bin/bash
# ============================================
# Lineaje Secure Deployment Script
# Deploy to Azure VM with security validation
# ============================================
# Prerequisites:
#   - SSH access to Azure VM configured
#   - Docker and Docker Compose on VM
#   - Lineaje scan completed (security gate passed)
# ============================================
# Usage: ./scripts/lineaje-deploy-secure.sh <vm-host> [--force]
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
REPORTS_DIR="$PROJECT_ROOT/.lineaje/reports"

VM_HOST="${1:-}"
FORCE_DEPLOY="${2:-}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  Lineaje Secure Deployment${NC}"
    echo -e "${BLUE}============================================${NC}"
}

check_usage() {
    if [ -z "$VM_HOST" ]; then
        echo -e "${RED}ERROR: VM host not specified${NC}"
        echo "Usage: $0 <vm-host> [--force]"
        echo ""
        echo "Examples:"
        echo "  $0 user@10.0.0.4"
        echo "  $0 azureuser@oms-vm.eastus.cloudapp.azure.com"
        echo "  $0 user@10.0.0.4 --force  # Skip security gate"
        exit 1
    fi
}

check_security_gate() {
    echo -e "${YELLOW}Checking security gate status...${NC}"
    
    if [ "$FORCE_DEPLOY" = "--force" ]; then
        echo -e "${YELLOW}WARNING: Bypassing security gate (--force flag)${NC}"
        return 0
    fi
    
    if [ ! -f "$REPORTS_DIR/vulnerability-report.json" ]; then
        echo -e "${RED}ERROR: No vulnerability report found${NC}"
        echo "Run './scripts/lineaje-local-scan.sh' first"
        exit 1
    fi
    
    CRITICAL=$(jq '[.vulnerabilities[]? | select(.severity == "CRITICAL")] | length' \
        "$REPORTS_DIR/vulnerability-report.json" 2>/dev/null || echo "0")
    
    if [ "$CRITICAL" -gt 0 ]; then
        echo -e "${RED}============================================${NC}"
        echo -e "${RED}  SECURITY GATE FAILED${NC}"
        echo -e "${RED}============================================${NC}"
        echo -e "${RED}  $CRITICAL critical vulnerabilities detected${NC}"
        echo ""
        echo "Options:"
        echo "  1. Apply Lineaje fix plans: droid lineaje-remediation"
        echo "  2. Force deploy (not recommended): $0 $VM_HOST --force"
        echo -e "${RED}============================================${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Security gate PASSED${NC}"
}

validate_ssh_access() {
    echo -e "${YELLOW}Validating SSH access to $VM_HOST...${NC}"
    
    if ! ssh -o ConnectTimeout=10 -o BatchMode=yes "$VM_HOST" "echo 'SSH OK'" 2>/dev/null; then
        echo -e "${RED}ERROR: Cannot connect to $VM_HOST${NC}"
        echo "Ensure SSH key is configured and VM is accessible"
        exit 1
    fi
    
    echo -e "${GREEN}SSH access validated${NC}"
}

check_remote_docker() {
    echo -e "${YELLOW}Checking Docker on remote VM...${NC}"
    
    if ! ssh "$VM_HOST" "docker --version && docker-compose --version" 2>/dev/null; then
        echo -e "${RED}ERROR: Docker or Docker Compose not found on VM${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Docker available on remote VM${NC}"
}

sync_compose_files() {
    echo -e "${YELLOW}Syncing Docker Compose files...${NC}"
    
    # Create deployment directory on VM
    ssh "$VM_HOST" "mkdir -p /app/oms"
    
    # Sync docker-compose and related files
    rsync -avz --progress \
        "$PROJECT_ROOT/docker-compose.yml" \
        "$PROJECT_ROOT/docker-compose.vm.yml" \
        "$VM_HOST:/app/oms/"
    
    echo -e "${GREEN}Compose files synced${NC}"
}

build_and_push_images() {
    echo -e "${YELLOW}Building and pushing Docker images...${NC}"
    
    # This assumes images are built locally and pushed to a registry
    # Modify based on your registry setup
    
    cd "$PROJECT_ROOT"
    
    # Build backend services
    echo "Building backend services..."
    cd backend
    mvn clean package -DskipTests -B -q
    
    # Build Docker images
    for service in api-gateway auth-service catalog-service order-service inventory-service payment-service shipping-service notification-service; do
        echo "Building $service..."
        docker build -t "oms/$service:latest" "./$service" -q
    done
    
    # Build frontend
    echo "Building frontend..."
    cd ../frontend
    npm ci --silent
    npm run build:web --silent || npx expo export --platform web
    docker build -t "oms/frontend:latest" . -q
    
    cd "$PROJECT_ROOT"
    echo -e "${GREEN}Images built${NC}"
    
    # If using a registry, push images here
    # docker push registry.example.com/oms/api-gateway:latest
    # For direct VM deployment, we'll use docker save/load
}

transfer_images() {
    echo -e "${YELLOW}Transferring images to VM...${NC}"
    
    # Save images to a tar file
    IMAGES="oms/api-gateway oms/auth-service oms/catalog-service oms/order-service oms/inventory-service oms/payment-service oms/shipping-service oms/notification-service oms/frontend"
    
    echo "Saving images..."
    docker save $IMAGES | gzip > /tmp/oms-images.tar.gz
    
    echo "Transferring to VM (this may take a while)..."
    rsync -avz --progress /tmp/oms-images.tar.gz "$VM_HOST:/tmp/"
    
    echo "Loading images on VM..."
    ssh "$VM_HOST" "gunzip -c /tmp/oms-images.tar.gz | docker load"
    
    # Cleanup
    rm -f /tmp/oms-images.tar.gz
    ssh "$VM_HOST" "rm -f /tmp/oms-images.tar.gz"
    
    echo -e "${GREEN}Images transferred${NC}"
}

deploy_to_vm() {
    echo -e "${YELLOW}Deploying to VM...${NC}"
    
    ssh "$VM_HOST" << 'DEPLOY_SCRIPT'
        cd /app/oms
        
        # Stop existing containers
        docker-compose down || true
        
        # Pull latest images (if using registry)
        # docker-compose pull
        
        # Start with VM-specific compose file if exists
        if [ -f "docker-compose.vm.yml" ]; then
            docker-compose -f docker-compose.yml -f docker-compose.vm.yml up -d
        else
            docker-compose up -d
        fi
        
        # Wait for services to be healthy
        echo "Waiting for services to start..."
        sleep 30
        
        # Health check
        docker-compose ps
DEPLOY_SCRIPT
    
    echo -e "${GREEN}Deployment completed${NC}"
}

upload_security_reports() {
    echo -e "${YELLOW}Uploading security reports to VM...${NC}"
    
    if [ -d "$REPORTS_DIR" ]; then
        ssh "$VM_HOST" "mkdir -p /app/oms/security-reports"
        rsync -avz "$REPORTS_DIR/" "$VM_HOST:/app/oms/security-reports/"
        echo -e "${GREEN}Security reports uploaded${NC}"
    fi
}

display_summary() {
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${GREEN}  DEPLOYMENT SUCCESSFUL${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
    echo "  VM Host: $VM_HOST"
    echo "  App URL: http://$VM_HOST:3000 (frontend)"
    echo "  API URL: http://$VM_HOST:8080 (api-gateway)"
    echo ""
    echo "  Useful commands:"
    echo "    ssh $VM_HOST 'cd /app/oms && docker-compose logs -f'"
    echo "    ssh $VM_HOST 'cd /app/oms && docker-compose ps'"
    echo "    ssh $VM_HOST 'cd /app/oms && docker-compose restart'"
    echo ""
    echo -e "${BLUE}============================================${NC}"
}

# Main execution
print_header
check_usage
check_security_gate
validate_ssh_access
check_remote_docker
sync_compose_files
build_and_push_images
transfer_images
deploy_to_vm
upload_security_reports
display_summary
