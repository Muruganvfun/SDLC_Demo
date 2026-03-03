# Order Management System - Runbook

## 1. Prerequisites

### 1.1 Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| Java JDK | 17+ | Backend runtime |
| Maven | 3.9+ | Build tool |
| Node.js | 20+ | Frontend build |
| npm | 10+ | Package manager |
| Docker | 24+ | Containerization |
| Docker Compose | 2.x | Local orchestration |
| Apache Tomcat | 10.x | External WAR deployment (optional) |

### 1.2 Verify Installation

```bash
java -version    # Should show 17+
mvn -version     # Should show 3.9+
node -version    # Should show 20+
npm -version     # Should show 10+
docker --version # Should show 24+
docker-compose --version
```

---

## 2. Backend Setup & Deployment

### 2.1 Build Backend (All Services)

```bash
cd SDLC_Demo/backend
mvn clean install -DskipTests
```

This builds:
- `common` - Shared library (DTOs, exceptions, JWT utilities)
- `api-gateway` - Spring Cloud Gateway
- `auth-service`, `catalog-service`, `order-service`, `inventory-service`, `payment-service`, `shipping-service`, `notification-service`

### 2.2 Run Backend Services (Embedded Tomcat - Development)

Open separate terminal windows for each service:

```bash
# Terminal 1: Auth Service (port 8081)
cd backend/auth-service
mvn spring-boot:run

# Terminal 2: Catalog Service (port 8082)
cd backend/catalog-service
mvn spring-boot:run

# Terminal 3: Inventory Service (port 8084)
cd backend/inventory-service
mvn spring-boot:run

# Terminal 4: Order Service (port 8083)
cd backend/order-service
mvn spring-boot:run

# Terminal 5: Payment Service (port 8085)
cd backend/payment-service
mvn spring-boot:run

# Terminal 6: Shipping Service (port 8086)
cd backend/shipping-service
mvn spring-boot:run

# Terminal 7: Notification Service (port 8087)
cd backend/notification-service
mvn spring-boot:run

# Terminal 8: API Gateway (port 8080) - Start LAST
cd backend/api-gateway
mvn spring-boot:run
```

**Alternative: Run as JAR**
```bash
cd backend/auth-service
mvn package -DskipTests
java -jar target/auth-service-1.0.0-SNAPSHOT.jar
```

### 2.3 Backend Service URLs (Development)

| Service | URL | Health Check |
|---------|-----|--------------|
| API Gateway | http://localhost:8080 | http://localhost:8080/actuator/health |
| Auth Service | http://localhost:8081 | http://localhost:8081/actuator/health |
| Catalog Service | http://localhost:8082 | http://localhost:8082/actuator/health |
| Order Service | http://localhost:8083 | http://localhost:8083/actuator/health |
| Inventory Service | http://localhost:8084 | http://localhost:8084/actuator/health |
| Payment Service | http://localhost:8085 | http://localhost:8085/actuator/health |
| Shipping Service | http://localhost:8086 | http://localhost:8086/actuator/health |
| Notification Service | http://localhost:8087 | http://localhost:8087/actuator/health |

---

## 3. Frontend (React Native Web) Setup

### 3.1 Install Dependencies

```bash
cd SDLC_Demo/frontend

# Install all dependencies (includes Expo web requirements)
npm install
```

**Key web dependencies (already in package.json):**
```json
{
  "dependencies": {
    "expo": "~50.0.0",
    "react": "18.2.0",
    "react-dom": "18.2.0",
    "react-native": "0.73.2",
    "react-native-web": "~0.19.10",
    "@expo/metro-runtime": "~3.1.1"
  }
}
```

If starting from scratch or dependencies are missing:
```bash
# Install Expo web-specific dependencies
npx expo install react-dom react-native-web @expo/metro-runtime
```

### 3.2 Configure API Base URL

**File:** `frontend/app.json`
```json
{
  "expo": {
    "extra": {
      "apiBaseUrl": "http://localhost:8080/api"
    }
  }
}
```

For production, update to your deployed API URL.

### 3.3 Run Web App in Browser (Development)

```bash
cd frontend

# Option 1: Using npm script
npm run web

# Option 2: Using npx directly
npx expo start --web

# Option 3: Start Expo and press 'w' for web
npx expo start
# Then press 'w' in the terminal
```

**Expected output:**
```
Starting project at /path/to/frontend
Starting Metro Bundler
Web is waiting on http://localhost:8081

› Press w │ open web
› Press a │ open Android
› Press i │ open iOS
```

**Access the app:** http://localhost:8081 (or http://localhost:19006 depending on Expo version)

### 3.4 Development Server Options

```bash
# Clear cache and start
npx expo start --web --clear

# Specify port
npx expo start --web --port 3000

# Enable HTTPS (for testing secure features)
npx expo start --web --https
```

### 3.5 Export Production Web Build

```bash
cd frontend

# Export static web build
npx expo export --platform web

# Or using the newer command
npx expo export:web
```

**Output:** `frontend/dist/` directory containing:
```
dist/
├── index.html
├── assets/
│   ├── index-XXXXX.js
│   └── index-XXXXX.css
└── ...
```

### 3.6 Serve Production Build Locally

```bash
# Install a static server
npm install -g serve

# Serve the dist folder
serve dist

# Or use npx
npx serve dist
```

**Access:** http://localhost:3000

### 3.7 Deploy Production Build

**Option A: nginx**
```bash
# Copy dist contents to nginx html directory
cp -r dist/* /usr/share/nginx/html/
```

**nginx.conf for SPA routing:**
```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://api-gateway:8080;
    }
}
```

**Option B: Docker (included in project)**
```bash
cd frontend
docker build -t oms-frontend .
docker run -p 3000:80 oms-frontend
```

### 3.8 Frontend Environment Summary

| Mode | Command | URL | Use Case |
|------|---------|-----|----------|
| Development | `npx expo start --web` | http://localhost:8081 | Local development with hot reload |
| Production Build | `npx expo export --platform web` | N/A | Generate static files |
| Production Serve | `serve dist` | http://localhost:3000 | Test production build |
| Docker | `docker-compose up frontend` | http://localhost:3000 | Containerized deployment |

---

## 4. Docker Compose (Recommended)

### 3.1 Start All Services

```bash
cd SDLC_Demo
docker-compose up --build
```

### 3.2 Start in Background

```bash
docker-compose up -d --build
```

### 3.3 View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api-gateway
docker-compose logs -f auth-service
```

### 3.4 Stop Services

```bash
docker-compose down

# With volume cleanup
docker-compose down -v
```

### 3.5 Access Application

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080

## 5. External Tomcat Deployment (WAR)

This section covers deploying services as WAR files to an external Apache Tomcat server.

### 5.1 Prerequisites

- Apache Tomcat 10.x installed
- PostgreSQL database(s) configured
- Environment variables set

### 5.2 Build WAR Files

```bash
cd backend
mvn clean package -DskipTests
```

**WAR file locations:**
```
backend/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.war
backend/auth-service/target/auth-service-1.0.0-SNAPSHOT.war
backend/catalog-service/target/catalog-service-1.0.0-SNAPSHOT.war
backend/order-service/target/order-service-1.0.0-SNAPSHOT.war
backend/inventory-service/target/inventory-service-1.0.0-SNAPSHOT.war
backend/payment-service/target/payment-service-1.0.0-SNAPSHOT.war
backend/shipping-service/target/shipping-service-1.0.0-SNAPSHOT.war
backend/notification-service/target/notification-service-1.0.0-SNAPSHOT.war
```

### 5.3 Deploy to Tomcat

**Step 1: Copy WAR files**
```bash
# Linux/Mac
cp backend/*/target/*.war $TOMCAT_HOME/webapps/

# Windows
copy backend\*\target\*.war %TOMCAT_HOME%\webapps\
```

**Step 2: Rename for cleaner URLs (optional)**
```bash
cd $TOMCAT_HOME/webapps
mv api-gateway-1.0.0-SNAPSHOT.war api-gateway.war
mv auth-service-1.0.0-SNAPSHOT.war auth-service.war
mv catalog-service-1.0.0-SNAPSHOT.war catalog-service.war
mv order-service-1.0.0-SNAPSHOT.war order-service.war
mv inventory-service-1.0.0-SNAPSHOT.war inventory-service.war
mv payment-service-1.0.0-SNAPSHOT.war payment-service.war
mv shipping-service-1.0.0-SNAPSHOT.war shipping-service.war
mv notification-service-1.0.0-SNAPSHOT.war notification-service.war
```

**Step 3: Configure environment variables**

Create or edit `$TOMCAT_HOME/bin/setenv.sh` (Linux/Mac):
```bash
#!/bin/bash

# Spring profile
export SPRING_PROFILES_ACTIVE=production

# JWT Configuration (MUST be same across gateway and auth-service)
export JWT_SECRET=your_production_secret_key_minimum_256_bits_for_hs256

# Database URLs (each service needs its own DB)
export AUTH_DB_URL=jdbc:postgresql://localhost:5432/authdb
export AUTH_DB_USER=postgres
export AUTH_DB_PASSWORD=your_password

export CATALOG_DB_URL=jdbc:postgresql://localhost:5432/catalogdb
export CATALOG_DB_USER=postgres
export CATALOG_DB_PASSWORD=your_password

export ORDER_DB_URL=jdbc:postgresql://localhost:5432/orderdb
export ORDER_DB_USER=postgres
export ORDER_DB_PASSWORD=your_password

export INVENTORY_DB_URL=jdbc:postgresql://localhost:5432/inventorydb
export INVENTORY_DB_USER=postgres
export INVENTORY_DB_PASSWORD=your_password

export PAYMENT_DB_URL=jdbc:postgresql://localhost:5432/paymentdb
export PAYMENT_DB_USER=postgres
export PAYMENT_DB_PASSWORD=your_password

export SHIPPING_DB_URL=jdbc:postgresql://localhost:5432/shippingdb
export SHIPPING_DB_USER=postgres
export SHIPPING_DB_PASSWORD=your_password

# Service URLs (adjust based on context paths)
export SERVICES_AUTH_URL=http://localhost:8080/auth-service
export SERVICES_CATALOG_URL=http://localhost:8080/catalog-service
export SERVICES_ORDER_URL=http://localhost:8080/order-service
export SERVICES_INVENTORY_URL=http://localhost:8080/inventory-service
export SERVICES_PAYMENT_URL=http://localhost:8080/payment-service
export SERVICES_SHIPPING_URL=http://localhost:8080/shipping-service
export SERVICES_NOTIFICATION_URL=http://localhost:8080/notification-service
```

For Windows, create `%TOMCAT_HOME%\bin\setenv.bat`:
```batch
set SPRING_PROFILES_ACTIVE=production
set JWT_SECRET=your_production_secret_key_minimum_256_bits_for_hs256
rem ... other variables
```

**Step 4: Start Tomcat**
```bash
# Linux/Mac
$TOMCAT_HOME/bin/startup.sh

# Windows
%TOMCAT_HOME%\bin\startup.bat

# View logs
tail -f $TOMCAT_HOME/logs/catalina.out
```

### 5.4 WAR Context Paths

When deployed as WAR, each service is accessible at its context path:

| Service | Base URL |
|---------|----------|
| API Gateway | http://localhost:8080/api-gateway/ |
| Auth Service | http://localhost:8080/auth-service/auth/* |
| Catalog Service | http://localhost:8080/catalog-service/products/* |
| Order Service | http://localhost:8080/order-service/orders/* |
| Inventory Service | http://localhost:8080/inventory-service/inventory/* |
| Payment Service | http://localhost:8080/payment-service/payments/* |
| Shipping Service | http://localhost:8080/shipping-service/shipments/* |
| Notification Service | http://localhost:8080/notification-service/notifications/* |

**Note:** API Gateway routes need adjustment for WAR deployment context paths. Update `application-production.yml` accordingly.

### 5.5 Verify Deployment

```bash
# Check Tomcat is running
curl http://localhost:8080

# Check service health
curl http://localhost:8080/auth-service/actuator/health
curl http://localhost:8080/catalog-service/actuator/health
curl http://localhost:8080/order-service/actuator/health

# Check deployed applications
ls $TOMCAT_HOME/webapps/
```

### 5.6 Tomcat Manager (Optional)

Enable Tomcat Manager for web-based deployment:

1. Edit `$TOMCAT_HOME/conf/tomcat-users.xml`:
```xml
<role rolename="manager-gui"/>
<role rolename="manager-script"/>
<user username="admin" password="admin123" roles="manager-gui,manager-script"/>
```

2. Access: http://localhost:8080/manager/html

### 5.7 Troubleshooting Tomcat Deployment

**WAR not deploying:**
```bash
# Check logs
tail -f $TOMCAT_HOME/logs/catalina.out
tail -f $TOMCAT_HOME/logs/localhost.log

# Check permissions
ls -la $TOMCAT_HOME/webapps/
```

**Port conflict:**
```bash
# Edit $TOMCAT_HOME/conf/server.xml
# Change Connector port from 8080 to another port
<Connector port="9080" protocol="HTTP/1.1" ... />
```

**Memory issues:**
```bash
# In setenv.sh, add:
export CATALINA_OPTS="-Xms512m -Xmx2048m"
```

## 6. Environment Variables

### 5.1 Core Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | (none) |
| `JWT_SECRET` | JWT signing key | Dev key |
| `DATABASE_URL` | Database URL | H2 in-memory |
| `DATABASE_USERNAME` | DB username | sa |
| `DATABASE_PASSWORD` | DB password | (empty) |

### 5.2 Service URLs (Docker)

| Variable | Default |
|----------|---------|
| `services.catalog-url` | http://catalog-service:8082 |
| `services.inventory-url` | http://inventory-service:8084 |
| `services.payment-url` | http://payment-service:8085 |
| `services.shipping-url` | http://shipping-service:8086 |

## 7. Health Checks

### 6.1 Check Service Health

```bash
# Auth Service
curl http://localhost:8081/actuator/health

# All services via Gateway
curl http://localhost:8080/actuator/health
```

### 6.2 Expected Response

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

## 8. Demo Credentials

### 7.1 Admin User

```
Email: admin@oms.com
Password: admin123
Role: ADMIN
```

### 7.2 Customer User (Create via Registration)

```
Email: customer@example.com
Password: password123
Role: CUSTOMER
```

## 9. API Testing

### 8.1 Register User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","name":"Test User"}'
```

### 8.2 Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@oms.com","password":"admin123"}'
```

### 8.3 Get Products

```bash
curl http://localhost:8080/api/products
```

### 8.4 Create Order (with token)

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "items": [{"productId":"PRODUCT_ID","quantity":2}],
    "shippingAddress": {
      "street":"123 Main St",
      "city":"New York",
      "state":"NY",
      "zipCode":"10001",
      "country":"USA"
    }
  }'
```

## 10. Troubleshooting

### 9.1 Service Won't Start

**Problem**: Port already in use
```bash
# Find process using port
netstat -ano | findstr :8080
# Kill process
taskkill /PID <PID> /F
```

### 9.2 Database Connection Failed

**Problem**: PostgreSQL not accessible
```bash
# Check container status
docker ps
# Check logs
docker-compose logs auth-db
```

### 9.3 JWT Token Invalid

**Problem**: Token validation fails
- Ensure JWT_SECRET is same across services
- Check token expiration

### 9.4 Service Discovery Issues

**Problem**: Services can't communicate
```bash
# Check network
docker network ls
# Inspect network
docker network inspect sdlc_demo_oms-network
```

## 11. Logs

### 10.1 Log Locations (Local)

Logs output to console in development mode.

### 10.2 Log Level Configuration

```yaml
logging:
  level:
    com.oms: DEBUG
    org.springframework: INFO
```

### 10.3 Enable SQL Logging

```yaml
spring:
  jpa:
    show-sql: true
```

## 12. Database Management

### 11.1 H2 Console (Development)

Access at: http://localhost:808X/h2-console

- JDBC URL: `jdbc:h2:mem:authdb`
- Username: `sa`
- Password: (empty)

### 11.2 PostgreSQL (Docker)

```bash
# Connect to database
docker exec -it sdlc_demo_auth-db_1 psql -U postgres -d authdb

# List tables
\dt

# Query users
SELECT * FROM users;
```

## 13. Scaling (Production)

### 12.1 Horizontal Scaling

```yaml
# docker-compose.yml
services:
  catalog-service:
    deploy:
      replicas: 3
```

### 12.2 Load Balancing

Configure nginx or cloud load balancer in front of API Gateway.
