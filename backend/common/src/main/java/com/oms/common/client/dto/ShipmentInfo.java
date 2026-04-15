package com.oms.common.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentInfo {
    private String id;
    private String orderId;
    private String trackingNumber;
    private String carrier;
    private String status;
}
