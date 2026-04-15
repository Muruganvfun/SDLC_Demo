---
name: lineaje-security
description: Run Lineaje SBOM360/SCA360 vulnerability scans and analyze results. Use when the user asks about security vulnerabilities, SBOM generation, dependency scanning, or compliance checks.
user-invocable: true
disable-model-invocation: false
---

# Lineaje Security Vulnerability Check

## Purpose

This skill integrates with Lineaje SBOM360 and SCA360 to:
- Generate Software Bill of Materials (SBOM) in CycloneDX format
- Scan for vulnerabilities in dependencies
- Check license compliance
- Validate against security policies (EO 14028, NIST SSDF, FedRAMP)
- Provide remediation recommendations

## When to Use

Invoke this skill when the user:
- Asks about security vulnerabilities in the codebase
- Wants to generate an SBOM
- Needs dependency vulnerability scanning
- Asks about compliance status
- Wants to check if a build is safe to deploy
- Requests security gate validation

## Prerequisites

1. Lineaje CLI installed or API credentials configured
2. Project registered in Lineaje SBOM360 portal
3. `.lineaje/config.yaml` present in project root

## Workflow

### Step 1: Check Lineaje Configuration

Verify the Lineaje configuration exists:

```bash
cat .lineaje/config.yaml
```

If missing, create it with these settings:
```yaml
project:
  name: <project-name>
  version: "1.0.0"

scan:
  default_type: "full"
  include_transitive: true
  fail_on_severity: "CRITICAL"

sbom:
  format: "cyclonedx"
  include_licenses: true

sca360:
  enabled: true

sbom360:
  enabled: true
  fix_strategy: "compatible"
```

### Step 2: Generate SBOM

For Maven projects:
```bash
cd backend && mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
```

For npm projects:
```bash
cd frontend && npx @cyclonedx/cyclonedx-npm --output-file sbom.json
```

### Step 3: Run Vulnerability Scan

Option A - Using Lineaje CLI (if installed):
```bash
lineaje scan --project . --format json --output vulnerability-report.json
```

Option B - Using Lineaje API:
```bash
curl -X POST "https://api.lineaje.com/v1/scan" \
  -H "Authorization: Bearer $LINEAJE_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d @sbom.json
```

Option C - Check Lineaje Portal:
- Navigate to https://sbom360.lineaje.dev
- Select the project
- View latest scan results

### Step 4: Analyze Results

Parse the vulnerability report and categorize findings:

| Severity | Action | Threshold |
|----------|--------|-----------|
| CRITICAL | BLOCK deployment | Any count > 0 |
| HIGH | WARN, require review | Count > 5 |
| MEDIUM | INFO, track in backlog | Count > 20 |
| LOW | PASS, accept risk | No limit |

### Step 5: Generate Remediation Plan

For each vulnerability found:
1. Identify the affected dependency
2. Check if a patched version exists
3. Verify compatibility with current codebase
4. Generate upgrade command or PR

Example remediation commands:
```bash
# Maven dependency upgrade
mvn versions:use-latest-releases -Dincludes=groupId:artifactId

# npm dependency upgrade
npm update vulnerable-package@latest
```

### Step 6: Report Results

Provide a summary in this format:

```
## Security Scan Results

**Project:** <project-name>
**Scan Date:** <date>
**SBOM Format:** CycloneDX 1.5

### Vulnerability Summary
- Critical: X
- High: X
- Medium: X
- Low: X

### Security Gate: PASS/FAIL/WARN

### Top Vulnerabilities
1. CVE-XXXX-XXXX - Package Name - Severity - Fix Available: Yes/No
2. ...

### Recommended Actions
1. Upgrade package-a to version X.Y.Z
2. ...

### Compliance Status
- EO 14028: Compliant/Non-Compliant
- NIST SSDF: Compliant/Non-Compliant
```

## Inputs

| Input | Required | Description |
|-------|----------|-------------|
| Project path | Yes | Path to the project root |
| Scan type | No | "full", "quick", or "dependencies-only" |
| Output format | No | "json", "html", or "markdown" |

## Success Criteria

- SBOM generated successfully
- Vulnerability scan completed
- Results categorized by severity
- Remediation plan provided for critical/high vulnerabilities
- Security gate decision made (PASS/FAIL/WARN)

## Integration with CI/CD

This skill can be invoked in Azure DevOps pipelines:

```yaml
- task: CmdLine@2
  displayName: 'Lineaje Security Scan'
  inputs:
    script: |
      lineaje scan --project $(Build.SourcesDirectory) \
        --fail-on CRITICAL \
        --output $(Build.ArtifactStagingDirectory)/security-report.json
```

## Escalation

If critical vulnerabilities are found:
1. Do NOT proceed with deployment
2. Notify the security team
3. Create Jira tickets for remediation
4. Block the PR/pipeline until resolved

## Related Tools

### Lineaje Remediation Droid
For applying fix plans and generating update PRs, use the `lineaje-remediation` custom droid:
```bash
droid lineaje-remediation
```

This droid can:
- Parse Lineaje vulnerability reports
- Generate dependency update PRs with risk assessment
- Validate compatibility of proposed fixes
- Update Docker base images

### Lineaje Security Pipeline
The project includes a dedicated security pipeline:
- Location: `.azure/pipelines/lineaje-security-pipeline.yml`
- Runs SBOM generation and vulnerability scanning
- Integrates with Azure DevOps

## Triggering New SBOM Scan After Remediation

After applying vulnerability fixes (e.g., upgrading Spring Boot), trigger a new SBOM scan to verify the fixes:

### Option 1: Using Lineaje CLI (veecli) - Recommended

1. **Download CLI** from Lineaje portal:
   - Go to https://app.veedna.com
   - Navigate to Integrations > Configure Scanners > Download CLI
   - Extract to `tools/lineaje/`

2. **Register CLI**:
   ```bash
   cd tools/lineaje
   ./veecli register --devicecode <code_from_portal>
   ```

3. **Trigger Scan**:
   ```bash
   ./veecli collect --inputfile .lineaje/input.json --output .lineaje/output
   ```

### Option 2: Using PowerShell Script

Run the provided trigger script:
```powershell
.\scripts\trigger-lineaje-scan.ps1 -Branch develop -ProjectName SDLC-POC
```

### Option 3: Via Lineaje Portal (Manual)

1. Login to https://app.veedna.com
2. Go to Integrations > Scan Remotely > Git
3. Find your integration (e.g., "Secure-SDLC-POC")
4. Click the three-dot menu and select "Rescan" or "Scan Now"

### Input Configuration

The `.lineaje/input.json` file configures the scan:
```json
{
  "project": "SDLC-POC",
  "version": "develop",
  "inputtype": "git",
  "inputs": [{
    "src_info": {
      "srcurl": "https://dev.azure.com/HOLMES-APPS/WINGS-POC/_git/SDLC-POC",
      "matchingref": "develop",
      "type": "git"
    }
  }],
  "repository_access_configs": [{
    "path": "https://dev.azure.com/HOLMES-APPS/WINGS-POC/_git/SDLC-POC",
    "type": "git",
    "user_name": "${AZURE_DEVOPS_USER}",
    "token": "${AZURE_DEVOPS_PAT}"
  }]
}
```

### Verifying Results

After triggering a scan:
1. Wait 5-15 minutes for scan completion
2. Check Lineaje portal: https://app.veedna.com/app/sbom360/projects
3. Look for updated "Last Scanned" timestamp
4. Compare vulnerability counts before/after remediation

## References

- Lineaje Documentation: https://docs.veedna.com
- Lineaje SBOM360 Portal: https://app.veedna.com
- Lineaje CLI Usage: https://docs.veedna.com/sca360-secure-deployment-for-restricted-environments/sca360-usage
- CycloneDX Specification: https://cyclonedx.org/specification/overview/
- NIST SSDF: https://csrc.nist.gov/publications/detail/sp/800-218/final
- Project Config: .lineaje/config.yaml
- Trigger Script: scripts/trigger-lineaje-scan.ps1
- Input Config: .lineaje/input.json
