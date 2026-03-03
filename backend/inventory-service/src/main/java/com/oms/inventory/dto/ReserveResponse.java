package com.oms.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveResponse {
    private boolean success;
    private String orderId;
    private List<ReservationResult> reservations;
    private String error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationResult {
        private String productId;
        private int quantity;
        private boolean reserved;
        private String error;
    }
}
