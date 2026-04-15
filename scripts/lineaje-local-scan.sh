#!/bin/bash
# ============================================
# Lineaje Local Scan Script
# Run security scans locally before pushing
# ============================================
# Prerequisites:
#   - LINEAJE_API_KEY environment variable set
#   - LINEAJE_ORG_ID environment variable set
#   - curl, jq installed
# ============================================
# Usage: ./scripts/lineaje-local-scan.sh [--full|--quick]
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
LINEAJE_API_URL="${LINEAJE_API_URL:-https://api.lineaje.com/v1}"
REPORTS_DIR="$PROJECT_ROOT/.lineaje/reports"
SCAN_MODE="${1:---quick}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  Lineaje Local Security Scan${NC}"
    echo -e "${BLUE}============================================${NC}"
}

check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    if [ -z "$LINEAJE_API_KEY" ]; then
        echo -e "${RED}ERROR: LINEAJE_API_KEY environment variable not set${NC}"
        echo "Set it with: export LINEAJE_API_KEY='your-api-key'"
        exit 1
    fi
    
    if [ -z "$LINEAJE_ORG_ID" ]; then
        echo -e "${RED}ERROR: LINEAJE_ORG_ID environment variable not set${NC}"
        echo "Set it with: export LINEAJE_ORG_ID='your-org-id'"
        exit 1
    fi
    
    for cmd in curl jq; do
        if ! command -v $cmd &> /dev/null; then
            echo -e "${RED}ERROR: $cmd is required but not installed${NC}"
            exit 1
        fi
    done
    
    echo -e "${GREEN}Prerequisites OK${NC}"
}

generate_backend_sbom() {
    echo -e "${YELLOW}Generating Backend SBOM...${NC}"
    cd "$PROJECT_ROOT/backend"
    
    if command -v mvn &> /dev/null; then
        mvn org.cyclonedx:cyclonedx-maven-plugin:2.7.11:makeAggregateBom \
            -DoutputFormat=json \
            -DoutputName=backend-sbom \
            -q -B
        mkdir -p "$REPORTS_DIR"
        cp target/backend-sbom.json "$REPORTS_DIR/"
        echo -e "${GREEN}Backend SBOM generated${NC}"
    else
        echo -e "${YELLOW}Maven not found, skipping backend SBOM${NC}"
    fi
    
    cd "$PROJECT_ROOT"
}

generate_frontend_sbom() {
    echo -e "${YELLOW}Generating Frontend SBOM...${NC}"
    cd "$PROJECT_ROOT/frontend"
    
    if command -v npm &> /dev/null; then
        npx @cyclonedx/cyclonedx-npm --output-format JSON \
            --output-file "$REPORTS_DIR/frontend-sbom.json" 2>/dev/null || {
            echo -e "${YELLOW}Installing CycloneDX npm plugin...${NC}"
            npm install -g @cyclonedx/cyclonedx-npm
            npx @cyclonedx/cyclonedx-npm --output-format JSON \
                --output-file "$REPORTS_DIR/frontend-sbom.json"
        }
        echo -e "${GREEN}Frontend SBOM generated${NC}"
    else
        echo -e "${YELLOW}npm not found, skipping frontend SBOM${NC}"
    fi
    
    cd "$PROJECT_ROOT"
}

upload_and_scan() {
    echo -e "${YELLOW}Uploading SBOMs to Lineaje...${NC}"
    
    # Upload Backend SBOM
    if [ -f "$REPORTS_DIR/backend-sbom.json" ]; then
        echo "Uploading backend SBOM..."
        curl -s -X POST "$LINEAJE_API_URL/sbom/upload" \
            -H "Authorization: Bearer $LINEAJE_API_KEY" \
            -H "Content-Type: application/json" \
            -H "X-Lineaje-Org: $LINEAJE_ORG_ID" \
            -d @"$REPORTS_DIR/backend-sbom.json" \
            -o "$REPORTS_DIR/backend-upload-response.json"
    fi
    
    # Upload Frontend SBOM
    if [ -f "$REPORTS_DIR/frontend-sbom.json" ]; then
        echo "Uploading frontend SBOM..."
        curl -s -X POST "$LINEAJE_API_URL/sbom/upload" \
            -H "Authorization: Bearer $LINEAJE_API_KEY" \
            -H "Content-Type: application/json" \
            -H "X-Lineaje-Org: $LINEAJE_ORG_ID" \
            -d @"$REPORTS_DIR/frontend-sbom.json" \
            -o "$REPORTS_DIR/frontend-upload-response.json"
    fi
    
    echo -e "${GREEN}SBOMs uploaded${NC}"
}

trigger_scan() {
    echo -e "${YELLOW}Triggering Lineaje scan...${NC}"
    
    BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
    COMMIT=$(git rev-parse HEAD 2>/dev/null || echo "unknown")
    
    SCAN_RESPONSE=$(curl -s -X POST "$LINEAJE_API_URL/scan/trigger" \
        -H "Authorization: Bearer $LINEAJE_API_KEY" \
        -H "Content-Type: application/json" \
        -H "X-Lineaje-Org: $LINEAJE_ORG_ID" \
        -d "{
            \"project_name\": \"order-management-system\",
            \"branch\": \"$BRANCH\",
            \"commit_sha\": \"$COMMIT\",
            \"scan_type\": \"$SCAN_MODE\"
        }")
    
    SCAN_ID=$(echo "$SCAN_RESPONSE" | jq -r '.scan_id // empty')
    
    if [ -z "$SCAN_ID" ]; then
        echo -e "${RED}Failed to trigger scan${NC}"
        echo "$SCAN_RESPONSE"
        exit 1
    fi
    
    echo "Scan ID: $SCAN_ID"
    echo "$SCAN_ID" > "$REPORTS_DIR/current-scan-id.txt"
}

wait_for_results() {
    echo -e "${YELLOW}Waiting for scan results...${NC}"
    
    SCAN_ID=$(cat "$REPORTS_DIR/current-scan-id.txt")
    MAX_ATTEMPTS=30
    ATTEMPT=0
    
    while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
        STATUS=$(curl -s -X GET "$LINEAJE_API_URL/scan/$SCAN_ID/status" \
            -H "Authorization: Bearer $LINEAJE_API_KEY" \
            -H "X-Lineaje-Org: $LINEAJE_ORG_ID" | jq -r '.status // "pending"')
        
        if [ "$STATUS" = "completed" ]; then
            echo -e "${GREEN}Scan completed!${NC}"
            break
        elif [ "$STATUS" = "failed" ]; then
            echo -e "${RED}Scan failed!${NC}"
            exit 1
        fi
        
        echo "Status: $STATUS (attempt $((ATTEMPT+1))/$MAX_ATTEMPTS)"
        sleep 5
        ATTEMPT=$((ATTEMPT + 1))
    done
}

fetch_results() {
    echo -e "${YELLOW}Fetching vulnerability report...${NC}"
    
    SCAN_ID=$(cat "$REPORTS_DIR/current-scan-id.txt")
    
    # Fetch vulnerabilities
    curl -s -X GET "$LINEAJE_API_URL/scan/$SCAN_ID/vulnerabilities" \
        -H "Authorization: Bearer $LINEAJE_API_KEY" \
        -H "X-Lineaje-Org: $LINEAJE_ORG_ID" \
        -o "$REPORTS_DIR/vulnerability-report.json"
    
    # Fetch fix plans
    curl -s -X POST "$LINEAJE_API_URL/sbom360/fix-plans" \
        -H "Authorization: Bearer $LINEAJE_API_KEY" \
        -H "Content-Type: application/json" \
        -H "X-Lineaje-Org: $LINEAJE_ORG_ID" \
        -d "{
            \"scan_id\": \"$SCAN_ID\",
            \"fix_strategy\": \"compatible\",
            \"include_transitive\": true
        }" \
        -o "$REPORTS_DIR/fix-plans.json"
    
    echo -e "${GREEN}Reports saved to $REPORTS_DIR${NC}"
}

display_summary() {
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  VULNERABILITY SUMMARY${NC}"
    echo -e "${BLUE}============================================${NC}"
    
    if [ -f "$REPORTS_DIR/vulnerability-report.json" ]; then
        CRITICAL=$(jq '[.vulnerabilities[]? | select(.severity == "CRITICAL")] | length' "$REPORTS_DIR/vulnerability-report.json" 2>/dev/null || echo "0")
        HIGH=$(jq '[.vulnerabilities[]? | select(.severity == "HIGH")] | length' "$REPORTS_DIR/vulnerability-report.json" 2>/dev/null || echo "0")
        MEDIUM=$(jq '[.vulnerabilities[]? | select(.severity == "MEDIUM")] | length' "$REPORTS_DIR/vulnerability-report.json" 2>/dev/null || echo "0")
        LOW=$(jq '[.vulnerabilities[]? | select(.severity == "LOW")] | length' "$REPORTS_DIR/vulnerability-report.json" 2>/dev/null || echo "0")
        
        [ "$CRITICAL" -gt 0 ] && echo -e "${RED}  Critical: $CRITICAL${NC}" || echo -e "${GREEN}  Critical: $CRITICAL${NC}"
        [ "$HIGH" -gt 0 ] && echo -e "${YELLOW}  High:     $HIGH${NC}" || echo -e "${GREEN}  High:     $HIGH${NC}"
        echo "  Medium:   $MEDIUM"
        echo "  Low:      $LOW"
    else
        echo "  No vulnerability report available"
    fi
    
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  FIX PLANS AVAILABLE${NC}"
    echo -e "${BLUE}============================================${NC}"
    
    if [ -f "$REPORTS_DIR/fix-plans.json" ]; then
        FIX_COUNT=$(jq '.fix_plans | length' "$REPORTS_DIR/fix-plans.json" 2>/dev/null || echo "0")
        echo "  Total fix plans: $FIX_COUNT"
        
        if [ "$FIX_COUNT" -gt 0 ]; then
            echo ""
            echo "  Top recommendations:"
            jq -r '.fix_plans[:5][] | "  - \(.component): \(.current_version) -> \(.recommended_version)"' \
                "$REPORTS_DIR/fix-plans.json" 2>/dev/null || true
        fi
    else
        echo "  No fix plans available"
    fi
    
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  NEXT STEPS${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo "  1. Review reports in: $REPORTS_DIR"
    echo "  2. Apply fixes with: droid lineaje-remediation"
    echo "  3. Run tests: cd backend && mvn test"
    echo -e "${BLUE}============================================${NC}"
}

# Main execution
print_header
check_prerequisites

mkdir -p "$REPORTS_DIR"

if [ "$SCAN_MODE" = "--full" ]; then
    echo -e "${YELLOW}Running FULL scan (includes container scanning)${NC}"
    generate_backend_sbom
    generate_frontend_sbom
    upload_and_scan
    trigger_scan
    wait_for_results
    fetch_results
else
    echo -e "${YELLOW}Running QUICK scan (source dependencies only)${NC}"
    generate_backend_sbom
    generate_frontend_sbom
    upload_and_scan
    trigger_scan
    wait_for_results
    fetch_results
fi

display_summary
