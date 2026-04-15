# Factory.AI Droid Prompt: Build Complete Enterprise SDLC Application

## Use this prompt to request Factory.AI Droid to design and develop a complete enterprise application with Secure SDLC

---

## THE PROMPT

```
Build me a complete enterprise-grade Order Management System (OMS) from scratch with full Secure SDLC implementation. This should be a production-ready microservices application that demonstrates modern DevSecOps practices.

## PROJECT REQUIREMENTS

### 1. Business Requirements

Build an e-commerce Order Management System that handles:
- User registration and authentication
- Product catalog browsing
- Shopping cart and order creation
- Inventory management with stock reservations
- Payment processing (mock implementation)
- Shipment tracking (mock implementation)
- Order status notifications

Target Users:
- Customers: Browse products, place orders, track shipments
- Admins: Manage products, inventory, view all orders

### 2. Technical Architecture

Design a microservices architecture with:

**Backend Services (Java 17 + Spring Boot 3.x):**
- API Gateway - Central entry point, JWT validation, request routing
- Auth Service - User registration, login, JWT token management
- Catalog Service - Product CRUD operations
- Order Service - Order lifecycle management, orchestrates other services
- Inventory Service - Stock management, reservations
- Payment Service - Payment processing (mock)
- Shipping Service - Shipment creation and tracking (mock)
- Notification Service - Event notifications (logging for demo)

**Frontend (React Native Web + Expo):**
- Cross-platform web application
- Responsive design
- JWT-based authentication
- Product browsing and ordering flows

**Database:**
- PostgreSQL with database-per-service pattern
- Each service owns its data
- H2 for local development/testing

**Containerization:**
- Docker containers for all services
- Docker Compose for local development
- Production-ready Dockerfiles with multi-stage builds

### 3. API Design

Design RESTful APIs following these patterns:

Auth Service:
- POST /api/auth/register - Register new user
- POST /api/auth/login - Authenticate and get JWT
- GET /api/auth/me - Get current user profile
- GET /api/auth/validate - Validate JWT token

Catalog Service:
- GET /api/products - List products (paginated)
- GET /api/products/{id} - Get product details
- POST /api/products - Create product (Admin)
- PUT /api/products/{id} - Update product (Admin)
- DELETE /api/products/{id} - Delete product (Admin)

Order Service:
- POST /api/orders - Create new order
- GET /api/orders - List user's orders
- GET /api/orders/{id} - Get order details
- POST /api/orders/{id}/cancel - Cancel order
- POST /api/orders/{id}/pay - Process payment

Inventory Service:
- GET /api/inventory - List inventory
- GET /api/inventory/{productId} - Get stock level
- POST /api/inventory/reserve - Reserve stock
- POST /api/inventory/release - Release reserved stock
- PUT /api/inventory/{productId} - Update stock (Admin)

Payment Service:
- POST /api/payments - Process payment
- GET /api/payments/order/{orderId} - Get payment status

Shipping Service:
- POST /api/shipments - Create shipment
- GET /api/shipments/order/{orderId} - Get shipment details
- PUT /api/shipments/{id}/status - Update status

### 4. Security Requirements

Implement these security controls:
- JWT-based authentication with HS256 algorithm
- BCrypt password hashing
- Role-based access control (CUSTOMER, ADMIN roles)
- Gateway-level token validation
- Input validation on all endpoints
- No sensitive data in logs
- CORS configuration for frontend

### 5. CI/CD Pipeline (Azure DevOps)

Create CI/CD pipelines with:

CI Pipeline:
- Trigger on push to main, develop, feature/*, release/*
- Build stage: Maven build, npm build
- Test stage: Unit tests with 70%+ coverage
- Quality stage: Code analysis, OWASP dependency check
- Security stage: SBOM generation, vulnerability scanning
- Artifact publishing

CD Pipeline:
- Dev environment: Auto-deploy on develop branch
- QA environment: Manual trigger with integration tests
- Production: Manual approval required

### 6. Security Integration (Lineaje)

Integrate Lineaje SBOM360 for software supply chain security:
- SBOM generation in CycloneDX format
- Vulnerability scanning with SCA360
- Container image scanning
- Compliance validation (EO 14028, NIST SSDF)
- Security gates in pipeline

### 7. Project Structure

Create this folder structure:

```
project-root/
├── backend/
│   ├── pom.xml (parent)
│   ├── common/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── catalog-service/
│   ├── order-service/
│   ├── inventory-service/
│   ├── payment-service/
│   ├── shipping-service/
│   └── notification-service/
├── frontend/
│   ├── package.json
│   ├── App.tsx
│   └── src/
├── docs/
│   ├── requirements.md
│   ├── architecture.md
│   ├── api-spec.md
│   ├── test-strategy.md
│   └── runbook.md
├── .azure/
│   └── pipelines/
├── .lineaje/
│   └── config.yaml
├── docker-compose.yml
└── README.md
```

### 8. Documentation Requirements

Generate these documentation files:
- Requirements document (functional & non-functional)
- Architecture document with diagrams
- API specification (OpenAPI style)
- Test strategy document
- Deployment runbook
- Git workflow guide

### 9. Testing Strategy

Implement comprehensive testing:
- Unit tests: 70%+ coverage (JUnit 5, Mockito)
- Integration tests: API endpoint testing
- Frontend tests: Jest, React Testing Library
- All tests must pass in CI pipeline

### 10. Deployment

Configure for deployment to:
- Local: Docker Compose
- Cloud: Azure VM with Docker Compose
- Kubernetes manifests (optional)

Health endpoints required:
- /actuator/health
- /actuator/health/liveness
- /actuator/health/readiness

## EXECUTION APPROACH

Please proceed in this order:

### Phase 1: Foundation
1. Create the Maven multi-module backend project structure
2. Set up the common module with shared DTOs, exceptions, JWT utilities
3. Create the React Native Web frontend project with Expo

### Phase 2: Core Services
4. Implement auth-service with JWT authentication
5. Implement catalog-service with product management
6. Implement inventory-service with stock management
7. Build API Gateway with routing and JWT validation

### Phase 3: Business Services
8. Implement order-service with orchestration logic
9. Implement payment-service (mock)
10. Implement shipping-service (mock)
11. Implement notification-service

### Phase 4: Frontend
12. Build authentication screens (Login, Register)
13. Build product catalog screens
14. Build order management screens
15. Integrate with backend APIs

### Phase 5: DevOps
16. Create Dockerfiles for all services
17. Create docker-compose.yml for local development
18. Create Azure DevOps CI pipeline
19. Create Azure DevOps CD pipeline
20. Integrate Lineaje security scanning

### Phase 6: Quality
21. Write unit tests for all services
22. Write integration tests for APIs
23. Generate all documentation
24. Configure quality gates

## CONSTRAINTS

- Use Java 17 and Spring Boot 3.2.x
- Use React Native 0.73.x with Expo 50.x
- Use PostgreSQL 15.x for production
- Keep services stateless for horizontal scaling
- Follow 12-factor app principles
- No hardcoded credentials - use environment variables
- All APIs must be documented

## EXPECTED DELIVERABLES

1. Complete working backend with 8 microservices
2. Complete working frontend web application
3. Docker Compose configuration
4. CI/CD pipeline configurations
5. Lineaje security integration
6. All documentation files
7. Unit and integration tests

Start with Phase 1 and proceed systematically. After each phase, verify the components work before moving to the next phase.
```

---

## SHORTER VERSION (Quick Start Prompt)

```
Build a complete Order Management System with microservices architecture:

Backend: 8 Spring Boot services (Java 17)
- API Gateway (routing, JWT validation)
- Auth Service (registration, login, JWT)
- Catalog Service (products CRUD)
- Order Service (order lifecycle)
- Inventory Service (stock management)
- Payment Service (mock payments)
- Shipping Service (mock shipments)
- Notification Service (logging)

Frontend: React Native Web with Expo
- Login/Register screens
- Product catalog
- Order management

Infrastructure:
- PostgreSQL per service
- Docker + Docker Compose
- Azure DevOps CI/CD
- Lineaje SBOM360 security integration

Requirements:
- JWT authentication with role-based access
- 70%+ test coverage
- Health check endpoints
- OpenAPI documentation
- SBOM generation for supply chain security

Create project structure first, then implement services one by one. Include Dockerfiles, docker-compose.yml, CI/CD pipelines, and all documentation.
```

---

## INCREMENTAL PROMPTS (Step-by-Step)

### Step 1: Project Setup
```
Create a new Maven multi-module project for an Order Management System:
- Parent POM with Spring Boot 3.2.x
- Common module for shared code (DTOs, exceptions, JWT utils)
- Modules for: api-gateway, auth-service, catalog-service, order-service, inventory-service, payment-service, shipping-service, notification-service
- Use Java 17, include Spring Web, Spring Data JPA, Spring Security dependencies
```

### Step 2: Auth Service
```
Implement the auth-service module:
- User entity with email, password (BCrypt hashed), role (CUSTOMER/ADMIN)
- POST /api/auth/register - register new user
- POST /api/auth/login - authenticate and return JWT token
- GET /api/auth/me - get current user profile
- GET /api/auth/validate - validate JWT token
- Use HS256 algorithm for JWT
- Include unit tests with 70%+ coverage
```

### Step 3: Catalog Service
```
Implement the catalog-service module:
- Product entity with id, name, description, price, imageUrl
- GET /api/products - list all products (paginated)
- GET /api/products/{id} - get product by ID
- POST /api/products - create product (Admin only)
- PUT /api/products/{id} - update product (Admin only)
- DELETE /api/products/{id} - delete product (Admin only)
- Include unit tests
```

### Step 4: Inventory Service
```
Implement the inventory-service module:
- Inventory entity with productId, quantity, reservedQuantity
- GET /api/inventory - list all inventory
- GET /api/inventory/{productId} - get stock for product
- POST /api/inventory/reserve - reserve stock (reduce available)
- POST /api/inventory/release - release reserved stock
- PUT /api/inventory/{productId} - update stock level (Admin)
- Prevent negative stock
- Include unit tests
```

### Step 5: Order Service
```
Implement the order-service module:
- Order entity with userId, items, totalAmount, status
- OrderItem entity with productId, quantity, price
- Order status: PENDING, CONFIRMED, PAID, SHIPPED, DELIVERED, CANCELLED
- POST /api/orders - create order (calls inventory to reserve)
- GET /api/orders - list user's orders
- GET /api/orders/{id} - get order details
- POST /api/orders/{id}/cancel - cancel order (release inventory)
- POST /api/orders/{id}/pay - process payment (call payment service)
- Orchestrate calls to inventory, payment, shipping services
- Include unit tests
```

### Step 6: Payment & Shipping Services
```
Implement payment-service and shipping-service:

Payment Service:
- Payment entity with orderId, amount, status, transactionId
- POST /api/payments - process payment (mock - always succeeds)
- GET /api/payments/order/{orderId} - get payment details

Shipping Service:
- Shipment entity with orderId, trackingNumber, status, address
- POST /api/shipments - create shipment
- GET /api/shipments/order/{orderId} - get shipment details
- PUT /api/shipments/{id}/status - update shipment status

Both are mock implementations for demo purposes.
```

### Step 7: API Gateway
```
Implement the api-gateway using Spring Cloud Gateway:
- Route all /api/auth/** to auth-service
- Route all /api/products/** to catalog-service
- Route all /api/orders/** to order-service
- Route all /api/inventory/** to inventory-service
- Route all /api/payments/** to payment-service
- Route all /api/shipments/** to shipping-service
- Add JWT validation filter for protected routes
- Configure CORS for frontend
- Add health check endpoint
```

### Step 8: Frontend
```
Create a React Native Web frontend with Expo:
- Login screen with email/password
- Register screen
- Product list screen with add to cart
- Cart/checkout screen
- Order history screen
- Order detail screen
- Use Axios for API calls
- Store JWT in React Context
- Handle 401 errors with logout
```

### Step 9: Docker & Compose
```
Create Docker configuration:
- Multi-stage Dockerfile for each backend service
- Dockerfile for frontend (nginx)
- docker-compose.yml with:
  - 6 PostgreSQL containers (one per service with DB)
  - 8 microservice containers
  - 1 frontend container
  - Shared network
  - Health checks
  - Environment variables for configuration
```

### Step 10: CI/CD & Security
```
Create Azure DevOps pipelines:

CI Pipeline (azure-pipelines.yml):
- Build backend (Maven)
- Build frontend (npm)
- Run unit tests
- Code coverage report
- SBOM generation (CycloneDX)
- Security scan with Lineaje

CD Pipeline:
- Deploy to dev on develop branch
- Deploy to QA with approval
- Deploy to prod with approval

Create .lineaje/config.yaml for Lineaje integration.
```

---

## TIPS FOR USING THESE PROMPTS

1. **Start with the full prompt** if you want Droid to plan everything upfront
2. **Use incremental prompts** if you want more control over each step
3. **Ask for verification** after each major phase before proceeding
4. **Request tests** explicitly - mention "include unit tests with 70%+ coverage"
5. **Be specific about versions** - Java 17, Spring Boot 3.2.x, etc.
6. **Mention security requirements** early - JWT, BCrypt, role-based access
7. **Request documentation** as part of the deliverables

---

## SAMPLE FOLLOW-UP PROMPTS

After initial development, use these for enhancements:

```
Add Swagger/OpenAPI documentation to all backend services using springdoc-openapi
```

```
Create draw.io architecture diagrams showing:
1. High-level system architecture
2. Microservices communication flow
3. CI/CD pipeline stages
4. Database schema per service
```

```
Add rate limiting to the API Gateway - 100 requests per minute per user
```

```
Integrate SonarQube for code quality analysis in the CI pipeline
```

```
Add Kubernetes deployment manifests for all services with:
- Deployments with 2 replicas
- Services with ClusterIP
- Ingress for external access
- ConfigMaps and Secrets
```

---

**Document Version:** 1.0  
**Created:** 2026-04-02  
**Purpose:** Initial prompt to recreate the SDLC Demo application from scratch using Factory.AI Droid
