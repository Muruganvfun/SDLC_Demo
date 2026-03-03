package com.oms.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogClient {

    private final RestTemplate restTemplate;

    @Value("${services.catalog-url:http://localhost:8082}")
    private String catalogServiceUrl;

    public ProductInfo getProduct(String productId) {
        try {
            String url = catalogServiceUrl + "/products/" + productId;
            ProductResponse response = restTemplate.getForObject(url, ProductResponse.class);
            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
        }
        return null;
    }

    @lombok.Data
    public static class ProductResponse {
        private ProductInfo data;
    }

    @lombok.Data
    public static class ProductInfo {
        private String id;
        private String name;
        private String description;
        private BigDecimal price;
        private boolean active;
        private Boolean inStock;
    }
}
