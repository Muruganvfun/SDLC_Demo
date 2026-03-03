package com.oms.catalog.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.catalog.dto.ProductRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Catalog Service E2E Integration Tests")
class ProductControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private static String createdProductId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Should get all products (with seeded data)")
    void testGetAllProducts() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/products",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("status").asInt()).isEqualTo(200);
        assertThat(responseBody.get("data").get("content").isArray()).isTrue();
        assertThat(responseBody.get("data").get("totalElements").asInt()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Should get products with pagination")
    void testGetProductsWithPagination() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/products?page=0&size=2",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("data").get("page").asInt()).isEqualTo(0);
        assertThat(responseBody.get("data").get("size").asInt()).isEqualTo(2);
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Should create product as admin")
    void testCreateProductAsAdmin() throws Exception {
        // Given
        ProductRequest request = new ProductRequest();
        request.setName("E2E Test Product");
        request.setDescription("Product created during E2E testing");
        request.setPrice(new BigDecimal("99.99"));
        request.setImageUrl("/images/e2e-test.jpg");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Roles", "ADMIN");

        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/products",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("status").asInt()).isEqualTo(201);
        assertThat(responseBody.get("data").get("name").asText()).isEqualTo("E2E Test Product");
        assertThat(responseBody.get("data").get("price").asDouble()).isEqualTo(99.99);
        assertThat(responseBody.get("data").get("active").asBoolean()).isTrue();
        
        createdProductId = responseBody.get("data").get("id").asText();
        assertThat(createdProductId).isNotEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Should fail to create product as non-admin")
    void testCreateProductAsNonAdmin() throws Exception {
        // Given
        ProductRequest request = new ProductRequest();
        request.setName("Unauthorized Product");
        request.setDescription("Should not be created");
        request.setPrice(new BigDecimal("50.00"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Roles", "CUSTOMER");

        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/products",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Should get product by ID")
    void testGetProductById() throws Exception {
        // Given
        assertThat(createdProductId).isNotNull();

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/products/" + createdProductId,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("data").get("id").asText()).isEqualTo(createdProductId);
        assertThat(responseBody.get("data").get("name").asText()).isEqualTo("E2E Test Product");
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Should return 404 for non-existent product")
    void testGetNonExistentProduct() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/products/00000000-0000-0000-0000-000000000000",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Should update product as admin")
    void testUpdateProductAsAdmin() throws Exception {
        // Given
        assertThat(createdProductId).isNotNull();

        ProductRequest request = new ProductRequest();
        request.setName("Updated E2E Product");
        request.setPrice(new BigDecimal("149.99"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Roles", "ADMIN");

        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/products/" + createdProductId,
                HttpMethod.PUT,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("data").get("name").asText()).isEqualTo("Updated E2E Product");
        assertThat(responseBody.get("data").get("price").asDouble()).isEqualTo(149.99);
    }

    @Test
    @Order(8)
    @DisplayName("E2E: Should fail to update product as non-admin")
    void testUpdateProductAsNonAdmin() throws Exception {
        // Given
        assertThat(createdProductId).isNotNull();

        ProductRequest request = new ProductRequest();
        request.setName("Hacked Product");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Roles", "CUSTOMER");

        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/products/" + createdProductId,
                HttpMethod.PUT,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(9)
    @DisplayName("E2E: Should soft-delete product as admin")
    void testDeleteProductAsAdmin() throws Exception {
        // Given
        assertThat(createdProductId).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Roles", "ADMIN");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/products/" + createdProductId,
                HttpMethod.DELETE,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify product is soft-deleted (still exists but inactive)
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                baseUrl + "/products/" + createdProductId,
                String.class
        );
        
        // Product should still be retrievable by ID
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(10)
    @DisplayName("E2E: Should fail to delete product as non-admin")
    void testDeleteProductAsNonAdmin() throws Exception {
        // Given - create another product first
        ProductRequest createRequest = new ProductRequest();
        createRequest.setName("Product to Fail Delete");
        createRequest.setDescription("Test");
        createRequest.setPrice(new BigDecimal("10.00"));

        HttpHeaders createHeaders = new HttpHeaders();
        createHeaders.setContentType(MediaType.APPLICATION_JSON);
        createHeaders.set("X-User-Roles", "ADMIN");

        HttpEntity<ProductRequest> createEntity = new HttpEntity<>(createRequest, createHeaders);
        ResponseEntity<String> createResponse = restTemplate.postForEntity(
                baseUrl + "/products", createEntity, String.class);
        
        JsonNode createBody = objectMapper.readTree(createResponse.getBody());
        String productId = createBody.get("data").get("id").asText();

        // Try to delete as non-admin
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Roles", "CUSTOMER");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/products/" + productId,
                HttpMethod.DELETE,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
