package com.oms.order.client;

import com.oms.common.client.dto.ProductInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    private static class ProductResponse {
        private ProductInfo data;
    }
}
