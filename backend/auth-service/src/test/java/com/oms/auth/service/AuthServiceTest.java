package com.oms.auth.service;

import com.oms.auth.dto.*;
import com.oms.auth.entity.User;
import com.oms.auth.repository.UserRepository;
import com.oms.common.exception.ApiException;
import com.oms.common.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .name("Test User")
                .role(User.Role.CUSTOMER)
                .createdAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void register_WithValidData_ShouldReturnUserResponse() {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("newuser@example.com");
            request.setPassword("password123");
            request.setName("New User");

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(UUID.randomUUID());
                user.setCreatedAt(Instant.now());
                return user;
            });

            // When
            UserResponse response = authService.register(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("newuser@example.com");
            assertThat(response.getName()).isEqualTo("New User");
            assertThat(response.getRole()).isEqualTo("CUSTOMER");

            verify(userRepository).existsByEmail("newuser@example.com");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_WithExistingEmail_ShouldThrowException() {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("existing@example.com");
            request.setPassword("password123");
            request.setName("Existing User");

            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Email already registered");

            verify(userRepository).existsByEmail("existing@example.com");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should convert email to lowercase")
        void register_WithMixedCaseEmail_ShouldConvertToLowercase() {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("TestUser@Example.COM");
            request.setPassword("password123");
            request.setName("Test User");

            when(userRepository.existsByEmail("testuser@example.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(UUID.randomUUID());
                user.setCreatedAt(Instant.now());
                return user;
            });

            // When
            UserResponse response = authService.register(request);

            // Then
            assertThat(response.getEmail()).isEqualTo("testuser@example.com");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_WithValidCredentials_ShouldReturnLoginResponse() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtUtil.generateToken(anyString(), anyString(), anyList())).thenReturn("jwt-token");

            // When
            LoginResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("jwt-token");
            assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

            verify(userRepository).findByEmail("test@example.com");
            verify(passwordEncoder).matches("password123", "hashedPassword");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void login_WithNonExistentUser_ShouldThrowException() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("nonexistent@example.com");
            request.setPassword("password123");

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void login_WithIncorrectPassword_ShouldThrowException() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("wrongpassword");

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyList());
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should return valid response for valid token")
        void validateToken_WithValidToken_ShouldReturnValidResponse() {
            // Given
            String token = "valid-jwt-token";
            when(jwtUtil.isTokenValid(token)).thenReturn(true);
            when(jwtUtil.getUserId(token)).thenReturn(userId.toString());
            when(jwtUtil.getEmail(token)).thenReturn("test@example.com");
            when(jwtUtil.getRoles(token)).thenReturn(List.of("CUSTOMER"));

            // When
            TokenValidationResponse response = authService.validateToken(token);

            // Then
            assertThat(response.isValid()).isTrue();
            assertThat(response.getUserId()).isEqualTo(userId.toString());
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getRoles()).contains("CUSTOMER");
        }

        @Test
        @DisplayName("Should return invalid response for invalid token")
        void validateToken_WithInvalidToken_ShouldReturnInvalidResponse() {
            // Given
            String token = "invalid-token";
            when(jwtUtil.isTokenValid(token)).thenReturn(false);

            // When
            TokenValidationResponse response = authService.validateToken(token);

            // Then
            assertThat(response.isValid()).isFalse();
        }

        @Test
        @DisplayName("Should return invalid response for null token")
        void validateToken_WithNullToken_ShouldReturnInvalidResponse() {
            // When
            TokenValidationResponse response = authService.validateToken(null);

            // Then
            assertThat(response.isValid()).isFalse();
        }

        @Test
        @DisplayName("Should handle Bearer prefix in token")
        void validateToken_WithBearerPrefix_ShouldStripPrefixAndValidate() {
            // Given
            String tokenWithBearer = "Bearer valid-jwt-token";
            when(jwtUtil.isTokenValid("valid-jwt-token")).thenReturn(true);
            when(jwtUtil.getUserId("valid-jwt-token")).thenReturn(userId.toString());
            when(jwtUtil.getEmail("valid-jwt-token")).thenReturn("test@example.com");
            when(jwtUtil.getRoles("valid-jwt-token")).thenReturn(List.of("CUSTOMER"));

            // When
            TokenValidationResponse response = authService.validateToken(tokenWithBearer);

            // Then
            assertThat(response.isValid()).isTrue();
            verify(jwtUtil).isTokenValid("valid-jwt-token");
        }
    }

    @Nested
    @DisplayName("Get Current User Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return user when found")
        void getCurrentUser_WithValidId_ShouldReturnUser() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            UserResponse response = authService.getCurrentUser(userId.toString());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(userId.toString());
            assertThat(response.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getCurrentUser_WithInvalidId_ShouldThrowException() {
            // Given
            UUID invalidId = UUID.randomUUID();
            when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.getCurrentUser(invalidId.toString()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("Create Admin User Tests")
    class CreateAdminUserTests {

        @Test
        @DisplayName("Should create admin user when not exists")
        void createAdminUser_WhenNotExists_ShouldCreateUser() {
            // Given
            when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
            when(passwordEncoder.encode("adminpass")).thenReturn("encodedAdminPass");

            // When
            authService.createAdminUser("admin@example.com", "adminpass", "Admin User");

            // Then
            verify(userRepository).save(argThat(user -> 
                user.getRole() == User.Role.ADMIN &&
                user.getEmail().equals("admin@example.com")
            ));
        }

        @Test
        @DisplayName("Should skip creation when admin already exists")
        void createAdminUser_WhenExists_ShouldSkip() {
            // Given
            when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);

            // When
            authService.createAdminUser("admin@example.com", "adminpass", "Admin User");

            // Then
            verify(userRepository, never()).save(any());
        }
    }
}
