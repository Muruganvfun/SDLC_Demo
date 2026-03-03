package com.oms.catalog.config;

import com.oms.catalog.entity.Product;
import com.oms.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    @Profile("!test")
    public CommandLineRunner initProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                log.info("Products already exist, skipping initialization");
                return;
            }

            log.info("Initializing demo products...");

            List<Product> products = List.of(
                Product.builder()
                    .name("Wireless Mouse")
                    .description("Ergonomic wireless mouse with USB receiver. Features adjustable DPI and long battery life.")
                    .price(new BigDecimal("29.99"))
                    .imageUrl("/images/mouse.jpg")
                    .build(),
                Product.builder()
                    .name("Mechanical Keyboard")
                    .description("RGB mechanical keyboard with Cherry MX switches. Full-size layout with numeric keypad.")
                    .price(new BigDecimal("89.99"))
                    .imageUrl("/images/keyboard.jpg")
                    .build(),
                Product.builder()
                    .name("USB-C Hub")
                    .description("7-in-1 USB-C hub with HDMI, USB 3.0, SD card reader, and power delivery.")
                    .price(new BigDecimal("49.99"))
                    .imageUrl("/images/hub.jpg")
                    .build(),
                Product.builder()
                    .name("Webcam HD")
                    .description("1080p HD webcam with auto-focus and built-in microphone. Perfect for video calls.")
                    .price(new BigDecimal("59.99"))
                    .imageUrl("/images/webcam.jpg")
                    .build(),
                Product.builder()
                    .name("Monitor Stand")
                    .description("Adjustable monitor stand with cable management. Supports monitors up to 32 inches.")
                    .price(new BigDecimal("39.99"))
                    .imageUrl("/images/stand.jpg")
                    .build(),
                Product.builder()
                    .name("Laptop Sleeve")
                    .description("Protective laptop sleeve for 15-inch laptops. Water-resistant neoprene material.")
                    .price(new BigDecimal("24.99"))
                    .imageUrl("/images/sleeve.jpg")
                    .build()
            );

            productRepository.saveAll(products);
            log.info("Demo products initialized: {} products created", products.size());
        };
    }
}
