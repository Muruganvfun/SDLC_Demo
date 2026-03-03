# Order Management System - API Specification

## Base URL

- **Development**: `http://localhost:8080/api`
- **Production**: `https://api.example.com/api`

## Authentication

All protected endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

## Common Response Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Invalid/missing token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error |

## Error Response Format

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/orders",
  "details": [
    {
      "field": "quantity",
      "message": "must be greater than 0"
    }
  ]
}
```

---

## 1. Auth Service API

Base path: `/api/auth`

### 1.1 Register User

**POST** `/api/auth/register`

Register a new user account.

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe"
}
```

**Validation**:
- `email`: Required, valid email format, unique
- `password`: Required, minimum 8 characters
- `name`: Required, 2-100 characters

**Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "CUSTOMER",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Errors**:
- 400: Validation failed
- 409: Email already registered

---

### 1.2 Login

**POST** `/api/auth/login`

Authenticate user and receive JWT token.

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "name": "John Doe",
    "role": "CUSTOMER"
  }
}
```

**Errors**:
- 401: Invalid credentials

---

### 1.3 Validate Token

**GET** `/api/auth/validate`

Validate JWT token and return user info. Used internally by API Gateway.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK):
```json
{
  "valid": true,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "roles": ["CUSTOMER"]
}
```

**Errors**:
- 401: Invalid or expired token

---

### 1.4 Get Current User

**GET** `/api/auth/me`

Get current authenticated user's profile.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "CUSTOMER",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

## 2. Catalog Service API

Base path: `/api/products`

### 2.1 List Products

**GET** `/api/products`

Get paginated list of products.

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number (0-indexed) |
| size | int | 20 | Items per page (max 100) |
| active | boolean | true | Filter by active status |

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "prod-001",
      "name": "Wireless Mouse",
      "description": "Ergonomic wireless mouse with USB receiver",
      "price": 29.99,
      "imageUrl": "/images/mouse.jpg",
      "active": true,
      "inStock": true,
      "createdAt": "2024-01-10T08:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50,
  "totalPages": 3
}
```

---

### 2.2 Get Product Details

**GET** `/api/products/{id}`

Get single product by ID.

**Path Parameters**:
- `id`: Product ID

**Response** (200 OK):
```json
{
  "id": "prod-001",
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with USB receiver",
  "price": 29.99,
  "imageUrl": "/images/mouse.jpg",
  "active": true,
  "stockQuantity": 150,
  "createdAt": "2024-01-10T08:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Errors**:
- 404: Product not found

---

### 2.3 Create Product (Admin)

**POST** `/api/products`

Create a new product. Requires ADMIN role.

**Headers**: `Authorization: Bearer <admin_token>`

**Request Body**:
```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with USB receiver",
  "price": 29.99,
  "imageUrl": "/images/mouse.jpg",
  "initialStock": 100
}
```

**Validation**:
- `name`: Required, 2-200 characters
- `description`: Optional, max 2000 characters
- `price`: Required, > 0
- `initialStock`: Optional, >= 0, default 0

**Response** (201 Created):
```json
{
  "id": "prod-001",
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with USB receiver",
  "price": 29.99,
  "imageUrl": "/images/mouse.jpg",
  "active": true,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Errors**:
- 403: Not authorized (not admin)

---

### 2.4 Update Product (Admin)

**PUT** `/api/products/{id}`

Update an existing product. Requires ADMIN role.

**Headers**: `Authorization: Bearer <admin_token>`

**Request Body**:
```json
{
  "name": "Wireless Mouse Pro",
  "description": "Updated description",
  "price": 34.99,
  "active": true
}
```

**Response** (200 OK):
```json
{
  "id": "prod-001",
  "name": "Wireless Mouse Pro",
  "description": "Updated description",
  "price": 34.99,
  "imageUrl": "/images/mouse.jpg",
  "active": true,
  "updatedAt": "2024-01-15T11:00:00Z"
}
```

---

### 2.5 Delete Product (Admin)

**DELETE** `/api/products/{id}`

Soft-delete a product (sets active=false). Requires ADMIN role.

**Response** (204 No Content)

---

## 3. Inventory Service API

Base path: `/api/inventory`

### 3.1 Get Inventory

**GET** `/api/inventory`

Get all inventory records. Requires authentication.

**Response** (200 OK):
```json
{
  "items": [
    {
      "productId": "prod-001",
      "quantity": 150,
      "reservedQuantity": 10,
      "availableQuantity": 140,
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ]
}
```

---

### 3.2 Get Product Inventory

**GET** `/api/inventory/{productId}`

Get inventory for specific product.

**Response** (200 OK):
```json
{
  "productId": "prod-001",
  "quantity": 150,
  "reservedQuantity": 10,
  "availableQuantity": 140,
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

---

### 3.3 Reserve Stock

**POST** `/api/inventory/reserve`

Reserve stock for an order. Internal service call.

**Request Body**:
```json
{
  "orderId": "order-001",
  "items": [
    {
      "productId": "prod-001",
      "quantity": 2
    }
  ]
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "orderId": "order-001",
  "reservations": [
    {
      "productId": "prod-001",
      "quantity": 2,
      "reserved": true
    }
  ]
}
```

**Errors**:
- 400: Insufficient stock

---

### 3.4 Release Stock

**POST** `/api/inventory/release`

Release reserved stock. Used when order is cancelled.

**Request Body**:
```json
{
  "orderId": "order-001",
  "items": [
    {
      "productId": "prod-001",
      "quantity": 2
    }
  ]
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "orderId": "order-001",
  "released": [
    {
      "productId": "prod-001",
      "quantity": 2
    }
  ]
}
```

---

### 3.5 Update Inventory (Admin)

**PUT** `/api/inventory/{productId}`

Adjust inventory levels. Requires ADMIN role.

**Request Body**:
```json
{
  "quantity": 200,
  "reason": "Restock shipment received"
}
```

**Response** (200 OK):
```json
{
  "productId": "prod-001",
  "quantity": 200,
  "reservedQuantity": 10,
  "availableQuantity": 190,
  "updatedAt": "2024-01-15T11:00:00Z"
}
```

---

## 4. Order Service API

Base path: `/api/orders`

### 4.1 Create Order

**POST** `/api/orders`

Create a new order.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "items": [
    {
      "productId": "prod-001",
      "quantity": 2
    },
    {
      "productId": "prod-002",
      "quantity": 1
    }
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```

**Validation**:
- `items`: Required, at least 1 item
- `items[].productId`: Required
- `items[].quantity`: Required, > 0
- `shippingAddress`: Required

**Response** (201 Created):
```json
{
  "id": "order-001",
  "userId": "user-001",
  "status": "PENDING",
  "items": [
    {
      "productId": "prod-001",
      "productName": "Wireless Mouse",
      "quantity": 2,
      "unitPrice": 29.99,
      "subtotal": 59.98
    }
  ],
  "totalAmount": 89.97,
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Errors**:
- 400: Validation failed or insufficient stock

---

### 4.2 List My Orders

**GET** `/api/orders`

Get current user's orders.

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 20 | Items per page |
| status | string | null | Filter by status |

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "order-001",
      "status": "PENDING",
      "totalAmount": 89.97,
      "itemCount": 2,
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
```

---

### 4.3 Get Order Details

**GET** `/api/orders/{id}`

Get order details by ID.

**Response** (200 OK):
```json
{
  "id": "order-001",
  "userId": "user-001",
  "status": "SHIPPED",
  "items": [
    {
      "productId": "prod-001",
      "productName": "Wireless Mouse",
      "quantity": 2,
      "unitPrice": 29.99,
      "subtotal": 59.98
    }
  ],
  "totalAmount": 89.97,
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "payment": {
    "transactionId": "txn-001",
    "status": "SUCCESS",
    "paidAt": "2024-01-15T10:35:00Z"
  },
  "shipment": {
    "trackingNumber": "TRK123456789",
    "carrier": "DemoShip",
    "status": "SHIPPED",
    "estimatedDelivery": "2024-01-20"
  },
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:40:00Z"
}
```

---

### 4.4 Cancel Order

**POST** `/api/orders/{id}/cancel`

Cancel a pending order.

**Response** (200 OK):
```json
{
  "id": "order-001",
  "status": "CANCELLED",
  "message": "Order cancelled successfully"
}
```

**Errors**:
- 400: Order cannot be cancelled (not in PENDING status)

---

### 4.5 Process Order Payment

**POST** `/api/orders/{id}/pay`

Process payment for an order.

**Request Body**:
```json
{
  "paymentMethod": "CREDIT_CARD"
}
```

**Response** (200 OK):
```json
{
  "orderId": "order-001",
  "status": "PAID",
  "payment": {
    "transactionId": "txn-001",
    "status": "SUCCESS",
    "amount": 89.97
  }
}
```

**Errors**:
- 400: Payment failed or invalid order status

---

## 5. Payment Service API

Base path: `/api/payments`

### 5.1 Process Payment

**POST** `/api/payments`

Process a payment. Internal service call.

**Request Body**:
```json
{
  "orderId": "order-001",
  "amount": 89.97,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD"
}
```

**Response** (200 OK):
```json
{
  "id": "pay-001",
  "orderId": "order-001",
  "transactionId": "txn-123456789",
  "amount": 89.97,
  "currency": "USD",
  "status": "SUCCESS",
  "paymentMethod": "CREDIT_CARD",
  "processedAt": "2024-01-15T10:35:00Z"
}
```

**Mock Behavior**: 90% success, 10% random failure

---

### 5.2 Get Payment by Order

**GET** `/api/payments/order/{orderId}`

Get payment details for an order.

**Response** (200 OK):
```json
{
  "id": "pay-001",
  "orderId": "order-001",
  "transactionId": "txn-123456789",
  "amount": 89.97,
  "status": "SUCCESS",
  "processedAt": "2024-01-15T10:35:00Z"
}
```

---

## 6. Shipping Service API

Base path: `/api/shipments`

### 6.1 Create Shipment

**POST** `/api/shipments`

Create a shipment for an order. Internal service call.

**Request Body**:
```json
{
  "orderId": "order-001",
  "address": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```

**Response** (201 Created):
```json
{
  "id": "ship-001",
  "orderId": "order-001",
  "trackingNumber": "TRK123456789",
  "carrier": "DemoShip",
  "status": "PROCESSING",
  "address": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "estimatedDelivery": "2024-01-20",
  "createdAt": "2024-01-15T10:40:00Z"
}
```

---

### 6.2 Get Shipment by Order

**GET** `/api/shipments/order/{orderId}`

Get shipment details for an order.

**Response** (200 OK):
```json
{
  "id": "ship-001",
  "orderId": "order-001",
  "trackingNumber": "TRK123456789",
  "carrier": "DemoShip",
  "status": "IN_TRANSIT",
  "estimatedDelivery": "2024-01-20",
  "statusHistory": [
    {
      "status": "PROCESSING",
      "timestamp": "2024-01-15T10:40:00Z"
    },
    {
      "status": "SHIPPED",
      "timestamp": "2024-01-16T08:00:00Z"
    },
    {
      "status": "IN_TRANSIT",
      "timestamp": "2024-01-17T14:00:00Z"
    }
  ]
}
```

---

### 6.3 Update Shipment Status (Admin)

**PUT** `/api/shipments/{id}/status`

Update shipment status. Admin only, used for demo.

**Request Body**:
```json
{
  "status": "DELIVERED"
}
```

**Valid Status Values**: PROCESSING, SHIPPED, IN_TRANSIT, DELIVERED

**Response** (200 OK):
```json
{
  "id": "ship-001",
  "status": "DELIVERED",
  "updatedAt": "2024-01-20T10:00:00Z"
}
```

---

## 7. Notification Service API

Base path: `/api/notifications`

### 7.1 Get Notifications

**GET** `/api/notifications`

Get notifications for current user (mock - returns logged notifications).

**Response** (200 OK):
```json
{
  "notifications": [
    {
      "id": "notif-001",
      "type": "ORDER_CREATED",
      "title": "Order Confirmed",
      "message": "Your order #order-001 has been confirmed",
      "read": false,
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ]
}
```

---

## 8. Health Check Endpoints

All services expose health endpoints via Spring Actuator:

### 8.1 Health Check

**GET** `/actuator/health`

**Response** (200 OK):
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### 8.2 Liveness Probe

**GET** `/actuator/health/liveness`

### 8.3 Readiness Probe

**GET** `/actuator/health/readiness`

---

## 9. API Summary Table

| Service | Method | Endpoint | Auth | Description |
|---------|--------|----------|------|-------------|
| Auth | POST | /api/auth/register | No | Register user |
| Auth | POST | /api/auth/login | No | Login |
| Auth | GET | /api/auth/validate | Yes | Validate token |
| Auth | GET | /api/auth/me | Yes | Get current user |
| Catalog | GET | /api/products | No | List products |
| Catalog | GET | /api/products/{id} | No | Get product |
| Catalog | POST | /api/products | Admin | Create product |
| Catalog | PUT | /api/products/{id} | Admin | Update product |
| Catalog | DELETE | /api/products/{id} | Admin | Delete product |
| Inventory | GET | /api/inventory | Yes | List inventory |
| Inventory | GET | /api/inventory/{productId} | Yes | Get product inventory |
| Inventory | POST | /api/inventory/reserve | Internal | Reserve stock |
| Inventory | POST | /api/inventory/release | Internal | Release stock |
| Inventory | PUT | /api/inventory/{productId} | Admin | Update inventory |
| Order | POST | /api/orders | Yes | Create order |
| Order | GET | /api/orders | Yes | List my orders |
| Order | GET | /api/orders/{id} | Yes | Get order |
| Order | POST | /api/orders/{id}/cancel | Yes | Cancel order |
| Order | POST | /api/orders/{id}/pay | Yes | Pay for order |
| Payment | POST | /api/payments | Internal | Process payment |
| Payment | GET | /api/payments/order/{orderId} | Yes | Get payment |
| Shipping | POST | /api/shipments | Internal | Create shipment |
| Shipping | GET | /api/shipments/order/{orderId} | Yes | Get shipment |
| Shipping | PUT | /api/shipments/{id}/status | Admin | Update status |
| Notification | GET | /api/notifications | Yes | Get notifications |
