# Order Management System - Requirements Specification

## 1. Project Overview

The Order Management System (OMS) is a microservices-based e-commerce platform that handles the complete order lifecycle from product browsing to delivery tracking. Built with Java Spring Boot backend and React Native Web frontend.

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

### 3.3 Reliability (NFR-REL)

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-REL-001 | Health check endpoints per service | Required |
| NFR-REL-002 | Graceful error handling | Required |
| NFR-REL-003 | Structured logging (JSON format) | Required |

### 3.4 Maintainability (NFR-MAIN)

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-MAIN-001 | Unit test coverage | > 70% |
| NFR-MAIN-002 | Integration test coverage for APIs | > 80% |
| NFR-MAIN-003 | Code follows standard conventions | Required |
| NFR-MAIN-004 | API documentation (OpenAPI) | Required |

### 3.5 Deployment (NFR-DEP)

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-DEP-001 | Docker containerization | Required |
| NFR-DEP-002 | Docker Compose for local development | Required |
| NFR-DEP-003 | Support embedded Tomcat (dev mode) | Required |
| NFR-DEP-004 | Support external Tomcat (WAR deployment) | Required |
| NFR-DEP-005 | Environment-based configuration | Required |

## 4. System Constraints

### 4.1 Technical Constraints

- **Backend**: Java 17+, Spring Boot 3.x, Spring Cloud Gateway
- **Frontend**: React Native Web with Expo
- **Database**: PostgreSQL (production), H2 (development/demo)
- **Runtime**: Apache Tomcat (embedded or external)
- **Container**: Docker, Docker Compose

### 4.2 Business Constraints

- System is for demonstration/training purposes
- No real payment processing (mock only)
- No real shipping integration (mock only)
- Single-region deployment

## 5. Assumptions

1. Users have modern web browsers (Chrome, Firefox, Safari, Edge)
2. Docker is available for local development
3. Java 17+ JDK is installed for non-Docker development
4. Network connectivity between services in same Docker network
5. Demo data will be seeded on startup

## 6. Dependencies

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

## 7. Glossary

| Term | Definition |
|------|------------|
| OMS | Order Management System |
| JWT | JSON Web Token |
| API Gateway | Entry point for all client requests |
| Microservice | Independent deployable service unit |
| WAR | Web Application Archive (Java deployment format) |
