# Order Management System - Test Strategy

## 1. Overview

This document outlines the testing strategy for the Order Management System (OMS), covering unit tests, integration tests, and end-to-end testing approaches.

## 2. Test Pyramid

```
         /\
        /  \
       / E2E\      (Manual + Automated)
      /------\
     /  Integ \    (API/Contract Tests)
    /----------\
   /   Unit     \  (Component Tests)
  /--------------\
```

### Distribution Goals
- **Unit Tests**: 70% of test effort
- **Integration Tests**: 20% of test effort
- **E2E Tests**: 10% of test effort

## 3. Unit Testing

### 3.1 Framework & Tools
- **Framework**: JUnit 5
- **Mocking**: Mockito
- **Assertions**: AssertJ
- **Coverage**: JaCoCo

### 3.2 Coverage Targets

| Component | Target Coverage |
|-----------|----------------|
| Service Layer | 80% |
| Controller Layer | 70% |
| Repository Layer | 60% |
| DTOs/Entities | 50% |
| Overall | 70% |

### 3.3 Unit Test Conventions

```java
@Test
void methodName_scenario_expectedBehavior() {
    // Given - setup
    // When - action
    // Then - assertion
}
```

### 3.4 Example Unit Test

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private InventoryClient inventoryClient;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    void createOrder_validRequest_createsOrder() {
        // Given
        CreateOrderRequest request = buildValidRequest();
        when(inventoryClient.reserveStock(any())).thenReturn(successResponse());
        
        // When
        OrderResponse result = orderService.createOrder(request, "user-id");
        
        // Then
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    void createOrder_insufficientStock_throwsException() {
        // Given
        CreateOrderRequest request = buildValidRequest();
        when(inventoryClient.reserveStock(any())).thenReturn(failureResponse());
        
        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(request, "user-id"))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Failed to reserve stock");
    }
}
```

## 4. Integration Testing

### 4.1 Framework & Tools
- **Framework**: Spring Boot Test
- **Database**: H2 (in-memory)
- **API Testing**: MockMvc
- **Container Testing**: Testcontainers (optional)

### 4.2 Integration Test Scope

| Test Type | Scope |
|-----------|-------|
| Repository Tests | Database queries |
| Controller Tests | REST API endpoints |
| Service Integration | Cross-service calls |

### 4.3 Example Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createOrder_validRequest_returns201() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
            .items(List.of(new OrderItemRequest("product-id", 2)))
            .shippingAddress(buildAddress())
            .build();
        
        mockMvc.perform(post("/orders")
                .header("X-User-Id", "user-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }
}
```

### 4.4 Test Profiles

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## 5. API Contract Testing

### 5.1 Approach
- Document API contracts in OpenAPI format
- Validate responses against schema
- Mock external services for isolation

### 5.2 Contract Validation

```java
@Test
void getProducts_responseMatchesContract() throws Exception {
    mockMvc.perform(get("/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.content[0].id").exists())
        .andExpect(jsonPath("$.data.content[0].name").exists())
        .andExpect(jsonPath("$.data.content[0].price").isNumber());
}
```

## 6. End-to-End Testing

### 6.1 E2E Test Scenarios

| Scenario | Steps |
|----------|-------|
| User Registration | Register → Verify email → Login |
| Complete Order | Login → Browse → Add to Cart → Checkout → Pay |
| Order Cancellation | Login → View Orders → Cancel Pending Order |

### 6.2 Manual Test Checklist

- [ ] User can register with valid email
- [ ] User can login with credentials
- [ ] Products display with prices and stock
- [ ] User can add products to cart
- [ ] User can place order
- [ ] User can view order history
- [ ] User can cancel pending order
- [ ] User can pay for confirmed order
- [ ] Admin can create products
- [ ] Admin can update inventory

## 7. Performance Testing

### 7.1 Targets

| Metric | Target |
|--------|--------|
| API Response Time (p95) | < 500ms |
| Concurrent Users | 100 |
| Throughput | 100 req/sec |

### 7.2 Load Test Scenarios

1. **Browse Products**: GET /products (100 concurrent)
2. **Create Order**: POST /orders (50 concurrent)
3. **Mixed Workload**: 70% reads, 30% writes

## 8. Security Testing

### 8.1 Security Test Cases

| Category | Test |
|----------|------|
| Authentication | Invalid token rejection |
| Authorization | Role-based access enforcement |
| Input Validation | SQL injection prevention |
| Data Protection | No sensitive data in logs |

### 8.2 Example Security Tests

```java
@Test
void protectedEndpoint_noToken_returns401() throws Exception {
    mockMvc.perform(get("/orders"))
        .andExpect(status().isUnauthorized());
}

@Test
void adminEndpoint_customerRole_returns403() throws Exception {
    mockMvc.perform(post("/products")
            .header("X-User-Roles", "CUSTOMER")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isForbidden());
}
```

## 9. Test Environment

### 9.1 Environments

| Environment | Purpose | Data |
|-------------|---------|------|
| Local | Development | H2 in-memory |
| Test | CI/CD | H2 in-memory |
| Staging | Integration | PostgreSQL (seeded) |
| Production | Live | PostgreSQL |

### 9.2 Test Data

Demo data is automatically seeded:
- Admin user: admin@oms.com / admin123
- 6 sample products
- Inventory for each product

## 10. CI/CD Integration

### 10.1 Test Pipeline

```yaml
test:
  stage: test
  script:
    - mvn test
    - mvn verify
  coverage:
    report:
      - target/site/jacoco/index.html
```

### 10.2 Quality Gates

| Gate | Threshold |
|------|-----------|
| Unit Test Coverage | >= 70% |
| Integration Tests | Pass |
| Security Scan | No High/Critical |

## 11. Test Reporting

### 11.1 Reports Generated
- JUnit XML reports
- JaCoCo coverage reports
- Surefire test reports

### 11.2 Report Locations
- `target/surefire-reports/` - Test results
- `target/site/jacoco/` - Coverage report

## 12. Running Tests

### 12.1 Unit Tests
```bash
cd backend
mvn test
```

### 12.2 Integration Tests
```bash
cd backend
mvn verify
```

### 12.3 Single Service
```bash
cd backend/auth-service
mvn test
```

### 12.4 With Coverage
```bash
mvn test jacoco:report
```
