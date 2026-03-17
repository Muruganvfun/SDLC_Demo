package com.oms.order.client;

import com.oms.common.client.dto.ReserveStockRequest;
import com.oms.common.client.dto.ReserveStockRequest.ReserveStockItem;
import com.oms.common.client.dto.ReserveStockResponse;
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

    public ReserveStockResponse reserveStock(String orderId, List<ReserveStockItem> items) {
        try {
            String url = inventoryServiceUrl + "/inventory/reserve";
            ReserveStockRequest request = new ReserveStockRequest(orderId, items);
            ApiResponse response = restTemplate.postForObject(url, request, ApiResponse.class);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Error reserving stock for order {}: {}", orderId, e.getMessage());
            return null;
        }
    }

    public void releaseStock(String orderId, List<ReserveStockItem> items) {
        try {
            String url = inventoryServiceUrl + "/inventory/release";
            ReserveStockRequest request = new ReserveStockRequest(orderId, items);
            restTemplate.postForObject(url, request, Object.class);
        } catch (Exception e) {
            log.error("Error releasing stock for order {}: {}", orderId, e.getMessage());
        }
    }

    @lombok.Data
    private static class ApiResponse {
        private ReserveStockResponse data;
    }
}
