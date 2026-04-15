# Security Defects Report - Order Management System

**Report Date**: 2026-03-17  
**Report Type**: AppSec & Production Readiness Review  
**Status**: NO-GO FOR PRODUCTION

---

## Executive Summary

This report identifies 17 security defects across the OMS application, with 3 Critical, 6 High, 5 Medium, and 3 Low severity issues. The application is NOT ready for production deployment until Critical and High severity issues are remediated.

---

## 1. Critical Severity Defects

### SEC-001: Hardcoded JWT Secret Default Value
| Field | Value |
|-------|-------|
| **Severity** | CRITICAL |
| **CVSS** | 9.1 |
| **Location** | `backend/auth-service/src/main/resources/application.yml:30` |
| **Location** | `backend/api-gateway/src/main/resources/application.yml:75` |
| **Description** | JWT secret has a hardcoded default value visible in configuration files. If JWT_SECRET environment variable is not set, the application uses a predictable default, allowing attackers to forge valid JWT tokens. |
| **Current Code** | `jwt.secret: ${JWT_SECRET:OMS_DEV_SECRET_KEY_MINIMUM_256_BITS_CHANGE_IN_PRODUCTION}` |
| **Remediation** | Remove the default value: `jwt.secret: ${JWT_SECRET}` and ensure the app fails to start without the env var. |
| **Test** | Verify application fails to start when JWT_SECRET is not provided. |

### SEC-002: H2 Console Enabled in Non-Test Profiles
| Field | Value |
|-------|-------|
| **Severity** | CRITICAL |
| **CVSS** | 9.0 |
| **Location** | `backend/auth-service/src/main/resources/application.yml:22-24` |
| **Description** | H2 database console is enabled by default, exposing the entire database through a web interface at `/h2-console`. This allows unauthorized database access and potential remote code execution. |
| **Current Code** | `spring.h2.console.enabled: true` |
| **Remediation** | Disable H2 console by default, only enable in `dev` profile. |
| **Test** | Verify `/h2-console` returns 404 in production profile. |

### SEC-003: Weak Password Policy
| Field | Value |
|-------|-------|
| **Severity** | CRITICAL |
| **CVSS** | 7.5 |
| **Location** | `backend/auth-service/src/main/java/com/oms/auth/dto/RegisterRequest.java:21-22` |
| **Description** | Password policy only requires 8 characters minimum with no complexity requirements. This makes brute-force and dictionary attacks significantly easier. |
| **Current Code** | `@Size(min = 8, message = "Password must be at least 8 characters")` |
| **Remediation** | Require minimum 12 characters with uppercase, lowercase, number, and special character. |
| **Test** | Verify passwords like "password123" are rejected. |

---

## 2. High Severity Defects

### SEC-004: PII (Email) Logged in Plain Text
| Field | Value |
|-------|-------|
| **Severity** | HIGH |
| **CVSS** | 6.5 |
| **Location** | `backend/auth-service/src/main/java/com/oms/auth/service/AuthService.java:29,49,55` |
| **Location** | `backend/auth-service/src/main/java/com/oms/auth/controller/AuthController.java:23,31` |
| **Description** | User email addresses are logged at INFO and WARN levels. This exposes PII in log files, violating GDPR/CCPA and creating data breach risk. |
| **Current Code** | `log.info("Registering user with email: {}", email);` |
| **Remediation** | Mask email addresses in logs: `t***@example.com` |
| **Test** | Verify logs do not contain full email addresses after login/register. |

### SEC-005: No Rate Limiting on Authentication Endpoints
| Field | Value |
|-------|-------|
| **Severity** | HIGH |
| **CVSS** | 7.5 |
| **Location** | `backend/api-gateway/` |
| **Description** | No rate limiting configured on `/api/auth/login` and `/api/auth/register` endpoints. This allows unlimited brute-force password attempts and account enumeration. |
| **Remediation** | Implement rate limiting using Spring Cloud Gateway RateLimiter (e.g., 10 requests/minute per IP for auth endpoints). |
| **Test** | Verify 429 response after exceeding rate limit threshold. |

### SEC-006: No HTTPS/TLS Enforcement
| Field | Value |
|-------|-------|
| **Severity** | HIGH |
| **CVSS** | 7.4 |
| **Location** | `backend/api-gateway/src/main/resources/application.yml` |
| **Description** | No HTTPS redirect or HSTS header configured. Traffic can be intercepted over HTTP, exposing credentials and tokens. |
| **Remediation** | Add HSTS header and configure HTTPS redirect in production. |
| **Test** | Verify HTTP requests redirect to HTTPS and HSTS header is present. |

### SEC-007: CSRF Protection Disabled Globally
| Field | Value |
|-------|-------|
| **Severity** | HIGH |
| **CVSS** | 6.8 |
| **Location** | `backend/auth-service/src/main/java/com/oms/auth/config/SecurityConfig.java:30` |
| **Description** | CSRF protection is completely disabled. While acceptable for pure API services with JWT, browser-based SPA clients are vulnerable to CSRF attacks. |
| **Current Code** | `.csrf(AbstractHttpConfigurer::disable)` |
| **Remediation** | Use cookie-based CSRF tokens for browser clients, exempt API-only endpoints. |
| **Test** | Verify state-changing requests without CSRF token are rejected from browser. |

### SEC-008: No Input Sanitization for XSS
| Field | Value |
|-------|-------|
| **Severity** | HIGH |
| **CVSS** | 6.1 |
| **Location** | Multiple DTOs (ProductRequest, RegisterRequest, etc.) |
| **Description** | Text fields like product name, description, and user name have no XSS sanitization. Malicious scripts could be stored and executed. |
| **Remediation** | Add input sanitization using OWASP Java HTML Sanitizer or encode output. |
| **Test** | Verify `<script>alert(1)</script>` in product name is sanitized/escaped. |

### SEC-009: Actuator Endpoints Exposed Without Authentication
| Field | Value |
|-------|-------|
| **Severity** | HIGH |
| **CVSS** | 6.5 |
| **Location** | All service `application.yml` files |
| **Description** | Spring Actuator endpoints (`/actuator/health`, `/actuator/info`) are exposed without authentication. While health is needed, detailed info could leak sensitive data. |
| **Current Code** | `management.endpoint.health.show-details: always` |
| **Remediation** | Change to `show-details: when_authorized` and secure actuator endpoints. |
| **Test** | Verify `/actuator/health` shows limited info without auth. |

---

## 3. Medium Severity Defects

### SEC-010: No Method-Level Authorization Annotations
| Field | Value |
|-------|-------|
| **Severity** | MEDIUM |
| **CVSS** | 5.3 |
| **Location** | All controller classes |
| **Description** | Authorization checks are done manually in code rather than using Spring Security annotations. This is error-prone and harder to audit. |
| **Remediation** | Add `@PreAuthorize("hasRole('ADMIN')")` annotations and enable `@EnableMethodSecurity`. |
| **Test** | Verify annotated endpoints reject non-admin users with 403. |

### SEC-011: Default PostgreSQL Credentials in Docker Compose
| Field | Value |
|-------|-------|
| **Severity** | MEDIUM |
| **CVSS** | 5.0 |
| **Location** | `docker-compose.yml` (all database services) |
| **Description** | All PostgreSQL databases use `postgres:postgres` as credentials. These defaults are well-known and could be exploited if containers are exposed. |
| **Remediation** | Use Docker secrets or environment files with strong, unique passwords per database. |
| **Test** | Verify databases reject connection with default credentials after fix. |

### SEC-012: No JWT Token Refresh Mechanism
| Field | Value |
|-------|-------|
| **Severity** | MEDIUM |
| **CVSS** | 5.0 |
| **Location** | `backend/common/src/main/java/com/oms/common/security/JwtUtil.java` |
| **Description** | Tokens have 24-hour expiration with no refresh mechanism. Users must re-authenticate daily, and compromised tokens remain valid for extended periods. |
| **Remediation** | Implement short-lived access tokens (15 min) with refresh tokens. |
| **Test** | Verify refresh endpoint issues new access token with valid refresh token. |

### SEC-013: Missing Distributed Tracing/Correlation IDs
| Field | Value |
|-------|-------|
| **Severity** | MEDIUM |
| **CVSS** | 4.3 |
| **Location** | All services |
| **Description** | No correlation IDs or distributed tracing configured. This makes debugging security incidents across microservices extremely difficult. |
| **Remediation** | Add Spring Cloud Sleuth or Micrometer Tracing for correlation IDs. |
| **Test** | Verify X-Correlation-ID header propagates through service calls. |

### SEC-014: Swagger UI Enabled Without Profile Restriction
| Field | Value |
|-------|-------|
| **Severity** | MEDIUM |
| **CVSS** | 4.0 |
| **Location** | `backend/auth-service/src/main/resources/application.yml:43-46` |
| **Description** | Swagger UI is enabled in all profiles including production. This exposes API documentation and testing interface to potential attackers. |
| **Remediation** | Disable Swagger UI in production profile. |
| **Test** | Verify `/swagger-ui.html` returns 404 in production profile. |

---

## 4. Low Severity Defects

### SEC-015: Missing Security Headers in Backend Services
| Field | Value |
|-------|-------|
| **Severity** | LOW |
| **CVSS** | 3.5 |
| **Location** | All backend services except API Gateway |
| **Description** | Security headers (X-Content-Type-Options, X-Frame-Options, etc.) are only configured in API Gateway, not in individual services. |
| **Remediation** | Add security headers to all services or ensure all traffic goes through gateway. |

### SEC-016: No Password Expiry/Rotation Policy
| Field | Value |
|-------|-------|
| **Severity** | LOW |
| **CVSS** | 3.0 |
| **Location** | `backend/auth-service/src/main/java/com/oms/auth/entity/User.java` |
| **Description** | No password expiry or rotation policy implemented. Compromised passwords remain valid indefinitely. |
| **Remediation** | Add `passwordChangedAt` field and enforce periodic password changes. |

### SEC-017: Debug Logging Level in Default Profile
| Field | Value |
|-------|-------|
| **Severity** | LOW |
| **CVSS** | 2.5 |
| **Location** | All service `application.yml` files |
| **Description** | DEBUG logging level is set by default, which may expose sensitive information and impact performance. |
| **Current Code** | `logging.level.com.oms: DEBUG` |
| **Remediation** | Set DEBUG only in dev profile, use INFO or WARN in production. |

---

## 5. Dependency Vulnerabilities

| Dependency | Version | CVE | Severity | Affected Component | Recommended Version |
|------------|---------|-----|----------|-------------------|-------------------|
| PostgreSQL JDBC | 42.x (managed) | CVE-2024-1597 | HIGH | SQL Injection in certain queries | 42.7.2+ |
| H2 Database | 2.x (managed) | CVE-2022-45868 | CRITICAL | RCE via JDBC URL | 2.2.224+ (dev only) |

**Note**: Spring Boot 3.2.3 manages these dependencies. Verify actual versions with `mvn dependency:tree`.

---

## 6. Remediation Priority

| Phase | Defects | Timeline | Effort |
|-------|---------|----------|--------|
| **Phase 1** | SEC-001, SEC-002, SEC-003 | Immediate (1-2 days) | Low |
| **Phase 2** | SEC-004 to SEC-009 | Week 1 | Medium |
| **Phase 3** | SEC-010 to SEC-014 | Week 2-3 | Medium |
| **Phase 4** | SEC-015 to SEC-017 | Post-launch | Low |

---

## 7. Go/No-Go Summary

### Current Status: **NO-GO FOR PRODUCTION**

**Blocking Issues:**
- SEC-001: Hardcoded JWT secret
- SEC-002: H2 console exposed
- SEC-005: No rate limiting
- SEC-006: No HTTPS enforcement

**Minimum Requirements for Production:**
1. Remove all hardcoded secrets
2. Disable H2 console in production
3. Implement rate limiting on auth endpoints
4. Configure HTTPS/TLS termination
5. Secure actuator endpoints

---

*Report generated by AppSec Review Process*
