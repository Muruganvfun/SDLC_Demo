package com.oms.order.client;

import com.oms.common.client.dto.CreateShipmentRequest;
import com.oms.common.client.dto.ShipmentInfo;
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

    public ShipmentInfo createShipment(String orderId, String address) {
        try {
            String url = shippingServiceUrl + "/shipments";
            CreateShipmentRequest request = CreateShipmentRequest.builder()
                    .orderId(orderId)
                    .address(address)
                    .build();
            ApiResponse response = restTemplate.postForObject(url, request, ApiResponse.class);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Error creating shipment for order {}: {}", orderId, e.getMessage());
            return null;
        }
    }

    @lombok.Data
    private static class ApiResponse {
        private ShipmentInfo data;
    }
}
