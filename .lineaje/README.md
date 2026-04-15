# Lineaje SBOM360 Integration

This directory contains configuration for Lineaje SBOM360 vulnerability scanning.

## Quick Start - Triggering SBOM Scan from Droid

After making security remediation changes (e.g., upgrading dependencies), trigger a new SBOM scan:

### Method 1: Using Lineaje CLI (Recommended)

```bash
# First time setup (one-time)
# 1. Download CLI from: https://app.veedna.com > Integrations > Configure Scanners > Download CLI
# 2. Extract to: tools/lineaje/
# 3. Register CLI:
cd tools/lineaje
./veecli register --devicecode <code_from_portal>

# Trigger scan
./veecli collect --inputfile .lineaje/input.json --output .lineaje/output
```

### Method 2: Using PowerShell Script

```powershell
# Set credentials (or store in .factory/credentials/azure-devops.env)
$env:AZURE_DEVOPS_USER = "your-username"
$env:AZURE_DEVOPS_PAT = "your-pat-token"

# Run the trigger script
.\scripts\trigger-lineaje-scan.ps1
```

### Method 3: Manual via Portal

1. Login to https://app.veedna.com
2. Go to Projects > _git-SDLC-POC
3. Check "Last Scanned" date
4. If outdated, go to Integrations > Scan Remotely > Git
5. Find integration and trigger rescan

## Files

| File | Description |
|------|-------------|
| `config.yaml` | Main Lineaje configuration |
| `input.json` | CLI input configuration for SBOM generation |
| `README.md` | This file |

## Verification

After triggering a scan:
1. Wait 5-15 minutes for completion
2. Check https://app.veedna.com/app/sbom360/projects
3. Look for updated vulnerability counts
4. Compare before/after remediation

## Current Status

**Last Manual Fix Applied:** 2026-04-07
- Upgraded Spring Boot 3.2.3 → 3.4.0
- Upgraded Spring Cloud 2023.0.0 → 2024.0.0
- Commit: 19c8662

**Before Fix:** 88 vulnerabilities (C 4, H 40, M 32, L 12)
**After Fix:** Pending rescan verification

## Portal Credentials

Stored in: `.factory/credentials/lineaje.env`

```
LINEAJE_URL=https://app.veedna.com/auth/signin
LINEAJE_EMAIL=tenant-admin@flobo.fr.nf
```

## References

- [Lineaje Documentation](https://docs.veedna.com)
- [CLI Usage Guide](https://docs.veedna.com/sca360-secure-deployment-for-restricted-environments/sca360-usage)
- [Skill Documentation](.factory/skills/lineaje-security/SKILL.md)
