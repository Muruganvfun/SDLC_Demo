# Order Management System - Demo Script

## Demo Duration: 7 Minutes

---

## Introduction (30 seconds)

**[SLIDE: Title]**

"Welcome! Today I'll demonstrate our Order Management System - a complete microservices-based e-commerce platform built with Java Spring Boot and React Native Web.

This demo covers the full SDLC: from requirements through architecture, implementation, and a working system."

---

## Part 1: Architecture Overview (1 minute)

**[SHOW: Architecture Diagram or docs/03-architecture.md]**

"Our system follows a microservices architecture with 8 independent services:

1. **API Gateway** - Single entry point, JWT validation, routing
2. **Auth Service** - User registration and JWT token management
3. **Catalog Service** - Product management
4. **Order Service** - Order lifecycle orchestration
5. **Inventory Service** - Stock management and reservations
6. **Payment Service** - Mock payment processing
7. **Shipping Service** - Mock shipment tracking
8. **Notification Service** - Event notifications

Each service owns its database and communicates via REST APIs. The frontend is built with React Native Web using Expo."

---

## Part 2: System Startup (30 seconds)

**[TERMINAL: Start services]**

"Let's start our system. For this demo, I'm using Docker Compose to orchestrate all services:

```bash
docker-compose up -d
```

Services starting: databases, backend services, API gateway, and frontend.

While that starts, let me show you the codebase structure..."

**[SHOW: Project structure in IDE]**

"The backend is a Maven multi-module project. Each service follows the same layered architecture: Controller → Service → Repository."

---

## Part 3: User Registration & Login (1 minute)

**[BROWSER: http://localhost:3000]**

"Here's our React Native Web frontend. Let's start by creating a new account.

**[CLICK: Register link]**

Entering:
- Name: 'Demo User'
- Email: 'demo@example.com'  
- Password: 'password123'

**[SUBMIT]**

Account created! Now let's login with these credentials.

**[LOGIN]**

Success! We received a JWT token which is now stored for authenticated requests. Notice I'm now on the Products page."

---

## Part 4: Browse Products & Add to Cart (1 minute)

**[SHOW: Product List]**

"Our catalog shows products loaded from the Catalog Service. Each product displays:
- Name and description
- Price
- Stock availability (fetched from Inventory Service)

Let me add some items to my cart:

**[ADD: Wireless Mouse - qty 2]**
**[ADD: Mechanical Keyboard - qty 1]**

Notice the cart bar at the bottom showing 2 items and the running total.

Let's checkout!"

**[CLICK: Checkout]**

---

## Part 5: Create Order (1 minute)

**[SHOW: Create Order Screen]**

"Here's our order screen showing:
- Cart items with quantity controls
- Shipping address form (pre-filled for demo)
- Order total: $149.97

When I place this order, the system will:
1. Validate products exist via Catalog Service
2. Reserve inventory via Inventory Service
3. Calculate total and save order

**[CLICK: Place Order]**

Order confirmed! The system reserved our stock. Let's view our orders."

---

## Part 6: Order Lifecycle (1 minute 30 seconds)

**[NAVIGATE: Order List]**

"Here's my order history. Notice the status badge showing 'CONFIRMED'.

**[CLICK: Order to view details]**

Order details show:
- All items with prices
- Shipping address
- Current status

Now let's process payment:

**[CLICK: Pay Now]**

Processing... Payment successful! 

The order status changed to 'SHIPPED' because after payment:
1. Payment Service processed the transaction
2. Shipping Service created a shipment with tracking number
3. Order status updated

**[SHOW: Payment and Shipping info]**

Here you can see:
- Transaction ID from Payment Service
- Tracking number from Shipping Service

The order will progress through: PENDING → CONFIRMED → PAID → SHIPPED → DELIVERED"

---

## Part 7: Admin Features & API (30 seconds)

**[TERMINAL or POSTMAN]**

"Let me quickly show the admin capabilities. Using the admin account:

```bash
curl -X POST http://localhost:8080/api/products \
  -H 'Authorization: Bearer ADMIN_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{"name":"New Product","price":49.99}'
```

Admins can:
- Create/update products
- Adjust inventory
- Update shipment status

All protected by role-based access control."

---

## Part 8: Wrap-Up (30 seconds)

**[SHOW: docs folder or summary slide]**

"To summarize what we've built:

**Documentation:**
- Requirements specification
- Product backlog with user stories
- Architecture diagrams
- API specifications
- Test strategy
- This runbook and demo script

**Technical Stack:**
- 8 Spring Boot microservices
- React Native Web frontend
- PostgreSQL databases
- Docker Compose deployment
- JWT authentication

**Key Features:**
- Full order lifecycle
- Inventory management
- Mock payments and shipping
- Role-based access

The entire SDLC is captured in our /docs folder. Questions?"

---

## Demo Checklist

Before demo:
- [ ] All services running (`docker-compose up`)
- [ ] Browser open to http://localhost:3000
- [ ] Terminal ready for API calls
- [ ] Architecture diagram accessible
- [ ] Demo user NOT already registered

During demo:
- [ ] Keep terminal visible for service health
- [ ] Narrate what's happening behind the scenes
- [ ] Highlight microservices communication

---

## Quick Recovery Commands

If something fails:

```bash
# Restart all services
docker-compose restart

# Check service health
curl http://localhost:8080/actuator/health

# View logs
docker-compose logs -f api-gateway

# Reset databases
docker-compose down -v && docker-compose up -d
```

---

## Talking Points

**On Architecture:**
- "Each service is independently deployable"
- "Database per service pattern prevents tight coupling"
- "API Gateway provides single entry point and security"

**On Technology Choices:**
- "Spring Boot 3.2 with Java 17 for modern features"
- "React Native Web allows code sharing with mobile"
- "Docker Compose simplifies local development"

**On Enterprise Readiness:**
- "Health endpoints for monitoring"
- "Structured logging for observability"
- "Input validation and error handling"
