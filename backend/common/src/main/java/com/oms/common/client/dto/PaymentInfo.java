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
public class PaymentInfo {
    private String id;
    private String orderId;
    private String transactionId;
    private BigDecimal amount;
    private String status;
}
