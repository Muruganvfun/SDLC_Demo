<#
.SYNOPSIS
    Triggers the Lineaje Security Pipeline in Azure DevOps to scan for vulnerabilities.

.DESCRIPTION
    This script triggers the lineaje-security-pipeline in Azure DevOps using REST API.
    It can be invoked by Factory.AI droids for automated security scanning.

.PARAMETER Branch
    The branch to scan (default: develop)

.PARAMETER Wait
    Wait for pipeline completion and return results

.PARAMETER PAT
    Azure DevOps Personal Access Token (or use AZURE_DEVOPS_PAT env var)

.EXAMPLE
    .\scripts\trigger-lineaje-pipeline.ps1 -Branch develop -Wait
#>

param(
    [string]$Branch = "develop",
    [switch]$Wait,
    [string]$PAT = $env:AZURE_DEVOPS_PAT,
    [int]$TimeoutMinutes = 30
)

$ErrorActionPreference = "Stop"

# Azure DevOps Configuration
$Organization = "HOLMES-APPS"
$Project = "WINGS-POC"
$PipelineName = "lineaje-security-pipeline"
$ApiVersion = "7.1"

# Validate PAT
if (-not $PAT) {
    Write-Host "ERROR: Azure DevOps PAT not provided." -ForegroundColor Red
    Write-Host "Set AZURE_DEVOPS_PAT environment variable or use -PAT parameter" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To create a PAT:" -ForegroundColor Cyan
    Write-Host "1. Go to https://dev.azure.com/$Organization/_usersSettings/tokens"
    Write-Host "2. Create new token with 'Build (Read & execute)' permission"
    exit 1
}

# Create auth header
$Base64Auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(":$PAT"))
$Headers = @{
    "Authorization" = "Basic $Base64Auth"
    "Content-Type" = "application/json"
}

$BaseUrl = "https://dev.azure.com/$Organization/$Project/_apis"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Lineaje Security Pipeline Trigger" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Organization: $Organization"
Write-Host "  Project: $Project"
Write-Host "  Branch: $Branch"
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Find the pipeline ID
Write-Host "Finding pipeline '$PipelineName'..." -ForegroundColor Yellow
try {
    $PipelinesUrl = "$BaseUrl/pipelines?api-version=$ApiVersion"
    $Pipelines = Invoke-RestMethod -Uri $PipelinesUrl -Headers $Headers -Method Get
    
    $Pipeline = $Pipelines.value | Where-Object { $_.name -eq $PipelineName }
    
    if (-not $Pipeline) {
        # Try to find by partial name match
        $Pipeline = $Pipelines.value | Where-Object { $_.name -like "*lineaje*" -or $_.name -like "*security*" }
        if ($Pipeline) {
            Write-Host "Found similar pipeline: $($Pipeline.name)" -ForegroundColor Yellow
        }
    }
    
    if (-not $Pipeline) {
        Write-Host "Pipeline '$PipelineName' not found." -ForegroundColor Red
        Write-Host "Available pipelines:" -ForegroundColor Yellow
        $Pipelines.value | ForEach-Object { Write-Host "  - $($_.name) (ID: $($_.id))" }
        exit 1
    }
    
    $PipelineId = $Pipeline.id
    Write-Host "Found pipeline ID: $PipelineId" -ForegroundColor Green
}
catch {
    Write-Host "Error finding pipeline: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Trigger the pipeline
Write-Host ""
Write-Host "Triggering pipeline run on branch '$Branch'..." -ForegroundColor Yellow

try {
    $RunUrl = "$BaseUrl/pipelines/$PipelineId/runs?api-version=$ApiVersion"
    $Body = @{
        resources = @{
            repositories = @{
                self = @{
                    refName = "refs/heads/$Branch"
                }
            }
        }
    } | ConvertTo-Json -Depth 10
    
    $Run = Invoke-RestMethod -Uri $RunUrl -Headers $Headers -Method Post -Body $Body
    $RunId = $Run.id
    $RunUrl = $Run._links.web.href
    
    Write-Host "Pipeline triggered successfully!" -ForegroundColor Green
    Write-Host "  Run ID: $RunId"
    Write-Host "  URL: $RunUrl"
}
catch {
    Write-Host "Error triggering pipeline: $_" -ForegroundColor Red
    exit 1
}

# Step 3: Wait for completion (optional)
if ($Wait) {
    Write-Host ""
    Write-Host "Waiting for pipeline completion (timeout: $TimeoutMinutes minutes)..." -ForegroundColor Yellow
    
    $StatusUrl = "$BaseUrl/pipelines/$PipelineId/runs/$RunId`?api-version=$ApiVersion"
    $StartTime = Get-Date
    $TimeoutTime = $StartTime.AddMinutes($TimeoutMinutes)
    
    while ((Get-Date) -lt $TimeoutTime) {
        try {
            $Status = Invoke-RestMethod -Uri $StatusUrl -Headers $Headers -Method Get
            $State = $Status.state
            $Result = $Status.result
            
            Write-Host "  Status: $State $(if($Result){"($Result)"})" -ForegroundColor Cyan
            
            if ($State -eq "completed") {
                Write-Host ""
                if ($Result -eq "succeeded") {
                    Write-Host "Pipeline completed successfully!" -ForegroundColor Green
                }
                elseif ($Result -eq "failed") {
                    Write-Host "Pipeline failed!" -ForegroundColor Red
                    exit 1
                }
                else {
                    Write-Host "Pipeline completed with result: $Result" -ForegroundColor Yellow
                }
                
                # Output results summary
                Write-Host ""
                Write-Host "========================================" -ForegroundColor Cyan
                Write-Host "  SCAN COMPLETE" -ForegroundColor Cyan
                Write-Host "========================================" -ForegroundColor Cyan
                Write-Host "  View results at: $RunUrl"
                Write-Host "  Check Lineaje portal: https://app.veedna.com"
                Write-Host "========================================" -ForegroundColor Cyan
                exit 0
            }
        }
        catch {
            Write-Host "  Error checking status: $_" -ForegroundColor Yellow
        }
        
        Start-Sleep -Seconds 30
    }
    
    Write-Host "Timeout waiting for pipeline completion." -ForegroundColor Yellow
    Write-Host "Check status at: $RunUrl"
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  NEXT STEPS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  1. Monitor pipeline at: $RunUrl"
Write-Host "  2. After completion, check Lineaje portal"
Write-Host "  3. View vulnerability report in pipeline artifacts"
Write-Host "========================================" -ForegroundColor Cyan
