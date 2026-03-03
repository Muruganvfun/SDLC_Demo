package com.oms.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final RestTemplate restTemplate;

    @Value("${services.inventory-url:http://localhost:8084}")
    private String inventoryServiceUrl;

    public ReserveResponse reserveStock(String orderId, List<ReserveItem> items) {
        try {
            String url = inventoryServiceUrl + "/inventory/reserve";
            ReserveRequest request = new ReserveRequest(orderId, items);
            ApiResponse response = restTemplate.postForObject(url, request, ApiResponse.class);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Error reserving stock for order {}: {}", orderId, e.getMessage());
            return null;
        }
    }

    public void releaseStock(String orderId, List<ReserveItem> items) {
        try {
            String url = inventoryServiceUrl + "/inventory/release";
            ReserveRequest request = new ReserveRequest(orderId, items);
            restTemplate.postForObject(url, request, Object.class);
        } catch (Exception e) {
            log.error("Error releasing stock for order {}: {}", orderId, e.getMessage());
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ReserveRequest {
        private String orderId;
        private List<ReserveItem> items;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ReserveItem {
        private String productId;
        private int quantity;
    }

    @lombok.Data
    public static class ApiResponse {
        private ReserveResponse data;
    }

    @lombok.Data
    public static class ReserveResponse {
        private boolean success;
        private String orderId;
        private String error;
    }
}
