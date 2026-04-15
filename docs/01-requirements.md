# Order Management System - Requirements Specification

## Document Information

| Item | Details |
|------|---------|
| **Project** | Order Management System (OMS) |
| **Version** | 2.0 |
| **Last Updated** | April 2026 |
| **Source** | [Confluence - Secure SDLC Pipeline](https://wipro-team-ky3gtu9s.atlassian.net/wiki/spaces/~7120207ab844a562c843b5880e9c9633e26746/pages/917505/Secure+SDLC+Pipeline) |

---

## 1. Project Overview

The Order Management System (OMS) is a microservices-based e-commerce platform that handles the complete order lifecycle from product browsing to delivery tracking. Built with Java Spring Boot backend and React Native Web frontend, integrated with **Factory.AI** for autonomous development and **Lineaje SBOM360** for software supply chain security.

### 1.1 Secure SDLC Pipeline Integration

| Stage | Description | Key Tools |
|-------|-------------|-----------|
| PLAN | Requirements & Design | Jira, Linear, Slack + Factory.AI Droids |
| CODE | Development | Factory.AI (Code Gen, Refactor, Test Gen) |
| BUILD | Continuous Integration | CI/CD + Lineaje SBOM Generation |
| TEST | Quality Assurance | Automated Testing + Factory.AI |
| SECURITY GATE | Compliance & Approval | Lineaje SBOM360 + BOMbots |
| DEPLOY | Release | Kubernetes, Docker + Security Attestation |
| MONITOR | Operations | APM + Lineaje Continuous Monitoring |

---

## 2. Functional Requirements

### 2.1 Authentication & Authorization (FR-AUTH)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-AUTH-001 | Users shall be able to register with email and password | High |
| FR-AUTH-002 | Users shall be able to login and receive JWT token | High |
| FR-AUTH-003 | System shall support role-based access (CUSTOMER, ADMIN) | High |
| FR-AUTH-004 | JWT tokens shall expire after configurable duration | Medium |
| FR-AUTH-005 | System shall validate tokens on protected endpoints | High |

### 2.2 Product Catalog (FR-CAT)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-CAT-001 | Users shall view list of available products | High |
| FR-CAT-002 | Users shall view product details (name, description, price) | High |
| FR-CAT-003 | Admins shall be able to create/update/delete products | Medium |
| FR-CAT-004 | Products shall display current availability status | High |

### 2.3 Inventory Management (FR-INV)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-INV-001 | System shall track stock quantity per product | High |
| FR-INV-002 | System shall reserve stock when order is placed | High |
| FR-INV-003 | System shall release stock when order is cancelled | High |
| FR-INV-004 | System shall prevent overselling (stock < 0) | High |
| FR-INV-005 | Admins shall be able to adjust inventory levels | Medium |

### 2.4 Order Management (FR-ORD)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-ORD-001 | Customers shall create orders with selected products | High |
| FR-ORD-002 | System shall calculate order total automatically | High |
| FR-ORD-003 | Orders shall have lifecycle states (PENDING, CONFIRMED, PAID, SHIPPED, DELIVERED, CANCELLED) | High |
| FR-ORD-004 | Customers shall view their order history | High |
| FR-ORD-005 | Customers shall view order details and status | High |
| FR-ORD-006 | Customers shall be able to cancel pending orders | Medium |

### 2.5 Payment Processing (FR-PAY)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-PAY-001 | System shall process payments for confirmed orders | High |
| FR-PAY-002 | System shall support mock payment provider | High |
| FR-PAY-003 | System shall handle payment success/failure scenarios | High |
| FR-PAY-004 | System shall record payment transaction details | Medium |

### 2.6 Shipping (FR-SHIP)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-SHIP-001 | System shall create shipments for paid orders | High |
| FR-SHIP-002 | System shall generate tracking numbers | High |
| FR-SHIP-003 | System shall update shipment status | Medium |
| FR-SHIP-004 | Customers shall view shipping status | High |

### 2.7 Notifications (FR-NOT)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-NOT-001 | System shall send order confirmation notifications | Low |
| FR-NOT-002 | System shall send shipping update notifications | Low |
| FR-NOT-003 | Notifications shall be logged for demo purposes | Low |

---

## 3. Non-Functional Requirements

### 3.1 Performance (NFR-PERF)

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-PERF-001 | API response time | < 500ms (p95) |
| NFR-PERF-002 | Concurrent users supported | 100 (demo) |
| NFR-PERF-003 | Database query timeout | < 5 seconds |

### 3.2 Security (NFR-SEC)

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-SEC-001 | All API endpoints (except public) shall require authentication | 100% |
| NFR-SEC-002 | Passwords shall be hashed using BCrypt | Required |
| NFR-SEC-003 | Service-to-service calls shall use internal JWT validation | Required |
| NFR-SEC-004 | Input validation on all endpoints | Required |
| NFR-SEC-005 | No sensitive data in logs | Required |
| NFR-SEC-006 | SBOM generation for all builds | Required |
| NFR-SEC-007 | Vulnerability scanning via Lineaje | Required |

### 3.3 Security Controls by Pipeline Stage

| Stage | Security Control | Tool | Automation Level |
|-------|-----------------|------|------------------|
| PLAN | Threat Modeling | Lineaje | Manual + AI-assisted |
| PLAN | Third-party Risk | Lineaje | Automated |
| CODE | SAST | Lineaje | Automated |
| CODE | Secrets Detection | Lineaje | Automated |
| CODE | Code Review | Factory.AI | Automated |
| BUILD | SBOM Generation | Lineaje | Automated |
| BUILD | Dependency Scan | Lineaje | Automated |
| BUILD | Container Scan | Lineaje | Automated |
| BUILD | License Check | Lineaje | Automated |
| TEST | DAST | Lineaje | Automated |
| TEST | API Security | Lineaje | Automated |
| GATE | CVE Validation | Lineaje | Automated |
| GATE | Policy Enforcement | Lineaje | Automated |
| GATE | Auto-Remediation | Lineaje BOMbots | Automated |
| DEPLOY | Runtime Security | Lineaje | Automated |
| MONITOR | CVE Monitoring | Lineaje | Continuous |
| MONITOR | Incident Response | Factory.AI | Automated |

### 3.4 Compliance Mapping

| Regulation | Requirement | How Addressed |
|------------|-------------|---------------|
| NIST SP 800-218 | Secure SDLC | Full pipeline coverage |
| EO 14028 | SBOM requirement | Lineaje SBOM generation |
| FedRAMP | Security controls | Automated security gates |
| SOC 2 | Change management | Audit trail, approvals |
| PCI DSS | Code review | Factory.AI automated review |
| HIPAA | Access controls | Role-based pipeline access |

### 3.5 Reliability (NFR-REL)

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-REL-001 | Health check endpoints per service | Required |
| NFR-REL-002 | Graceful error handling | Required |
| NFR-REL-003 | Structured logging (JSON format) | Required |

### 3.6 Maintainability (NFR-MAIN)

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-MAIN-001 | Unit test coverage | > 70% |
| NFR-MAIN-002 | Integration test coverage for APIs | > 80% |
| NFR-MAIN-003 | Code follows standard conventions | Required |
| NFR-MAIN-004 | API documentation (OpenAPI) | Required |

### 3.7 Deployment (NFR-DEP)

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-DEP-001 | Docker containerization | Required |
| NFR-DEP-002 | Docker Compose for local development | Required |
| NFR-DEP-003 | Support embedded Tomcat (dev mode) | Required |
| NFR-DEP-004 | Support external Tomcat (WAR deployment) | Required |
| NFR-DEP-005 | Environment-based configuration | Required |
| NFR-DEP-006 | Azure Ubuntu VM deployment | Required |
| NFR-DEP-007 | Kubernetes-ready manifests | Optional |

---

## 4. System Constraints

### 4.1 Technical Constraints

- **Backend**: Java 17+, Spring Boot 3.x, Spring Cloud Gateway
- **Frontend**: React Native Web with Expo
- **Database**: PostgreSQL (production), H2 (development/demo)
- **Runtime**: Apache Tomcat (embedded or external)
- **Container**: Docker, Docker Compose
- **CI/CD**: Azure DevOps
- **Security**: Lineaje SBOM360

### 4.2 Business Constraints

- System is for demonstration/training purposes
- No real payment processing (mock only)
- No real shipping integration (mock only)
- Single-region deployment

---

## 5. Factory.AI Platform Capabilities

### 5.1 Autonomous Development Features

| Capability | Description |
|------------|-------------|
| Autonomous code generation | From requirements to working code |
| Multi-file refactoring | Across entire codebase |
| Automated unit test generation | JUnit 5 with Mockito |
| PR creation | With detailed descriptions |
| Code review | With actionable feedback |
| Incident response | Via Slack integration |
| Self-healing CI/CD pipelines | Automatic fix generation |

### 5.2 Lineaje Security Capabilities

| Capability | Description |
|------------|-------------|
| Deep SBOM generation | CycloneDX/SPDX formats |
| Vulnerability detection | CVE scanning |
| Agentic auto-remediation | BOMbots |
| Self-healing containers | Automatic patching |
| License compliance | Validation |
| Federal compliance | NIST, EO 14028, FedRAMP |

---

## 6. Assumptions

1. Users have modern web browsers (Chrome, Firefox, Safari, Edge)
2. Docker is available for local development
3. Java 17+ JDK is installed for non-Docker development
4. Network connectivity between services in same Docker network
5. Demo data will be seeded on startup
6. Azure DevOps organization is available for CI/CD
7. Lineaje API credentials are configured

---

## 7. Dependencies

| Dependency | Purpose | Version |
|------------|---------|---------|
| Spring Boot | Application framework | 3.2.x |
| Spring Cloud Gateway | API Gateway | 4.1.x |
| Spring Security | Authentication/Authorization | 6.x |
| Spring Data JPA | Database access | 3.2.x |
| PostgreSQL Driver | Database connectivity | 42.x |
| H2 Database | In-memory testing | 2.x |
| JWT (jjwt) | Token handling | 0.12.x |
| Expo | React Native Web build | 50.x |
| React Native Web | Web rendering | 0.19.x |
| CycloneDX | SBOM generation | 2.7.x |
| Lineaje SDK | Security scanning | Latest |

---

## 8. Metrics & KPIs

### 8.1 Security Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Mean Time to Detect (MTTD) | < 1 hour | Time from CVE publish to detection |
| Mean Time to Remediate (MTTR) | < 24 hours | Time from detection to fix deployed |
| Critical CVE Escape Rate | 0% | Critical CVEs reaching production |
| SBOM Coverage | 100% | % of deployments with complete SBOM |
| Security Gate Pass Rate | 95% | % of builds passing security gate |
| Auto-Remediation Rate | 80% | % of vulnerabilities auto-fixed by BOMbots |

### 8.2 Development Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Developer Productivity | +40% | Code output per developer |
| PR Review Time | < 2 hours | Time from PR to review complete |
| Build Success Rate | 95% | % of builds succeeding |
| Test Coverage | 80% | Code coverage percentage |
| Deployment Frequency | Daily | Deployments per day |
| Lead Time | < 1 week | Commit to production time |

### 8.3 Cost Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Maintenance Cost Reduction | 40% | YoY maintenance spend |
| Tool Consolidation | 5 → 2 | Number of SDLC tools |
| Incident Cost Reduction | 50% | Cost per production incident |
| Compliance Audit Time | -70% | Time spent on compliance audits |

---

## 9. Glossary

| Term | Definition |
|------|------------|
| OMS | Order Management System |
| JWT | JSON Web Token |
| API Gateway | Entry point for all client requests |
| Microservice | Independent deployable service unit |
| WAR | Web Application Archive (Java deployment format) |
| SBOM | Software Bill of Materials - inventory of all software components |
| CVE | Common Vulnerabilities and Exposures |
| BOMbot | Lineaje's agentic AI for automated vulnerability remediation |
| Droid | Factory.AI's autonomous AI agent for development tasks |
| SAST | Static Application Security Testing |
| DAST | Dynamic Application Security Testing |
| SCA | Software Composition Analysis |
| MTTR | Mean Time to Remediate |
| MTTD | Mean Time to Detect |

---

**Document Version:** 2.0  
**Source:** Confluence - Secure SDLC Pipeline  
**Author:** Factory.AI + Lineaje Integration Team
