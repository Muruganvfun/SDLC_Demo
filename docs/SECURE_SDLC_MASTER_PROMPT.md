# Secure SDLC Master Prompt - Order Management System (OMS)
## Complete Blueprint for Building Enterprise Microservices with DevSecOps

**Created:** 2026-04-02  
**Version:** 1.0  
**Project:** SDLC_Demo (Order Management System)  
**Purpose:** Comprehensive prompt to recreate this application with all components, security integrations, and SDLC processes

---

## PROMPT: Build a Complete Secure SDLC Enterprise Application

### Use this prompt with Factory.AI Droid or any AI coding assistant:

```
Build me a complete enterprise-grade Order Management System (OMS) with full Secure SDLC implementation, following this comprehensive specification:

## 1. APPLICATION OVERVIEW

Create a microservices-based e-commerce Order Management System with:

### Architecture
- **Pattern**: Microservices with API Gateway
- **Backend**: 8 Spring Boot microservices (Java 17)
- **Frontend**: React Native Web (Expo 50+)
- **Database**: PostgreSQL (per-service database pattern)
- **Communication**: REST/HTTP via API Gateway
- **Authentication**: JWT-based with centralized validation
- **Containerization**: Docker + Docker Compose

### Microservices to Build

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| api-gateway | 8080 | None | Request routing, JWT validation, load balancing |
| auth-service | 8081 | auth_db | User registration, login, JWT issuance |
| catalog-service | 8082 | catalog_db | Product management, product queries |
| order-service | 8083 | order_db | Order lifecycle, order orchestration |
| inventory-service | 8084 | inventory_db | Stock management, reservations |
| payment-service | 8085 | payment_db | Payment processing (mock) |
| shipping-service | 8086 | shipping_db | Shipment creation, tracking (mock) |
| notification-service | 8087 | None | Event notifications (mock, logging) |

### Service Dependencies Flow
```
Frontend (React Native Web)
    ↓
API Gateway (Spring Cloud Gateway)
    ↓
├── auth-service (standalone)
├── catalog-service → inventory-service
├── order-service → catalog-service
│                 → inventory-service (reserve/release)
│                 → payment-service
│                 → shipping-service
│                 → notification-service
├── inventory-service (standalone)
├── payment-service (standalone)
├── shipping-service (standalone)
└── notification-service (standalone)
```

## 2. BACKEND TECHNICAL SPECIFICATIONS

### Technology Stack
- Java 17+
- Spring Boot 3.2.x
- Spring Cloud Gateway 4.1.x
- Spring Security 6.x
- Spring Data JPA / Hibernate
- PostgreSQL 15.x (production) / H2 (development)
- Maven 3.9.x (multi-module project)
- JWT Library: jjwt 0.12.x
- Testing: JUnit 5, Mockito, TestContainers

### Maven Project Structure
```
backend/
├── pom.xml (parent POM)
├── common/
│   ├── pom.xml
│   └── src/main/java/com/oms/common/
│       ├── dto/
│       ├── exception/
│       └── security/
├── api-gateway/
├── auth-service/
├── catalog-service/
├── order-service/
├── inventory-service/
├── payment-service/
├── shipping-service/
└── notification-service/
```

### Each Service Structure
```
service-name/
├── pom.xml
├── Dockerfile
└── src/main/
    ├── java/com/oms/servicename/
    │   ├── ServiceNameApplication.java
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   ├── dto/
    │   └── config/
    └── resources/
        ├── application.yml
        └── application-docker.yml
```

### API Endpoints

**Auth Service (/api/auth):**
- POST /register - User registration
- POST /login - User authentication
- GET /validate - Token validation
- GET /me - Current user profile

**Catalog Service (/api/products):**
- GET / - List products (paginated)
- GET /{id} - Get product details
- POST / - Create product (Admin)
- PUT /{id} - Update product (Admin)
- DELETE /{id} - Delete product (Admin)

**Order Service (/api/orders):**
- POST / - Create order
- GET / - List user's orders
- GET /{id} - Get order details
- POST /{id}/cancel - Cancel pending order
- POST /{id}/pay - Process payment

**Inventory Service (/api/inventory):**
- GET / - List all inventory
- GET /{productId} - Get product inventory
- POST /reserve - Reserve stock
- POST /release - Release stock
- PUT /{productId} - Update inventory (Admin)

**Payment Service (/api/payments):**
- POST / - Process payment (internal)
- GET /order/{orderId} - Get payment details

**Shipping Service (/api/shipments):**
- POST / - Create shipment (internal)
- GET /order/{orderId} - Get shipment details
- PUT /{id}/status - Update status (Admin)

### Security Implementation
1. JWT tokens with HS256 algorithm
2. BCrypt password hashing
3. Role-based access (CUSTOMER, ADMIN)
4. Gateway-level JWT validation
5. Input validation (@Valid, Bean Validation)
6. No sensitive data in logs

### Health Endpoints
All services expose:
- GET /actuator/health
- GET /actuator/health/liveness
- GET /actuator/health/readiness

## 3. FRONTEND TECHNICAL SPECIFICATIONS

### Technology Stack
- React Native 0.73.x
- React Native Web 0.19.x
- Expo 50.x
- TypeScript
- Axios for HTTP
- React Navigation 6.x
- React Context for state

### Screens to Build
1. **LoginScreen** - Email/password login
2. **RegisterScreen** - User registration
3. **ProductListScreen** - Browse products, add to cart
4. **CreateOrderScreen** - Checkout flow
5. **OrderListScreen** - View order history
6. **OrderDetailScreen** - Order details, cancel/pay actions

### Frontend Structure
```
frontend/
├── package.json
├── App.tsx
├── Dockerfile
└── src/
    ├── screens/
    ├── navigation/
    ├── context/
    ├── services/
    │   └── api.ts
    ├── theme/
    └── __tests__/
```

### API Integration
- Base URL configurable via environment
- JWT stored in React Context
- Axios interceptors for auth headers
- Error handling for 401/403

## 4. DOCKER CONTAINERIZATION

### Docker Compose Services (14+ containers)
- 6 PostgreSQL databases
- 8 microservices
- 1 frontend (nginx)

### docker-compose.yml Structure
- Each service depends on its database (healthcheck)
- Services share oms-network
- API Gateway depends on all services
- Environment-based configuration
- Named volumes for data persistence

### Dockerfile Template (Backend)
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-alpine
COPY --from=build /app/target/*.jar app.jar
EXPOSE 808X
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Dockerfile (Frontend)
```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build:web

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

## 5. CI/CD PIPELINE (AZURE DEVOPS)

### Pipeline Files Structure
```
.azure/
├── pipelines/
│   ├── ci-pipeline.yml (or azure-pipelines.yml)
│   ├── cd-pipeline.yml
│   └── lineaje-security-pipeline.yml
└── k8s/
    ├── namespace.yml
    ├── configmap.yml
    ├── secrets.yml
    └── *-deployment.yml
```

### CI Pipeline Stages
1. **Build Stage**
   - Backend: Maven build, unit tests, JaCoCo coverage
   - Frontend: npm install, lint, test, build

2. **Code Quality Stage**
   - OWASP Dependency Check
   - SonarQube analysis (optional)
   - Code coverage gates (70%+)

3. **Security Scanning**
   - SBOM generation (CycloneDX)
   - Lineaje SCA360 vulnerability scan
   - Container image scanning

### CD Pipeline Stages
1. **Dev Environment** - Auto-deploy on develop branch
2. **QA Environment** - Deploy with integration tests
3. **Production** - Manual approval, blue-green deployment

### Pipeline Triggers
- CI: On push to main, develop, feature/*, release/*, hotfix/*
- CD: Triggered by CI pipeline completion or manual

## 6. SECURITY INTEGRATION (LINEAJE)

### Lineaje Configuration (.lineaje/config.yaml)
```yaml
project:
  name: order-management-system
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
  ecosystems:
    - java (maven)
    - javascript (npm)

sbom360:
  enabled: true
  fix_strategy: "compatible"
  auto_create_pr: false
  require_approval: true

containers:
  enabled: true
  self_healing: true

compliance:
  enabled: true
  frameworks:
    - "EO_14028"
    - "NIST_SSDF"
    - "CRA"
  generate_vex: true
```

### Security Pipeline Stages
1. SBOM Generation (CycloneDX)
2. Lineaje SCA360 Vulnerability Scan
3. SBOM360 Auto-Remediation Plans
4. Container Security Scan
5. Compliance Validation (EO 14028, NIST)
6. Security Gate Decision

### Factory.AI Lineaje Droid
Create `.factory/droids/lineaje-remediation.yaml` for AI-assisted vulnerability remediation.

## 7. DOCUMENTATION REQUIREMENTS

### Required Documentation Files
```
docs/
├── 01-requirements.md (Functional & Non-functional)
├── 02-backlog.md (Epics, User Stories, Tasks)
├── 03-architecture.md (System design, diagrams)
├── 04-api-spec.md (OpenAPI-style documentation)
├── 05-test-strategy.md (Test pyramid, coverage)
├── 06-runbook.md (Deployment & operations)
├── 07-demo-script.md (Demo scenarios)
├── 08-git-workflow.md (Branch strategy)
└── 09-azure-devops-setup.md (CI/CD configuration)
```

### Git Workflow
- Main branch: Production-ready
- Develop branch: Integration
- Feature branches: feature/OMS-XXX-description
- Hotfix branches: hotfix/vX.X.X-description
- Branch protection with PR reviews
- Commit convention: type(scope): message

## 8. TESTING STRATEGY

### Test Coverage Targets
- Unit Tests: 70%+ (Service layer 80%+)
- Integration Tests: API endpoints
- E2E Tests: Critical user flows

### Testing Tools
- Backend: JUnit 5, Mockito, AssertJ, JaCoCo
- Frontend: Jest, React Testing Library

### Test Categories
1. Unit Tests (isolated, mocked)
2. Integration Tests (Spring Boot Test, H2)
3. Contract Tests (API validation)
4. Security Tests (auth, authorization)
5. Performance Tests (optional, k6)

## 9. DEPLOYMENT ENVIRONMENTS

### Local Development
- Docker Compose: `docker-compose up -d`
- Frontend: http://localhost:3000
- API: http://localhost:8080

### Azure VM Production
- Docker Compose with external network
- Nginx reverse proxy
- SSL/TLS termination
- API Gateway on port 8090

### Kubernetes (Optional)
- Namespace: oms-{env}
- ConfigMaps for environment config
- Secrets for sensitive data
- Ingress for external access

## 10. STEPS TO RECREATE THIS APPLICATION

### Phase 1: Project Setup (Day 1)
1. Create Maven multi-module backend project
2. Create React Native Web frontend project
3. Initialize Git repository with branching strategy
4. Setup Azure DevOps project and repos

### Phase 2: Core Services (Days 2-5)
1. Build common module (DTOs, exceptions, JWT utils)
2. Implement auth-service with JWT
3. Implement catalog-service with products
4. Implement inventory-service with stock management
5. Build API Gateway with routing

### Phase 3: Business Services (Days 6-8)
1. Implement order-service with orchestration
2. Implement payment-service (mock)
3. Implement shipping-service (mock)
4. Implement notification-service (logging)
5. Add service-to-service communication

### Phase 4: Frontend (Days 9-11)
1. Setup Expo project with navigation
2. Build authentication screens
3. Build product catalog screens
4. Build order management screens
5. Integrate with backend APIs

### Phase 5: DevSecOps (Days 12-14)
1. Write Dockerfiles for all services
2. Create docker-compose.yml
3. Setup Azure DevOps CI pipeline
4. Setup Azure DevOps CD pipeline
5. Integrate Lineaje security scanning
6. Configure branch protection

### Phase 6: Testing & Documentation (Days 15-17)
1. Write unit tests (70%+ coverage)
2. Write integration tests for APIs
3. Create all documentation files
4. Setup test automation in pipeline
5. Configure quality gates

### Phase 7: Deployment (Days 18-20)
1. Deploy to development environment
2. Run integration tests
3. Deploy to QA with approvals
4. Production deployment (blue-green)
5. Verify all services operational

## 11. KEY INTEGRATION POINTS

### Factory.AI Integration
- Custom Droids for code generation
- Lineaje remediation droid
- Code review assistance
- Documentation generation

### Azure DevOps Integration
- Repos for source control
- Pipelines for CI/CD
- Boards for work tracking
- Artifacts for packages

### Lineaje SBOM360 Integration
- Pipeline stage for scanning
- SBOM generation and upload
- Vulnerability reports
- Auto-fix plan generation
- Compliance validation

### Docker/Container Integration
- Multi-stage builds
- Image scanning
- Registry push (ACR/Docker Hub)
- Compose orchestration

## 12. DEMO CREDENTIALS

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@oms.com | admin123 |
| Customer | (register) | password123 |

## 13. QUICK REFERENCE COMMANDS

### Local Development
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f api-gateway

# Run backend tests
cd backend && mvn test

# Run frontend
cd frontend && npm run web

# Build frontend
npm run build:web
```

### Deployment
```bash
# Build backend
cd backend && mvn clean package -DskipTests

# Deploy to VM
ssh user@vm 'cd /app && docker-compose pull && docker-compose up -d'

# Check health
curl http://localhost:8080/actuator/health
```

---

## IMPORTANT NOTES

1. **Security First**: Always validate JWT, hash passwords, sanitize inputs
2. **Database per Service**: Each service owns its data, no sharing
3. **Stateless Services**: JWT enables horizontal scaling
4. **Health Checks**: Essential for container orchestration
5. **Logging**: Structured JSON logs for observability
6. **Testing**: Minimum 70% coverage before deployment
7. **SBOM**: Generate for every build for supply chain security
8. **Documentation**: Keep in sync with code changes

---

*This prompt captures the complete Secure SDLC implementation of the Order Management System, including architecture, code structure, CI/CD pipelines, security integrations, and deployment processes. Use it as a reference to build similar enterprise applications with Factory.AI assistance.*
```

---

## APPENDIX: Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        FRONTEND (React Native Web)                  │
│                        http://localhost:3000                        │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       API GATEWAY (Port 8080)                       │
│                 JWT Validation, Routing, Load Balancing             │
└─────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────┬───────────┬───┴───┬───────────┬───────────┬─────┐
        ▼           ▼           ▼       ▼           ▼           ▼     ▼
   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
   │  Auth   │ │ Catalog │ │  Order  │ │Inventory│ │ Payment │ │Shipping │ │Notif.   │
   │ :8081   │ │ :8082   │ │ :8083   │ │ :8084   │ │ :8085   │ │ :8086   │ │:8087    │
   └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └─────────┘
        │           │           │           │           │           │
        ▼           ▼           ▼           ▼           ▼           ▼
   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
   │ AuthDB  │ │CatalogDB│ │ OrderDB │ │ InvDB   │ │PaymentDB│ │ShipDB   │
   │Postgres │ │Postgres │ │Postgres │ │Postgres │ │Postgres │ │Postgres │
   └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘
```

## APPENDIX: CI/CD Pipeline Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         AZURE DEVOPS PIPELINE FLOW                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐  │
│  │    BUILD    │───▶│   QUALITY   │───▶│  SECURITY   │───▶│   DEPLOY    │  │
│  │             │    │             │    │             │    │             │  │
│  │ • Compile   │    │ • Unit Test │    │ • SBOM Gen  │    │ • Dev       │  │
│  │ • Package   │    │ • Coverage  │    │ • SCA360    │    │ • QA        │  │
│  │ • Artifacts │    │ • Lint      │    │ • SBOM360   │    │ • Production│  │
│  │             │    │ • OWASP     │    │ • Container │    │             │  │
│  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘  │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    SECURITY GATE (Lineaje)                           │   │
│  │  Critical: BLOCK │ High: WARN (>5) │ Medium: INFO │ Low: PASS       │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## APPENDIX: Session History Summary

| Session | Date | Topic | Outcome |
|---------|------|-------|---------|
| 1 | Mar 27-29 | VM Deployment | OMS deployed to Azure Ubuntu VM |
| 2 | Mar 29 | Azure DevOps Agent | Self-hosted agent setup documented |
| 3 | Mar 27 | Pay-I Integration | AI cost tracking approach defined |
| 4 | Mar 26 | Deployment Planning | Architecture and steps defined |
| 5 | Mar 26 | Initial Setup | Repository and structure created |

---

**Generated by Factory.AI Droid**  
**For Project:** SDLC_Demo (Order Management System)  
**Organization:** HOLMES-APPS / WINGS-POC
