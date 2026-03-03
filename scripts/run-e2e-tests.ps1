# ============================================
# Order Management System - E2E Test Runner
# ============================================

param(
    [switch]$StartServices,
    [switch]$StopServices,
    [string]$Module,
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"

# Set environment
$env:JAVA_HOME = "$PSScriptRoot\..\tools\jdk-22.0.1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$MVN = "$PSScriptRoot\..\tools\apache-maven-3.9.6\bin\mvn.cmd"
$BACKEND_DIR = "$PSScriptRoot\..\backend"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Order Management System - E2E Tests" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Function to run Maven
function Invoke-Maven {
    param([string]$Goals, [string]$Options = "")
    $cmd = "& `"$MVN`" -f `"$BACKEND_DIR\pom.xml`" $Goals $Options"
    if ($Verbose) {
        Write-Host "Running: $cmd" -ForegroundColor Gray
    }
    Invoke-Expression $cmd
    if ($LASTEXITCODE -ne 0) {
        throw "Maven command failed with exit code $LASTEXITCODE"
    }
}

# Start services if requested
if ($StartServices) {
    Write-Host "`n[SETUP] Starting backend services..." -ForegroundColor Yellow
    
    $services = @(
        @{name="auth-service"; port=8081},
        @{name="catalog-service"; port=8082},
        @{name="order-service"; port=8083},
        @{name="inventory-service"; port=8084},
        @{name="payment-service"; port=8085},
        @{name="shipping-service"; port=8086},
        @{name="notification-service"; port=8087},
        @{name="api-gateway"; port=8080}
    )
    
    foreach ($svc in $services) {
        $warPath = "$BACKEND_DIR\$($svc.name)\target\$($svc.name)-1.0.0-SNAPSHOT.war"
        if (Test-Path $warPath) {
            Write-Host "  Starting $($svc.name)..." -ForegroundColor Gray
            Start-Process -FilePath "$env:JAVA_HOME\bin\java.exe" -ArgumentList "-jar", $warPath -WindowStyle Hidden
        } else {
            Write-Host "  [WARN] WAR not found for $($svc.name), skipping..." -ForegroundColor Yellow
        }
    }
    
    Write-Host "  Waiting 30 seconds for services to start..." -ForegroundColor Gray
    Start-Sleep -Seconds 30
}

# Run E2E Integration Tests
Write-Host "`n[TEST] Running E2E Integration Tests..." -ForegroundColor Yellow
Write-Host "=" * 50

$testOptions = "-Dtest=*IntegrationTest -DfailIfNoTests=false"

if ($Module) {
    Write-Host "  Running tests for module: $Module" -ForegroundColor Gray
    Invoke-Maven "test -pl $Module -am" $testOptions
} else {
    Write-Host "  Running all integration tests..." -ForegroundColor Gray
    Invoke-Maven "test" $testOptions
}

Write-Host "`n[PASS] E2E Integration tests completed!" -ForegroundColor Green

# Stop services if requested
if ($StopServices) {
    Write-Host "`n[CLEANUP] Stopping backend services..." -ForegroundColor Yellow
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force
    Write-Host "  All Java processes stopped." -ForegroundColor Gray
}

# Summary
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "  E2E Test Execution Summary" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Integration Tests: COMPLETED" -ForegroundColor Green
Write-Host ""

# Show test report locations
Write-Host "Test Reports:" -ForegroundColor Yellow
Get-ChildItem -Path "$BACKEND_DIR" -Recurse -Filter "surefire-reports" -Directory | ForEach-Object {
    Write-Host "  - $($_.FullName)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan

# Usage instructions
Write-Host "`nUsage Examples:" -ForegroundColor Yellow
Write-Host "  .\run-e2e-tests.ps1                           # Run E2E tests only" -ForegroundColor Gray
Write-Host "  .\run-e2e-tests.ps1 -StartServices            # Start services then run tests" -ForegroundColor Gray
Write-Host "  .\run-e2e-tests.ps1 -Module auth-service      # Test specific module" -ForegroundColor Gray
Write-Host "  .\run-e2e-tests.ps1 -StartServices -StopServices  # Full cycle" -ForegroundColor Gray
