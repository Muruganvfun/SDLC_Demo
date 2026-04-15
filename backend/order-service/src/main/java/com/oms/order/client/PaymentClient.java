package com.oms.order.client;

import com.oms.common.client.dto.PaymentInfo;
import com.oms.common.client.dto.PaymentProcessRequest;
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

    public PaymentInfo processPayment(String orderId, BigDecimal amount, String paymentMethod) {
        try {
            String url = paymentServiceUrl + "/payments";
            PaymentProcessRequest request = PaymentProcessRequest.builder()
                    .orderId(orderId)
                    .amount(amount)
                    .currency("USD")
                    .paymentMethod(paymentMethod)
                    .build();
            ApiResponse response = restTemplate.postForObject(url, request, ApiResponse.class);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Error processing payment for order {}: {}", orderId, e.getMessage());
            return null;
        }
    }

    @lombok.Data
    private static class ApiResponse {
        private PaymentInfo data;
    }
}
