package com.oms.inventory.config;

import com.oms.inventory.entity.Inventory;
import com.oms.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Value("${catalog.service.url:http://localhost:8082}")
    private String catalogServiceUrl;

    @Bean
    @Profile("!test")
    public CommandLineRunner initInventory(InventoryRepository inventoryRepository) {
        return args -> {
            if (inventoryRepository.count() > 0) {
                log.info("Inventory already exists, skipping initialization");
                return;
            }

            log.info("Initializing inventory from catalog service...");
            
            try {
                Thread.sleep(5000);
                
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Map> response = restTemplate.getForEntity(
                    catalogServiceUrl + "/products", Map.class);
                
                if (response.getBody() != null && response.getBody().get("data") != null) {
                    Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                    List<Map<String, Object>> products = (List<Map<String, Object>>) data.get("content");
                    
                    for (Map<String, Object> product : products) {
                        String productIdStr = (String) product.get("id");
                        UUID productId = UUID.fromString(productIdStr);
                        Inventory inventory = Inventory.builder()
                            .productId(productId)
                            .quantity(100)
                            .reservedQuantity(0)
                            .build();
                        inventoryRepository.save(inventory);
                        log.info("Initialized inventory for product: {} with quantity: 100", productId);
                    }
                    log.info("Inventory initialization complete: {} products", products.size());
                }
            } catch (Exception e) {
                log.warn("Could not initialize inventory from catalog: {}. Inventory will be created on demand.", e.getMessage());
            }
        };
    }
}
