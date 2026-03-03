package com.oms.inventory.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.inventory.dto.InventoryInitRequest;
import com.oms.inventory.dto.InventoryUpdateRequest;
import com.oms.inventory.dto.ReserveRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Inventory Service E2E Integration Tests")
class InventoryControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private static String testProductId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        if (testProductId == null) {
            testProductId = UUID.randomUUID().toString();
        }
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Should initialize inventory for a product")
    void testInitializeInventory() throws Exception {
        // Given
        InventoryInitRequest request = new InventoryInitRequest();
        request.setProductId(testProductId);
        request.setQuantity(100);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<InventoryInitRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/inventory/initialize",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("status").asInt()).isEqualTo(201);
        assertThat(responseBody.get("data").get("productId").asText()).isEqualTo(testProductId);
        assertThat(responseBody.get("data").get("quantity").asInt()).isEqualTo(100);
        assertThat(responseBody.get("data").get("reservedQuantity").asInt()).isEqualTo(0);
        assertThat(responseBody.get("data").get("availableQuantity").asInt()).isEqualTo(100);
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Should get inventory by product ID")
    void testGetInventoryByProductId() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/inventory/" + testProductId,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("productId").asText()).isEqualTo(testProductId);
        assertThat(responseBody.get("quantity").asInt()).isEqualTo(100);
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Should get all inventory")
    void testGetAllInventory() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/inventory",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("data").isArray()).isTrue();
        assertThat(responseBody.get("data").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Should reserve stock successfully")
    void testReserveStockSuccess() throws Exception {
        // Given
        ReserveRequest.Item item = new ReserveRequest.Item();
        item.setProductId(testProductId);
        item.setQuantity(10);

        ReserveRequest request = new ReserveRequest();
        request.setOrderId("ORDER-E2E-001");
        request.setItems(List.of(item));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReserveRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/inventory/reserve",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("data").get("success").asBoolean()).isTrue();
        assertThat(responseBody.get("data").get("orderId").asText()).isEqualTo("ORDER-E2E-001");
        assertThat(responseBody.get("data").get("reservations").get(0).get("reserved").asBoolean()).isTrue();

        // Verify inventory was updated
        ResponseEntity<String> inventoryResponse = restTemplate.getForEntity(
                baseUrl + "/inventory/" + testProductId,
                String.class
        );
        JsonNode inventoryBody = objectMapper.readTree(inventoryResponse.getBody());
        assertThat(inventoryBody.get("reservedQuantity").asInt()).isEqualTo(10);
        assertThat(inventoryBody.get("availableQuantity").asInt()).isEqualTo(90);
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Should fail to reserve insufficient stock")
    void testReserveStockInsufficientQuantity() throws Exception {
        // Given - try to reserve more than available
        ReserveRequest.Item item = new ReserveRequest.Item();
        item.setProductId(testProductId);
        item.setQuantity(1000); // More than available

        ReserveRequest request = new ReserveRequest();
        request.setOrderId("ORDER-E2E-002");
        request.setItems(List.of(item));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReserveRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/inventory/reserve",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Should release reserved stock")
    void testReleaseStock() throws Exception {
        // Given
        ReserveRequest.Item item = new ReserveRequest.Item();
        item.setProductId(testProductId);
        item.setQuantity(5); // Release part of the reserved quantity

        ReserveRequest request = new ReserveRequest();
        request.setOrderId("ORDER-E2E-001");
        request.setItems(List.of(item));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReserveRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/inventory/release",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("data").get("success").asBoolean()).isTrue();

        // Verify inventory was updated
        ResponseEntity<String> inventoryResponse = restTemplate.getForEntity(
                baseUrl + "/inventory/" + testProductId,
                String.class
        );
        JsonNode inventoryBody = objectMapper.readTree(inventoryResponse.getBody());
        assertThat(inventoryBody.get("reservedQuantity").asInt()).isEqualTo(5); // 10 - 5 = 5
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Should update inventory as admin")
    void testUpdateInventoryAsAdmin() throws Exception {
        // Given
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .quantity(200)
                .reason("E2E Test - Restocking")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Roles", "ADMIN");

        HttpEntity<InventoryUpdateRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/inventory/" + testProductId,
                HttpMethod.PUT,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("data").get("quantity").asInt()).isEqualTo(200);
    }

    @Test
    @Order(8)
    @DisplayName("E2E: Should fail to update inventory as non-admin")
    void testUpdateInventoryAsNonAdmin() throws Exception {
        // Given
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .quantity(999)
                .reason("Hacking attempt")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Roles", "CUSTOMER");

        HttpEntity<InventoryUpdateRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/inventory/" + testProductId,
                HttpMethod.PUT,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(9)
    @DisplayName("E2E: Should return 404 for non-existent product inventory")
    void testGetNonExistentInventory() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/inventory/00000000-0000-0000-0000-000000000000",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(10)
    @DisplayName("E2E: Should handle multiple item reservation")
    void testReserveMultipleItems() throws Exception {
        // Given - create another product inventory
        String secondProductId = UUID.randomUUID().toString();
        
        InventoryInitRequest initRequest = new InventoryInitRequest();
        initRequest.setProductId(secondProductId);
        initRequest.setQuantity(50);
        
        HttpHeaders initHeaders = new HttpHeaders();
        initHeaders.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(baseUrl + "/inventory/initialize", 
                new HttpEntity<>(initRequest, initHeaders), String.class);

        // Reserve from both products
        ReserveRequest.Item item1 = new ReserveRequest.Item();
        item1.setProductId(testProductId);
        item1.setQuantity(5);

        ReserveRequest.Item item2 = new ReserveRequest.Item();
        item2.setProductId(secondProductId);
        item2.setQuantity(10);

        ReserveRequest request = new ReserveRequest();
        request.setOrderId("ORDER-E2E-MULTI");
        request.setItems(List.of(item1, item2));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReserveRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/inventory/reserve",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("data").get("success").asBoolean()).isTrue();
        assertThat(responseBody.get("data").get("reservations").size()).isEqualTo(2);
    }
}
