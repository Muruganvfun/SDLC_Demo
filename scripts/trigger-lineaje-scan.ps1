<#
.SYNOPSIS
    Trigger a new Lineaje SBOM scan after remediation
.DESCRIPTION
    This script downloads and runs the Lineaje CLI (veecli) to generate a new SBOM
    for the SDLC-POC project. Run this after making security fixes to verify
    vulnerability reduction in Lineaje portal.
.NOTES
    Prerequisites:
    1. Download veecli from Lineaje portal: Integrations > Configure Scanners > Download CLI
    2. Register the CLI: ./veecli register --devicecode <code_from_portal>
    3. Set environment variables: AZURE_DEVOPS_USER, AZURE_DEVOPS_PAT
.EXAMPLE
    .\trigger-lineaje-scan.ps1
#>

param(
    [string]$Branch = "develop",
    [string]$ProjectName = "SDLC-POC",
    [switch]$SkipDownload
)

$ErrorActionPreference = "Stop"

# Configuration
$LineajeDir = "$PSScriptRoot\..\tools\lineaje"
$VeecliPath = "$LineajeDir\veecli"
$InputJsonPath = "$PSScriptRoot\..\.lineaje\input.json"
$OutputDir = "$PSScriptRoot\..\.lineaje\output"

# Colors for output
function Write-Status($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Success($msg) { Write-Host "[SUCCESS] $msg" -ForegroundColor Green }
function Write-Warning($msg) { Write-Host "[WARNING] $msg" -ForegroundColor Yellow }
function Write-Error($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red }

Write-Status "=== Lineaje SBOM Scan Trigger ==="
Write-Status "Project: $ProjectName"
Write-Status "Branch: $Branch"

# Check if veecli exists
if (-not (Test-Path "$VeecliPath.exe") -and -not (Test-Path $VeecliPath)) {
    Write-Warning "Lineaje CLI (veecli) not found at: $LineajeDir"
    Write-Status ""
    Write-Status "To download the CLI:"
    Write-Status "1. Login to https://app.veedna.com"
    Write-Status "2. Go to Integrations > Configure Scanners > Download CLI"
    Write-Status "3. Extract veecli.tar.gz to: $LineajeDir"
    Write-Status "4. Run: cd $LineajeDir && bash pre.sh"
    Write-Status "5. Register: ./veecli register --devicecode <code_from_portal>"
    Write-Status ""
    
    if (-not $SkipDownload) {
        Write-Error "Please download and configure the Lineaje CLI first."
        exit 1
    }
}

# Check environment variables
$AzureUser = $env:AZURE_DEVOPS_USER
$AzurePAT = $env:AZURE_DEVOPS_PAT

if (-not $AzureUser -or -not $AzurePAT) {
    Write-Warning "Azure DevOps credentials not set."
    Write-Status "Please set the following environment variables:"
    Write-Status "  - AZURE_DEVOPS_USER: Your Azure DevOps username"
    Write-Status "  - AZURE_DEVOPS_PAT: Your Personal Access Token"
    
    # Check if stored in credentials file
    $CredFile = "$PSScriptRoot\..\.factory\credentials\azure-devops.env"
    if (Test-Path $CredFile) {
        Write-Status "Loading credentials from: $CredFile"
        Get-Content $CredFile | ForEach-Object {
            if ($_ -match "^([^=]+)=(.*)$") {
                [Environment]::SetEnvironmentVariable($matches[1], $matches[2])
            }
        }
        $AzureUser = $env:AZURE_DEVOPS_USER
        $AzurePAT = $env:AZURE_DEVOPS_PAT
    }
}

# Create output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

# Update input.json with actual values
$InputJson = Get-Content $InputJsonPath -Raw | ConvertFrom-Json
$InputJson.version = $Branch
$InputJson.project = $ProjectName
if ($AzureUser) {
    $InputJson.repository_access_configs[0].user_name = $AzureUser
}
if ($AzurePAT) {
    $InputJson.repository_access_configs[0].token = $AzurePAT
}

# Write updated input.json
$TempInputJson = "$OutputDir\input-$((Get-Date).ToString('yyyyMMdd-HHmmss')).json"
$InputJson | ConvertTo-Json -Depth 10 | Set-Content $TempInputJson
Write-Status "Generated input file: $TempInputJson"

# Run veecli if available
if (Test-Path "$VeecliPath.exe" -or Test-Path $VeecliPath) {
    Write-Status "Running Lineaje CLI..."
    Write-Status "Command: veecli collect --inputfile $TempInputJson --output $OutputDir"
    
    Push-Location $LineajeDir
    try {
        if (Test-Path "$VeecliPath.exe") {
            & "$VeecliPath.exe" collect --inputfile $TempInputJson --output $OutputDir
        } else {
            & $VeecliPath collect --inputfile $TempInputJson --output $OutputDir
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "SBOM scan completed successfully!"
            Write-Status "Check the Lineaje portal for updated vulnerability report."
            Write-Status "Portal: https://app.veedna.com/app/sbom360/projects"
        } else {
            Write-Error "SBOM scan failed with exit code: $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
} else {
    Write-Status ""
    Write-Status "=== Manual Steps Required ==="
    Write-Status "1. Download Lineaje CLI from portal"
    Write-Status "2. Extract to: $LineajeDir"
    Write-Status "3. Register CLI with device code"
    Write-Status "4. Run this script again"
    Write-Status ""
    Write-Status "Alternatively, use the Git integration in Lineaje portal:"
    Write-Status "1. Go to https://app.veedna.com/app/sbom360/integrations/scan-remotely/git"
    Write-Status "2. Find 'Secure-SDLC-POC' integration"
    Write-Status "3. Click 'Scan' or 'Rescan' button"
}

Write-Status ""
Write-Status "=== Done ==="
