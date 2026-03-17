package com.oms.auth.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.auth.dto.LoginRequest;
import com.oms.auth.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Auth Service E2E Integration Tests")
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private RestTemplate restTemplate;
    private static String authToken;
    private static String userId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(HttpStatusCode statusCode) {
                return false;
            }
        });
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Should register a new user successfully")
    void testRegisterUser() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("e2etest@example.com");
        request.setPassword("E2ETest123!");
        request.setName("E2E Test User");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RegisterRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("status").asInt()).isEqualTo(201);
        assertThat(responseBody.get("message").asText()).isEqualTo("Created");
        assertThat(responseBody.get("data").get("email").asText()).isEqualTo("e2etest@example.com");
        assertThat(responseBody.get("data").get("name").asText()).isEqualTo("E2E Test User");
        assertThat(responseBody.get("data").get("role").asText()).isEqualTo("CUSTOMER");
        
        userId = responseBody.get("data").get("id").asText();
        assertThat(userId).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Should fail to register duplicate email")
    void testRegisterDuplicateEmail() throws Exception {
        // Given - same email as previous test
        RegisterRequest request = new RegisterRequest();
        request.setEmail("e2etest@example.com");
        request.setPassword("AnotherPass123!");
        request.setName("Another User");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RegisterRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("e2etest@example.com");
        request.setPassword("E2ETest123!");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("status").asInt()).isEqualTo(200);
        assertThat(responseBody.get("data").get("accessToken").asText()).isNotEmpty();
        assertThat(responseBody.get("data").get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(responseBody.get("data").get("expiresIn").asInt()).isGreaterThan(0);
        assertThat(responseBody.get("data").get("user").get("email").asText()).isEqualTo("e2etest@example.com");
        
        authToken = responseBody.get("data").get("accessToken").asText();
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Should fail login with invalid password")
    void testLoginInvalidPassword() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("e2etest@example.com");
        request.setPassword("WrongPassword!");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Should fail login with non-existent user")
    void testLoginNonExistentUser() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("SomePassword123!");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Should validate token successfully")
    void testValidateToken() throws Exception {
        // Given - use token from login test
        assertThat(authToken).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/auth/validate",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("valid").asBoolean()).isTrue();
        assertThat(responseBody.get("email").asText()).isEqualTo("e2etest@example.com");
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Should reject invalid token")
    void testValidateInvalidToken() throws Exception {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid-token-here");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/auth/validate",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(8)
    @DisplayName("E2E: Should get current user with valid token")
    void testGetCurrentUser() throws Exception {
        // Given
        assertThat(authToken).isNotNull();
        assertThat(userId).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/auth/me",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        assertThat(responseBody.get("data").get("email").asText()).isEqualTo("e2etest@example.com");
        assertThat(responseBody.get("data").get("name").asText()).isEqualTo("E2E Test User");
    }
}
