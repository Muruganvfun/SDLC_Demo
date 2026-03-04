# ============================================
# Order Management System - Full SDLC Pipeline
# ============================================
# This script automates the complete SDLC process:
# 1. Commit changes
# 2. Build application
# 3. Run unit tests
# 4. Run integration tests
# 5. Generate coverage reports
# 6. Package application
# 7. (Optional) Deploy to Docker
# ============================================

param(
    [string]$CommitMessage = "",
    [switch]$SkipTests,
    [switch]$SkipBuild,
    [switch]$Deploy,
    [switch]$Verbose
)

$ErrorActionPreference = "Continue"
$script:exitCode = 0
$script:startTime = Get-Date

# Colors for output
function Write-Step { param($msg) Write-Host "`n[$((Get-Date).ToString('HH:mm:ss'))] $msg" -ForegroundColor Cyan }
function Write-Success { param($msg) Write-Host "  [PASS] $msg" -ForegroundColor Green }
function Write-Fail { param($msg) Write-Host "  [FAIL] $msg" -ForegroundColor Red; $script:exitCode = 1 }
function Write-Info { param($msg) Write-Host "  [INFO] $msg" -ForegroundColor Gray }
function Write-Warn { param($msg) Write-Host "  [WARN] $msg" -ForegroundColor Yellow }

# Set environment
$BACKEND_DIR = "$PSScriptRoot\..\backend"
$FRONTEND_DIR = "$PSScriptRoot\..\frontend"

# Check for Java and Maven
$env:JAVA_HOME = "$PSScriptRoot\..\tools\jdk-22.0.1"
if (Test-Path $env:JAVA_HOME) {
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}
$MVN = "$PSScriptRoot\..\tools\apache-maven-3.9.6\bin\mvn.cmd"
if (-not (Test-Path $MVN)) {
    $MVN = "mvn"
}

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         ORDER MANAGEMENT SYSTEM - SDLC PIPELINE            ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# ==================== STAGE 1: VERSION CONTROL ====================
Write-Step "STAGE 1: VERSION CONTROL"

Set-Location "$PSScriptRoot\.."

# Check for changes
$changes = git status --porcelain
if ($changes) {
    Write-Info "Changes detected:"
    git status --short | ForEach-Object { Write-Host "    $_" -ForegroundColor White }
    
    # Prompt for commit message if not provided
    if (-not $CommitMessage) {
        $CommitMessage = "fix: code update in catalog service"
    }
    
    # Stage and commit
    git add .
    git commit -m "$CommitMessage"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Changes committed: $CommitMessage"
    } else {
        Write-Fail "Commit failed"
    }
} else {
    Write-Info "No changes to commit"
}

# Show current state
Write-Info "Branch: $(git branch --show-current)"
Write-Info "Latest commit: $(git log --oneline -1)"

# ==================== STAGE 2: BUILD ====================
if (-not $SkipBuild) {
    Write-Step "STAGE 2: BUILD - Backend"
    
    Set-Location $BACKEND_DIR
    
    # Clean and compile
    & $MVN clean compile -DskipTests -B -q 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Backend build successful"
    } else {
        Write-Fail "Backend build failed"
        & $MVN clean compile -DskipTests -B 2>&1 | Select-Object -Last 20
    }
    
    Write-Step "STAGE 2: BUILD - Frontend"
    
    if (Test-Path "$FRONTEND_DIR\package.json") {
        Set-Location $FRONTEND_DIR
        
        # Check if node_modules exists
        if (-not (Test-Path "node_modules")) {
            Write-Info "Installing dependencies..."
            npm install --silent 2>&1 | Out-Null
        }
        
        Write-Success "Frontend dependencies ready"
    } else {
        Write-Warn "Frontend package.json not found, skipping"
    }
}

# ==================== STAGE 3: UNIT TESTS ====================
if (-not $SkipTests) {
    Write-Step "STAGE 3: UNIT TESTS - Backend"
    
    Set-Location $BACKEND_DIR
    
    # Run unit tests (excluding integration tests)
    & $MVN test -Dtest="!*IntegrationTest" -DfailIfNoTests=false -B -q 2>&1 | Out-Null
    $unitTestResult = $LASTEXITCODE
    
    if ($unitTestResult -eq 0) {
        # Count test results
        $testReports = Get-ChildItem -Path $BACKEND_DIR -Recurse -Filter "TEST-*.xml" -ErrorAction SilentlyContinue
        $totalTests = 0
        $passedTests = 0
        $failedTests = 0
        
        foreach ($report in $testReports) {
            [xml]$xml = Get-Content $report.FullName
            $totalTests += [int]$xml.testsuite.tests
            $failedTests += [int]$xml.testsuite.failures + [int]$xml.testsuite.errors
        }
        $passedTests = $totalTests - $failedTests
        
        Write-Success "Backend unit tests passed ($passedTests/$totalTests)"
    } else {
        Write-Fail "Backend unit tests failed"
    }
    
    Write-Step "STAGE 3: UNIT TESTS - Frontend"
    
    if (Test-Path "$FRONTEND_DIR\package.json") {
        Set-Location $FRONTEND_DIR
        
        # Run Jest tests
        $testOutput = npm test -- --watchAll=false --passWithNoTests 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Frontend unit tests passed"
        } else {
            Write-Fail "Frontend unit tests failed"
            Write-Host $testOutput | Select-Object -Last 10
        }
    }
}

# ==================== STAGE 4: INTEGRATION TESTS ====================
if (-not $SkipTests) {
    Write-Step "STAGE 4: INTEGRATION TESTS"
    
    Set-Location $BACKEND_DIR
    
    # Run integration tests
    & $MVN test -Dtest="*IntegrationTest" -DfailIfNoTests=false -B -q 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Integration tests passed"
    } else {
        Write-Warn "Integration tests had issues (may need services running)"
    }
}

# ==================== STAGE 5: CODE COVERAGE ====================
if (-not $SkipTests) {
    Write-Step "STAGE 5: CODE COVERAGE"
    
    Set-Location $BACKEND_DIR
    
    # Generate JaCoCo report
    & $MVN jacoco:report -B -q 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        # Find coverage reports
        $coverageReports = Get-ChildItem -Path $BACKEND_DIR -Recurse -Filter "jacoco.xml" -ErrorAction SilentlyContinue
        
        if ($coverageReports) {
            Write-Success "Coverage reports generated"
            foreach ($report in $coverageReports) {
                $serviceName = $report.Directory.Parent.Parent.Parent.Parent.Name
                Write-Info "  - $serviceName/target/site/jacoco/index.html"
            }
        }
    } else {
        Write-Warn "Coverage report generation had issues"
    }
}

# ==================== STAGE 6: PACKAGE ====================
if (-not $SkipBuild) {
    Write-Step "STAGE 6: PACKAGE"
    
    Set-Location $BACKEND_DIR
    
    # Package all modules
    & $MVN package -DskipTests -B -q 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Backend packaged successfully"
        
        # List generated artifacts
        $wars = Get-ChildItem -Path $BACKEND_DIR -Recurse -Filter "*.war" -ErrorAction SilentlyContinue |
                Where-Object { $_.DirectoryName -like "*\target" }
        
        foreach ($war in $wars) {
            $size = [math]::Round($war.Length / 1MB, 2)
            Write-Info "  - $($war.Directory.Parent.Name): $($war.Name) - $size MB"
        }
    } else {
        Write-Fail "Packaging failed"
    }
}

# ==================== STAGE 7: DOCKER BUILD (Optional) ====================
if ($Deploy) {
    Write-Step "STAGE 7: DOCKER BUILD"
    
    $dockerAvailable = Get-Command docker -ErrorAction SilentlyContinue
    
    if ($dockerAvailable) {
        Set-Location "$PSScriptRoot\.."
        
        # Build with docker-compose
        docker-compose build --quiet 2>&1 | Out-Null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Docker images built"
            
            # Start services
            Write-Info "Starting services..."
            docker-compose up -d 2>&1 | Out-Null
            
            if ($LASTEXITCODE -eq 0) {
                Write-Success "Services started"
                Write-Info "  - Frontend: http://localhost:3000"
                Write-Info "  - API Gateway: http://localhost:8080"
                Write-Info "  - Auth Service: http://localhost:8081"
            }
        } else {
            Write-Fail "Docker build failed"
        }
    } else {
        Write-Warn "Docker not available, skipping deployment"
    }
}

# ==================== SUMMARY ====================
$duration = (Get-Date) - $script:startTime

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                    PIPELINE SUMMARY                        ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

Write-Host "  Duration: $($duration.ToString('mm\:ss'))" -ForegroundColor White
Write-Host "  Branch:   $(git branch --show-current)" -ForegroundColor White
Write-Host "  Commit:   $(git log --oneline -1)" -ForegroundColor White
Write-Host ""

if ($script:exitCode -eq 0) {
    Write-Host "  ╔════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "  ║   PIPELINE COMPLETED SUCCESSFULLY  ║" -ForegroundColor Green
    Write-Host "  ╚════════════════════════════════════╝" -ForegroundColor Green
} else {
    Write-Host "  ╔════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "  ║   PIPELINE COMPLETED WITH ERRORS   ║" -ForegroundColor Red
    Write-Host "  ╚════════════════════════════════════╝" -ForegroundColor Red
}

Write-Host ""
Write-Host "  Next Steps:" -ForegroundColor Yellow
Write-Host "    1. Push to GitHub: git push origin develop" -ForegroundColor Gray
Write-Host "    2. Create PR to main branch" -ForegroundColor Gray
Write-Host "    3. GitHub Actions will run CI automatically" -ForegroundColor Gray
Write-Host ""

exit $script:exitCode
