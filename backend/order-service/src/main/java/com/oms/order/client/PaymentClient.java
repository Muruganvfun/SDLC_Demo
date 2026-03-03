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
public class PaymentClient {

    private final RestTemplate restTemplate;

    @Value("${services.payment-url:http://localhost:8085}")
    private String paymentServiceUrl;

    public PaymentResponse processPayment(String orderId, BigDecimal amount, String paymentMethod) {
        try {
            String url = paymentServiceUrl + "/payments";
            PaymentRequest request = new PaymentRequest(orderId, amount, "USD", paymentMethod);
            ApiResponse response = restTemplate.postForObject(url, request, ApiResponse.class);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Error processing payment for order {}: {}", orderId, e.getMessage());
            return null;
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PaymentRequest {
        private String orderId;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
    }

    @lombok.Data
    public static class ApiResponse {
        private PaymentResponse data;
    }

    @lombok.Data
    public static class PaymentResponse {
        private String id;
        private String orderId;
        private String transactionId;
        private BigDecimal amount;
        private String status;
    }
}
