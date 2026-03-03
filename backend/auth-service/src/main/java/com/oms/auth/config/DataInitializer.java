package com.oms.auth.config;

import com.oms.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    @Profile("!test")
    public CommandLineRunner initData(AuthService authService) {
        return args -> {
            log.info("Initializing demo data...");
            authService.createAdminUser("admin@oms.com", "admin123", "System Admin");
            log.info("Demo data initialized");
        };
    }
}
