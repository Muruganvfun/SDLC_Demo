# Order Management System - Architecture Document

## 1. Architecture Overview

The Order Management System follows a microservices architecture pattern with a React Native Web frontend. Each service is independently deployable, owns its data, and communicates via REST APIs through an API Gateway.

### 1.1 Architecture Style
- **Pattern**: Microservices Architecture
- **Communication**: Synchronous REST (HTTP/JSON)
- **Authentication**: JWT-based with centralized validation
- **Data Management**: Database per Service
- **Deployment**: Containerized (Docker)

## 2. System Context Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              EXTERNAL                                    │
│  ┌──────────────┐                                                       │
│  │   Browser    │                                                       │
│  │  (Customer)  │                                                       │
│  └──────┬───────┘                                                       │
│         │ HTTPS                                                         │
└─────────┼───────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        ORDER MANAGEMENT SYSTEM                           │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │                    React Native Web App                         │    │
│  │                      (Expo Web Build)                           │    │
│  └────────────────────────────┬───────────────────────────────────┘    │
│                               │                                          │
│                               ▼                                          │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │                      API Gateway                                │    │
│  │                 (Spring Cloud Gateway)                          │    │
│  └────────────────────────────┬───────────────────────────────────┘    │
│                               │                                          │
│         ┌─────────┬─────────┬┴────────┬─────────┬─────────┐            │
│         ▼         ▼         ▼         ▼         ▼         ▼            │
│    ┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐       │
│    │ Auth   ││Catalog ││  Order ││Inventory││Payment ││Shipping│       │
│    │Service ││Service ││ Service││ Service ││Service ││Service │       │
│    └───┬────┘└───┬────┘└───┬────┘└───┬────┘└───┬────┘└───┬────┘       │
│        │         │         │         │         │         │             │
│        ▼         ▼         ▼         ▼         ▼         ▼             │
│    ┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐       │
│    │ AuthDB ││CatalogDB││OrderDB ││ InvDB  ││PaymentDB││ShipDB  │       │
│    └────────┘└────────┘└────────┘└────────┘└────────┘└────────┘       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

## 3. Microservices Overview

### 3.1 Service Inventory

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| api-gateway | 8080 | None | Request routing, JWT validation, rate limiting |
| auth-service | 8081 | auth_db | User registration, login, JWT issuance |
| catalog-service | 8082 | catalog_db | Product management, product queries |
| order-service | 8083 | order_db | Order lifecycle, order queries |
| inventory-service | 8084 | inventory_db | Stock management, reservations |
| payment-service | 8085 | payment_db | Payment processing (mock) |
| shipping-service | 8086 | shipping_db | Shipment creation, tracking (mock) |
| notification-service | 8087 | None | Event notifications (mock, in-memory) |

### 3.2 Service Dependencies

```
api-gateway
    └── All services (routing)

auth-service
    └── No dependencies (standalone)

catalog-service
    └── inventory-service (stock status)

order-service
    ├── catalog-service (product validation)
    ├── inventory-service (reserve/release)
    ├── payment-service (process payment)
    ├── shipping-service (create shipment)
    └── notification-service (send notifications)

inventory-service
    └── No dependencies

payment-service
    └── No dependencies (mock provider)

shipping-service
    └── No dependencies (mock provider)

notification-service
    └── No dependencies (mock logging)
```

## 4. Component Architecture

### 4.1 API Gateway (Spring Cloud Gateway)

```
┌─────────────────────────────────────────────┐
│              API Gateway                     │
├─────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Routes    │  │   JWT Filter        │  │
│  │  Config     │  │   (Validation)      │  │
│  └─────────────┘  └─────────────────────┘  │
│  ┌─────────────┐  ┌─────────────────────┐  │
│  │   CORS      │  │   Rate Limiter      │  │
│  │   Filter    │  │   (optional)        │  │
│  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────┘
```

**Responsibilities**:
- Route requests to appropriate microservices
- Validate JWT tokens on protected routes
- Handle CORS for web frontend
- Centralized error handling

### 4.2 Auth Service

```
┌─────────────────────────────────────────────┐
│              Auth Service                    │
├─────────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐   │
│  │         AuthController              │   │
│  │  POST /auth/register                │   │
│  │  POST /auth/login                   │   │
│  │  GET  /auth/validate                │   │
│  └──────────────┬──────────────────────┘   │
│                 │                           │
│  ┌──────────────▼──────────────────────┐   │
│  │         AuthService                 │   │
│  │  - registerUser()                   │   │
│  │  - authenticate()                   │   │
│  │  - validateToken()                  │   │
│  └──────────────┬──────────────────────┘   │
│                 │                           │
│  ┌──────────────▼──────────────────────┐   │
│  │         JwtTokenProvider            │   │
│  │  - generateToken()                  │   │
│  │  - validateToken()                  │   │
│  │  - extractClaims()                  │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │         UserRepository              │   │
│  │         (JPA/PostgreSQL)            │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

### 4.3 Standard Service Template

Each business service follows this layered architecture:

```
┌─────────────────────────────────────────────┐
│              [Service Name]                  │
├─────────────────────────────────────────────┤
│  Controller Layer                            │
│  ┌─────────────────────────────────────┐   │
│  │  @RestController                    │   │
│  │  - Input validation                 │   │
│  │  - HTTP response handling           │   │
│  └──────────────┬──────────────────────┘   │
│                 │                           │
│  Service Layer  │                           │
│  ┌──────────────▼──────────────────────┐   │
│  │  @Service                           │   │
│  │  - Business logic                   │   │
│  │  - Transaction management           │   │
│  │  - External service calls           │   │
│  └──────────────┬──────────────────────┘   │
│                 │                           │
│  Repository Layer                           │
│  ┌──────────────▼──────────────────────┐   │
│  │  @Repository (JPA)                  │   │
│  │  - Data access                      │   │
│  │  - Query methods                    │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  Domain Layer                               │
│  ┌─────────────────────────────────────┐   │
│  │  @Entity classes                    │   │
│  │  DTOs, Request/Response objects     │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

## 5. Data Architecture

### 5.1 Database Per Service Pattern

Each service owns its database schema. No direct database sharing between services.

### 5.2 Entity Relationship Diagrams

#### Auth Service Database
```
┌─────────────────────┐
│       users         │
├─────────────────────┤
│ id (PK)            │
│ email (UNIQUE)     │
│ password_hash      │
│ name               │
│ role               │
│ created_at         │
│ updated_at         │
└─────────────────────┘
```

#### Catalog Service Database
```
┌─────────────────────┐
│      products       │
├─────────────────────┤
│ id (PK)            │
│ name               │
│ description        │
│ price              │
│ image_url          │
│ active             │
│ created_at         │
│ updated_at         │
└─────────────────────┘
```

#### Inventory Service Database
```
┌─────────────────────┐
│     inventory       │
├─────────────────────┤
│ id (PK)            │
│ product_id (UNIQUE)│
│ quantity           │
│ reserved_quantity  │
│ updated_at         │
└─────────────────────┘
```

#### Order Service Database
```
┌─────────────────────┐       ┌─────────────────────┐
│       orders        │       │    order_items      │
├─────────────────────┤       ├─────────────────────┤
│ id (PK)            │──┐    │ id (PK)            │
│ user_id            │  │    │ order_id (FK)      │──┘
│ status             │  └───│ product_id         │
│ total_amount       │       │ product_name       │
│ shipping_address   │       │ quantity           │
│ created_at         │       │ unit_price         │
│ updated_at         │       │ subtotal           │
└─────────────────────┘       └─────────────────────┘
```

#### Payment Service Database
```
┌─────────────────────┐
│     payments        │
├─────────────────────┤
│ id (PK)            │
│ order_id           │
│ transaction_id     │
│ amount             │
│ status             │
│ payment_method     │
│ created_at         │
└─────────────────────┘
```

#### Shipping Service Database
```
┌─────────────────────┐
│     shipments       │
├─────────────────────┤
│ id (PK)            │
│ order_id           │
│ tracking_number    │
│ status             │
│ carrier            │
│ shipping_address   │
│ estimated_delivery │
│ created_at         │
│ updated_at         │
└─────────────────────┘
```

## 6. Security Architecture

### 6.1 Authentication Flow

```
┌──────────┐         ┌───────────┐         ┌─────────────┐
│  Client  │         │  Gateway  │         │ Auth Service│
└────┬─────┘         └─────┬─────┘         └──────┬──────┘
     │                     │                      │
     │ POST /auth/login    │                      │
     │────────────────────>│                      │
     │                     │  Forward request     │
     │                     │─────────────────────>│
     │                     │                      │
     │                     │   JWT Token          │
     │                     │<─────────────────────│
     │   JWT Token         │                      │
     │<────────────────────│                      │
     │                     │                      │
     │ GET /orders         │                      │
     │ Authorization: Bearer token                │
     │────────────────────>│                      │
     │                     │ Validate JWT         │
     │                     │─────────────────────>│
     │                     │    Valid             │
     │                     │<─────────────────────│
     │                     │                      │
     │                     │ Forward to Order Service
     │                     │ (with user context)  │
     │                     │                      │
```

### 6.2 JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user-uuid",
    "email": "user@example.com",
    "roles": ["CUSTOMER"],
    "iat": 1234567890,
    "exp": 1234654290
  }
}
```

### 6.3 Security Controls

| Layer | Control | Implementation |
|-------|---------|----------------|
| Transport | TLS/HTTPS | Nginx/Load Balancer (production) |
| Authentication | JWT Tokens | Auth Service + Gateway validation |
| Authorization | Role-based | @PreAuthorize annotations |
| Input Validation | Bean Validation | @Valid, custom validators |
| Password Storage | BCrypt hashing | Spring Security |
| API Protection | Rate limiting | Gateway filter (optional) |

## 7. Deployment Architecture

### 7.1 Local Development (Docker Compose)

```
┌─────────────────────────────────────────────────────────────────┐
│                     Docker Network (oms-network)                 │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Frontend Container                     │  │
│  │                    (nginx:80 → 3000)                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   Gateway Container                       │  │
│  │                   (Spring Boot:8080)                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│         │         │         │         │         │         │     │
│  ┌──────┴──┐┌─────┴───┐┌────┴────┐┌───┴────┐┌───┴────┐┌───┴──┐ │
│  │ Auth    ││ Catalog ││ Order   ││Inventory││Payment ││Ship  │ │
│  │ :8081   ││ :8082   ││ :8083   ││ :8084   ││ :8085  ││:8086 │ │
│  └────┬────┘└────┬────┘└────┬────┘└────┬────┘└────┬───┘└───┬──┘ │
│       │          │          │          │          │        │    │
│  ┌────┴────┐┌────┴────┐┌────┴────┐┌────┴────┐┌────┴───┐┌───┴──┐│
│  │ AuthDB  ││CatalogDB││ OrderDB ││ InvDB   ││PaymentDB││ShipDB││
│  │ PG:5432 ││ PG:5433 ││ PG:5434 ││ PG:5435 ││ PG:5436 ││PG:5437│
│  └─────────┘└─────────┘└─────────┘└─────────┘└─────────┘└──────┘│
└─────────────────────────────────────────────────────────────────┘
```

### 7.2 WAR Deployment (External Tomcat)

```
┌─────────────────────────────────────────────────────────────────┐
│                    External Tomcat Server                        │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Tomcat 10.x                            │  │
│  │                                                           │  │
│  │  webapps/                                                 │  │
│  │  ├── api-gateway.war                                      │  │
│  │  ├── auth-service.war                                     │  │
│  │  ├── catalog-service.war                                  │  │
│  │  ├── order-service.war                                    │  │
│  │  ├── inventory-service.war                                │  │
│  │  ├── payment-service.war                                  │  │
│  │  └── shipping-service.war                                 │  │
│  │                                                           │  │
│  │  Context paths:                                           │  │
│  │  /api-gateway, /auth-service, etc.                        │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Environment: SPRING_PROFILES_ACTIVE=production                 │
│  Database: External PostgreSQL cluster                          │
└─────────────────────────────────────────────────────────────────┘
```

## 8. Data Flow Diagrams

### 8.1 Order Creation Flow

```
┌────────┐  ┌─────────┐  ┌───────────┐  ┌─────────┐  ┌───────────┐
│ Client │  │ Gateway │  │  Order    │  │ Catalog │  │ Inventory │
└───┬────┘  └────┬────┘  └─────┬─────┘  └────┬────┘  └─────┬─────┘
    │            │             │              │             │
    │ POST /orders             │              │             │
    │───────────>│             │              │             │
    │            │ Validate JWT│              │             │
    │            │────────────>│              │             │
    │            │             │              │             │
    │            │ Forward     │              │             │
    │            │────────────>│              │             │
    │            │             │              │             │
    │            │             │ GET products │             │
    │            │             │─────────────>│             │
    │            │             │   products   │             │
    │            │             │<─────────────│             │
    │            │             │              │             │
    │            │             │ Reserve stock│             │
    │            │             │─────────────────────────────>│
    │            │             │              │   reserved   │
    │            │             │<─────────────────────────────│
    │            │             │              │             │
    │            │   Order Created            │             │
    │<───────────│<────────────│              │             │
    │            │             │              │             │
```

### 8.2 Payment & Shipping Flow

```
┌────────┐  ┌───────────┐  ┌─────────┐  ┌──────────┐  ┌────────────┐
│ Client │  │  Order    │  │ Payment │  │ Shipping │  │Notification│
└───┬────┘  └─────┬─────┘  └────┬────┘  └─────┬────┘  └──────┬─────┘
    │             │              │             │              │
    │ POST /orders/{id}/pay     │             │              │
    │────────────>│              │             │              │
    │             │              │             │              │
    │             │ Process payment            │              │
    │             │─────────────>│             │              │
    │             │   Success    │             │              │
    │             │<─────────────│             │              │
    │             │              │             │              │
    │             │ Update status: PAID        │              │
    │             │              │             │              │
    │             │ Create shipment            │              │
    │             │───────────────────────────>│              │
    │             │   Tracking #  │             │              │
    │             │<───────────────────────────│              │
    │             │              │             │              │
    │             │ Update status: SHIPPED     │              │
    │             │              │             │              │
    │             │ Send notification          │              │
    │             │────────────────────────────────────────────>│
    │             │              │             │     Logged   │
    │             │<────────────────────────────────────────────│
    │             │              │             │              │
    │   Order Updated            │             │              │
    │<────────────│              │             │              │
```

## 9. Technology Stack Summary

### 9.1 Backend

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.x |
| API Gateway | Spring Cloud Gateway | 4.1.x |
| Security | Spring Security | 6.x |
| ORM | Spring Data JPA / Hibernate | 3.2.x |
| Database | PostgreSQL / H2 | 15.x / 2.x |
| Build Tool | Maven | 3.9.x |
| JWT Library | jjwt | 0.12.x |
| Testing | JUnit 5, Mockito, TestContainers | 5.x |

### 9.2 Frontend

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | React Native | 0.73.x |
| Web Renderer | react-native-web | 0.19.x |
| Build/Dev | Expo | 50.x |
| HTTP Client | Axios | 1.x |
| Navigation | React Navigation | 6.x |
| State | React Context | Built-in |

### 9.3 Infrastructure

| Component | Technology | Version |
|-----------|------------|---------|
| Container Runtime | Docker | 24.x |
| Orchestration (local) | Docker Compose | 2.x |
| Web Server | Nginx (frontend) | 1.25.x |
| App Server | Apache Tomcat | 10.x |

## 10. API Gateway Routes Configuration

```yaml
routes:
  - id: auth-service
    uri: http://auth-service:8081
    predicates:
      - Path=/api/auth/**
    filters:
      - RewritePath=/api/auth/(?<segment>.*), /auth/${segment}

  - id: catalog-service
    uri: http://catalog-service:8082
    predicates:
      - Path=/api/products/**
    filters:
      - RewritePath=/api/products/(?<segment>.*), /products/${segment}
      - JwtAuthFilter

  - id: order-service
    uri: http://order-service:8083
    predicates:
      - Path=/api/orders/**
    filters:
      - RewritePath=/api/orders/(?<segment>.*), /orders/${segment}
      - JwtAuthFilter

  - id: inventory-service
    uri: http://inventory-service:8084
    predicates:
      - Path=/api/inventory/**
    filters:
      - RewritePath=/api/inventory/(?<segment>.*), /inventory/${segment}
      - JwtAuthFilter

  - id: payment-service
    uri: http://payment-service:8085
    predicates:
      - Path=/api/payments/**
    filters:
      - RewritePath=/api/payments/(?<segment>.*), /payments/${segment}
      - JwtAuthFilter

  - id: shipping-service
    uri: http://shipping-service:8086
    predicates:
      - Path=/api/shipments/**
    filters:
      - RewritePath=/api/shipments/(?<segment>.*), /shipments/${segment}
      - JwtAuthFilter
```

## 11. Cross-Cutting Concerns

### 11.1 Logging

- **Format**: JSON structured logging
- **Library**: SLF4J + Logback
- **Fields**: timestamp, level, service, traceId, message, context

### 11.2 Health Checks

All services expose:
- `GET /actuator/health` - Overall health
- `GET /actuator/health/liveness` - Kubernetes liveness probe
- `GET /actuator/health/readiness` - Kubernetes readiness probe

### 11.3 Error Handling

Standardized error response format:
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/orders",
  "details": [
    {"field": "quantity", "message": "must be greater than 0"}
  ]
}
```
