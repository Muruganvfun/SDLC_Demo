package com.oms.auth.service;

import com.oms.auth.dto.*;
import com.oms.auth.entity.User;
import com.oms.auth.repository.UserRepository;
import com.oms.common.exception.ApiException;
import com.oms.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(User.Role.CUSTOMER)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getId());

        return UserResponse.from(user);
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", request.getEmail());
            throw ApiException.unauthorized("Invalid credentials");
        }

        String token = jwtUtil.generateToken(
                user.getId().toString(),
                user.getEmail(),
                List.of(user.getRole().name())
        );

        log.info("User logged in successfully: {}", user.getId());
        return LoginResponse.of(token, 86400, UserResponse.from(user));
    }

    public TokenValidationResponse validateToken(String token) {
        if (token == null || token.isBlank()) {
            return TokenValidationResponse.builder().valid(false).build();
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtil.isTokenValid(token)) {
            return TokenValidationResponse.builder().valid(false).build();
        }

        return TokenValidationResponse.builder()
                .valid(true)
                .userId(jwtUtil.getUserId(token))
                .email(jwtUtil.getEmail(token))
                .roles(jwtUtil.getRoles(token))
                .build();
    }

    public UserResponse getCurrentUser(String userId) {
        UUID id = UUID.fromString(userId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        return UserResponse.from(user);
    }

    @Transactional
    public void createAdminUser(String email, String password, String name) {
        if (userRepository.existsByEmail(email.toLowerCase())) {
            log.info("Admin user already exists: {}", email);
            return;
        }

        User admin = User.builder()
                .email(email.toLowerCase())
                .passwordHash(passwordEncoder.encode(password))
                .name(name)
                .role(User.Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Admin user created: {}", email);
    }
}
