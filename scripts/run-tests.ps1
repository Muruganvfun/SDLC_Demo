# ============================================
# Order Management System - Test Runner Script
# ============================================

param(
    [switch]$Coverage,
    [switch]$Sonar,
    [string]$SonarToken,
    [string]$Module
)

$ErrorActionPreference = "Stop"

# Set environment
$env:JAVA_HOME = "$PSScriptRoot\..\tools\jdk-22.0.1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$MVN = "$PSScriptRoot\..\tools\apache-maven-3.9.6\bin\mvn.cmd"
$BACKEND_DIR = "$PSScriptRoot\..\backend"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Order Management System - Test Runner" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Function to run Maven
function Invoke-Maven {
    param([string]$Goals, [string]$Options = "")
    $cmd = "& `"$MVN`" -f `"$BACKEND_DIR\pom.xml`" $Goals $Options"
    Write-Host "Running: $cmd" -ForegroundColor Gray
    Invoke-Expression $cmd
    if ($LASTEXITCODE -ne 0) {
        throw "Maven command failed with exit code $LASTEXITCODE"
    }
}

# 1. Run Unit Tests
Write-Host "`n[1/4] Running Unit Tests..." -ForegroundColor Yellow
Write-Host "=" * 50

if ($Module) {
    Invoke-Maven "test -pl $Module -am"
} else {
    Invoke-Maven "test"
}

Write-Host "`n[PASS] Unit tests completed!" -ForegroundColor Green

# 2. Generate Coverage Report
if ($Coverage) {
    Write-Host "`n[2/4] Generating Coverage Report..." -ForegroundColor Yellow
    Write-Host "=" * 50
    
    Invoke-Maven "jacoco:report"
    
    Write-Host "`n[INFO] Coverage reports generated at:" -ForegroundColor Cyan
    Get-ChildItem -Path "$BACKEND_DIR" -Recurse -Filter "jacoco.xml" | ForEach-Object {
        Write-Host "  - $($_.FullName)" -ForegroundColor Gray
    }
    
    # Print summary
    Write-Host "`n[INFO] Coverage Summary:" -ForegroundColor Cyan
    Get-ChildItem -Path "$BACKEND_DIR" -Recurse -Filter "index.html" -Directory | ForEach-Object {
        if ($_.FullName -like "*jacoco*") {
            Write-Host "  - $($_.Parent.Parent.Name): $($_.FullName)" -ForegroundColor Gray
        }
    }
}

# 3. Run SonarQube Analysis
if ($Sonar) {
    Write-Host "`n[3/4] Running SonarQube Analysis..." -ForegroundColor Yellow
    Write-Host "=" * 50
    
    $sonarOpts = "-Dsonar.host.url=http://localhost:9000"
    if ($SonarToken) {
        $sonarOpts += " -Dsonar.token=$SonarToken"
    }
    
    Invoke-Maven "sonar:sonar" $sonarOpts
    
    Write-Host "`n[INFO] SonarQube analysis complete!" -ForegroundColor Cyan
    Write-Host "  View results at: http://localhost:9000/dashboard?id=oms-order-management-system" -ForegroundColor Gray
}

# 4. Summary
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "  Test Execution Summary" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Unit Tests:     PASSED" -ForegroundColor Green
if ($Coverage) {
    Write-Host "  Coverage:       GENERATED" -ForegroundColor Green
}
if ($Sonar) {
    Write-Host "  SonarQube:      ANALYZED" -ForegroundColor Green
}
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan

# Usage instructions
Write-Host "`nUsage Examples:" -ForegroundColor Yellow
Write-Host "  .\run-tests.ps1                    # Run unit tests only" -ForegroundColor Gray
Write-Host "  .\run-tests.ps1 -Coverage          # Run tests with coverage" -ForegroundColor Gray
Write-Host "  .\run-tests.ps1 -Sonar             # Run tests and SonarQube analysis" -ForegroundColor Gray
Write-Host "  .\run-tests.ps1 -Coverage -Sonar   # Full analysis" -ForegroundColor Gray
Write-Host "  .\run-tests.ps1 -Module auth-service  # Test specific module" -ForegroundColor Gray
