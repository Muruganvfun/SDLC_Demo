package com.oms.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingClient {

    private final RestTemplate restTemplate;

    @Value("${services.shipping-url:http://localhost:8086}")
    private String shippingServiceUrl;

    public ShipmentResponse createShipment(String orderId, String address) {
        try {
            String url = shippingServiceUrl + "/shipments";
            ShipmentRequest request = new ShipmentRequest(orderId, address);
            ApiResponse response = restTemplate.postForObject(url, request, ApiResponse.class);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Error creating shipment for order {}: {}", orderId, e.getMessage());
            return null;
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ShipmentRequest {
        private String orderId;
        private String address;
    }

    @lombok.Data
    public static class ApiResponse {
        private ShipmentResponse data;
    }

    @lombok.Data
    public static class ShipmentResponse {
        private String id;
        private String orderId;
        private String trackingNumber;
        private String carrier;
        private String status;
    }
}
