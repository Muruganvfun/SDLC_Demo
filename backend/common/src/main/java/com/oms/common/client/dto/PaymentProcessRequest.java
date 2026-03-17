package com.oms.common.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessRequest {
    private String orderId;
    private BigDecimal amount;
    @Builder.Default
    private String currency = "USD";
    private String paymentMethod;
}
