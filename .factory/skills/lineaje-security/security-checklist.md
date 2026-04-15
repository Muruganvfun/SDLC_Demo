# Security Scan Checklist

## Pre-Scan Verification

- [ ] Lineaje config file exists (`.lineaje/config.yaml`)
- [ ] Project is registered in Lineaje SBOM360 portal
- [ ] API credentials are configured (if using API)
- [ ] All dependencies are resolved (build succeeds)

## SBOM Generation

- [ ] Backend SBOM generated (`backend/target/bom.json`)
- [ ] Frontend SBOM generated (`frontend/sbom.json`)
- [ ] Container images scanned (if applicable)
- [ ] SBOM format is CycloneDX 1.5+

## Vulnerability Scan

- [ ] Full dependency tree scanned (including transitive)
- [ ] All ecosystems covered (Maven, npm)
- [ ] Results exported to JSON for tracking
- [ ] Vulnerabilities categorized by severity

## Security Gate Evaluation

| Check | Criteria | Status |
|-------|----------|--------|
| Critical CVEs | Count = 0 | |
| High CVEs | Count <= 5 | |
| Known exploited | None in CISA KEV | |
| License compliance | No copyleft in prod | |
| SBOM completeness | > 95% components identified | |

## Remediation Tracking

- [ ] Critical vulnerabilities have Jira tickets
- [ ] High vulnerabilities reviewed by security team
- [ ] Upgrade PRs created for fixable issues
- [ ] Risk acceptance documented for unfixable issues

## Compliance Validation

- [ ] EO 14028 requirements met
- [ ] NIST SSDF controls satisfied
- [ ] VEX document generated (if needed)
- [ ] Attestation signed (for federal deployments)

## Post-Scan Actions

- [ ] Report uploaded to Lineaje portal
- [ ] Stakeholders notified of results
- [ ] Pipeline status updated (PASS/FAIL)
- [ ] Metrics recorded for tracking
