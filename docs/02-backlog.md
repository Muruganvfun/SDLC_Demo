# Order Management System - Product Backlog

## Definition of Done (DoD)

A feature is considered "Done" when:
1. Code is written and follows coding standards
2. Unit tests are written and passing (>70% coverage)
3. Integration tests are written for API endpoints
4. Code is reviewed (self-review for demo)
5. API documentation is updated
6. Feature works in Docker environment
7. No critical/high severity bugs

---

## Epic 1: User Authentication & Authorization

**Epic Description**: Enable secure user registration, login, and role-based access control.

### User Stories

#### US-1.1: User Registration
**As a** new user  
**I want to** register with my email and password  
**So that** I can access the order management system

**Acceptance Criteria**:
- [ ] Registration endpoint accepts email, password, name
- [ ] Email must be unique and valid format
- [ ] Password must be minimum 8 characters
- [ ] Password is stored as BCrypt hash
- [ ] Success returns user ID and confirmation
- [ ] Duplicate email returns 409 Conflict

**Story Points**: 3

#### US-1.2: User Login
**As a** registered user  
**I want to** login with my credentials  
**So that** I can access protected features

**Acceptance Criteria**:
- [ ] Login endpoint accepts email and password
- [ ] Valid credentials return JWT access token
- [ ] Token contains user ID and roles
- [ ] Token expires after configured duration (default: 24h)
- [ ] Invalid credentials return 401 Unauthorized

**Story Points**: 3

#### US-1.3: Role-Based Access Control
**As an** administrator  
**I want to** restrict certain actions to admin users  
**So that** customers cannot perform admin operations

**Acceptance Criteria**:
- [ ] Users have roles: CUSTOMER (default), ADMIN
- [ ] Admin endpoints check for ADMIN role
- [ ] Unauthorized role access returns 403 Forbidden
- [ ] Token includes user roles claim

**Story Points**: 2

#### US-1.4: Token Validation
**As the** system  
**I want to** validate JWT tokens on every request  
**So that** only authenticated users access protected resources

**Acceptance Criteria**:
- [ ] API Gateway validates tokens
- [ ] Expired tokens return 401
- [ ] Invalid tokens return 401
- [ ] Valid tokens pass through to services

**Story Points**: 2

---

## Epic 2: Product Catalog Management

**Epic Description**: Enable product browsing and management for customers and admins.

### User Stories

#### US-2.1: View Product List
**As a** customer  
**I want to** see a list of available products  
**So that** I can browse what's available to purchase

**Acceptance Criteria**:
- [ ] GET /products returns paginated product list
- [ ] Each product shows: id, name, description, price, availability
- [ ] Supports pagination (page, size parameters)
- [ ] Returns empty list if no products

**Story Points**: 2

#### US-2.2: View Product Details
**As a** customer  
**I want to** see detailed information about a product  
**So that** I can decide whether to purchase it

**Acceptance Criteria**:
- [ ] GET /products/{id} returns product details
- [ ] Shows: id, name, description, price, stock status
- [ ] Returns 404 if product not found

**Story Points**: 1

#### US-2.3: Create Product (Admin)
**As an** administrator  
**I want to** add new products to the catalog  
**So that** customers can purchase them

**Acceptance Criteria**:
- [ ] POST /products creates new product (ADMIN only)
- [ ] Requires: name, description, price
- [ ] Price must be positive
- [ ] Returns created product with ID
- [ ] Returns 403 if not admin

**Story Points**: 2

#### US-2.4: Update Product (Admin)
**As an** administrator  
**I want to** update product information  
**So that** I can correct errors or change prices

**Acceptance Criteria**:
- [ ] PUT /products/{id} updates product (ADMIN only)
- [ ] Can update: name, description, price
- [ ] Returns 404 if product not found
- [ ] Returns 403 if not admin

**Story Points**: 2

---

## Epic 3: Inventory Management

**Epic Description**: Track and manage product stock levels.

### User Stories

#### US-3.1: View Inventory
**As an** administrator  
**I want to** see current stock levels  
**So that** I can manage inventory

**Acceptance Criteria**:
- [ ] GET /inventory returns all inventory records
- [ ] Shows: productId, quantity, reservedQuantity
- [ ] Supports filtering by product ID

**Story Points**: 2

#### US-3.2: Reserve Stock
**As the** order service  
**I want to** reserve stock when an order is placed  
**So that** items are held for the customer

**Acceptance Criteria**:
- [ ] POST /inventory/reserve decrements available stock
- [ ] Returns success if sufficient stock
- [ ] Returns 400 if insufficient stock
- [ ] Tracks reserved quantity separately

**Story Points**: 3

#### US-3.3: Release Stock
**As the** order service  
**I want to** release reserved stock when order is cancelled  
**So that** items are available for other customers

**Acceptance Criteria**:
- [ ] POST /inventory/release increments available stock
- [ ] Decrements reserved quantity
- [ ] Returns success on completion

**Story Points**: 2

#### US-3.4: Update Inventory (Admin)
**As an** administrator  
**I want to** adjust inventory levels  
**So that** I can restock or correct quantities

**Acceptance Criteria**:
- [ ] PUT /inventory/{productId} updates quantity (ADMIN only)
- [ ] Quantity cannot be negative
- [ ] Returns updated inventory record

**Story Points**: 2

---

## Epic 4: Order Management

**Epic Description**: Enable customers to create and manage orders.

### User Stories

#### US-4.1: Create Order
**As a** customer  
**I want to** place an order for products  
**So that** I can purchase items

**Acceptance Criteria**:
- [ ] POST /orders creates new order
- [ ] Accepts: list of items (productId, quantity)
- [ ] Validates product existence
- [ ] Reserves inventory for each item
- [ ] Calculates total price
- [ ] Returns order with PENDING status
- [ ] Returns 400 if insufficient stock

**Story Points**: 5

#### US-4.2: View My Orders
**As a** customer  
**I want to** see my order history  
**So that** I can track my purchases

**Acceptance Criteria**:
- [ ] GET /orders returns customer's orders
- [ ] Shows: orderId, status, total, createdDate
- [ ] Ordered by most recent first
- [ ] Only shows current user's orders

**Story Points**: 2

#### US-4.3: View Order Details
**As a** customer  
**I want to** see details of a specific order  
**So that** I can review what I ordered

**Acceptance Criteria**:
- [ ] GET /orders/{id} returns order details
- [ ] Shows: items, quantities, prices, status, total
- [ ] Shows shipping info if shipped
- [ ] Returns 404 if order not found
- [ ] Returns 403 if not owner (unless admin)

**Story Points**: 2

#### US-4.4: Cancel Order
**As a** customer  
**I want to** cancel a pending order  
**So that** I can change my mind before payment

**Acceptance Criteria**:
- [ ] POST /orders/{id}/cancel cancels order
- [ ] Only works for PENDING status
- [ ] Releases reserved inventory
- [ ] Updates status to CANCELLED
- [ ] Returns 400 if not cancellable

**Story Points**: 3

#### US-4.5: Update Order Status (Internal)
**As the** system  
**I want to** update order status through lifecycle  
**So that** orders progress correctly

**Acceptance Criteria**:
- [ ] Status transitions: PENDING → CONFIRMED → PAID → SHIPPED → DELIVERED
- [ ] Invalid transitions return 400
- [ ] Status changes are logged

**Story Points**: 3

---

## Epic 5: Payment Processing

**Epic Description**: Process payments for orders (mock implementation).

### User Stories

#### US-5.1: Process Payment
**As the** order service  
**I want to** process payment for confirmed orders  
**So that** customers can complete their purchase

**Acceptance Criteria**:
- [ ] POST /payments processes payment
- [ ] Accepts: orderId, amount, paymentMethod
- [ ] Mock provider: 90% success, 10% random failure
- [ ] Returns transaction ID on success
- [ ] Returns payment status (SUCCESS/FAILED)

**Story Points**: 3

#### US-5.2: View Payment Status
**As a** customer  
**I want to** see payment status for my order  
**So that** I know if payment was successful

**Acceptance Criteria**:
- [ ] GET /payments/order/{orderId} returns payment details
- [ ] Shows: transactionId, status, amount, timestamp
- [ ] Returns 404 if no payment found

**Story Points**: 2

---

## Epic 6: Shipping Management

**Epic Description**: Create and track shipments (mock implementation).

### User Stories

#### US-6.1: Create Shipment
**As the** order service  
**I want to** create a shipment for paid orders  
**So that** items are delivered to customers

**Acceptance Criteria**:
- [ ] POST /shipments creates shipment
- [ ] Accepts: orderId, address
- [ ] Generates tracking number
- [ ] Returns shipment with PROCESSING status

**Story Points**: 2

#### US-6.2: View Shipment Status
**As a** customer  
**I want to** track my shipment  
**So that** I know when to expect delivery

**Acceptance Criteria**:
- [ ] GET /shipments/order/{orderId} returns shipment details
- [ ] Shows: trackingNumber, status, estimatedDelivery
- [ ] Status: PROCESSING → SHIPPED → IN_TRANSIT → DELIVERED

**Story Points**: 2

#### US-6.3: Update Shipment Status (Mock)
**As the** system  
**I want to** simulate shipment progress  
**So that** demo shows realistic flow

**Acceptance Criteria**:
- [ ] PUT /shipments/{id}/status updates status
- [ ] Admin only endpoint
- [ ] Validates status transitions

**Story Points**: 1

---

## Epic 7: Notifications

**Epic Description**: Send notifications for order events (mock implementation).

### User Stories

#### US-7.1: Order Confirmation Notification
**As a** customer  
**I want to** receive order confirmation  
**So that** I know my order was placed

**Acceptance Criteria**:
- [ ] Notification logged when order created
- [ ] Contains: orderId, items, total
- [ ] Mock: logs to console/file

**Story Points**: 1

#### US-7.2: Shipping Notification
**As a** customer  
**I want to** receive shipping updates  
**So that** I can track my delivery

**Acceptance Criteria**:
- [ ] Notification logged when order shipped
- [ ] Contains: orderId, trackingNumber
- [ ] Mock: logs to console/file

**Story Points**: 1

---

## Epic 8: Frontend Web Application

**Epic Description**: Build React Native Web interface for customers.

### User Stories

#### US-8.1: Login Screen
**As a** user  
**I want to** see a login screen  
**So that** I can authenticate

**Acceptance Criteria**:
- [ ] Email and password input fields
- [ ] Login button calls auth API
- [ ] Shows error on invalid credentials
- [ ] Redirects to product list on success
- [ ] Link to registration

**Story Points**: 3

#### US-8.2: Registration Screen
**As a** new user  
**I want to** register an account  
**So that** I can use the system

**Acceptance Criteria**:
- [ ] Name, email, password input fields
- [ ] Validation messages shown
- [ ] Redirects to login on success

**Story Points**: 2

#### US-8.3: Product List Screen
**As a** customer  
**I want to** browse products  
**So that** I can select items to order

**Acceptance Criteria**:
- [ ] Displays product cards with name, price
- [ ] Shows availability status
- [ ] "Add to Order" button per product
- [ ] Quantity selector

**Story Points**: 3

#### US-8.4: Create Order Screen
**As a** customer  
**I want to** create an order  
**So that** I can purchase selected items

**Acceptance Criteria**:
- [ ] Shows selected items with quantities
- [ ] Displays calculated total
- [ ] "Place Order" button
- [ ] Confirmation message on success

**Story Points**: 3

#### US-8.5: Order List Screen
**As a** customer  
**I want to** see my orders  
**So that** I can track my purchases

**Acceptance Criteria**:
- [ ] Lists all orders with status
- [ ] Shows order date and total
- [ ] Click to view details
- [ ] Status badge (color coded)

**Story Points**: 2

#### US-8.6: Order Details Screen
**As a** customer  
**I want to** see order details  
**So that** I can review items and track status

**Acceptance Criteria**:
- [ ] Shows all order items
- [ ] Shows current status
- [ ] Shows shipping info if available
- [ ] Cancel button for pending orders

**Story Points**: 2

---

## Epic 9: API Gateway & Infrastructure

**Epic Description**: Setup API Gateway and infrastructure components.

### User Stories

#### US-9.1: API Gateway Routing
**As the** system  
**I want to** route requests through API Gateway  
**So that** clients have single entry point

**Acceptance Criteria**:
- [ ] Routes to all microservices
- [ ] Strips service prefix from paths
- [ ] Health check endpoint

**Story Points**: 3

#### US-9.2: JWT Validation at Gateway
**As the** system  
**I want to** validate tokens at gateway  
**So that** invalid requests are rejected early

**Acceptance Criteria**:
- [ ] Validates JWT on protected routes
- [ ] Passes user info to downstream services
- [ ] Allows public endpoints (login, register)

**Story Points**: 3

#### US-9.3: Docker Compose Setup
**As a** developer  
**I want to** run entire system with docker-compose  
**So that** I can demo locally

**Acceptance Criteria**:
- [ ] All services in docker-compose.yml
- [ ] PostgreSQL databases configured
- [ ] Network connectivity between services
- [ ] Environment variables documented

**Story Points**: 3

#### US-9.4: Health Endpoints
**As an** operator  
**I want to** check service health  
**So that** I can monitor system status

**Acceptance Criteria**:
- [ ] /actuator/health on each service
- [ ] Returns UP/DOWN status
- [ ] Includes database connectivity check

**Story Points**: 2

---

## Sprint Planning Summary

### Sprint 1: Foundation (Story Points: 18)
- US-1.1, US-1.2, US-1.3, US-1.4 (Authentication)
- US-9.1, US-9.4 (Gateway basics)

### Sprint 2: Catalog & Inventory (Story Points: 14)
- US-2.1, US-2.2, US-2.3, US-2.4 (Catalog)
- US-3.1, US-3.2, US-3.3, US-3.4 (Inventory)

### Sprint 3: Orders & Payments (Story Points: 17)
- US-4.1, US-4.2, US-4.3, US-4.4, US-4.5 (Orders)
- US-5.1, US-5.2 (Payments)

### Sprint 4: Shipping & Notifications (Story Points: 7)
- US-6.1, US-6.2, US-6.3 (Shipping)
- US-7.1, US-7.2 (Notifications)

### Sprint 5: Frontend & Integration (Story Points: 18)
- US-8.1 through US-8.6 (Frontend)
- US-9.2, US-9.3 (Infrastructure)

---

## Backlog Prioritization (MoSCoW)

### Must Have
- User authentication (login/register)
- Product catalog viewing
- Order creation and viewing
- Payment processing (mock)
- Basic frontend screens
- Docker compose setup

### Should Have
- Admin product management
- Inventory management
- Order cancellation
- Shipping tracking

### Could Have
- Notifications
- Advanced filtering/search
- Order history pagination

### Won't Have (This Release)
- Real payment integration
- Real shipping integration
- Email notifications
- User profile management
