package com.oms.order.dto;

import com.oms.order.entity.Order;
import com.oms.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String userId;
    private String status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemResponse> items;
    private PaymentInfo payment;
    private ShipmentInfo shipment;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private String transactionId;
        private String status;
        private Instant paidAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipmentInfo {
        private String trackingNumber;
        private String carrier;
        private String status;
        private String estimatedDelivery;
    }

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId().toString())
                .userId(order.getUserId().toString())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(order.getItems().stream()
                        .map(OrderResponse::fromItem)
                        .collect(Collectors.toList()))
                .payment(order.getPaymentTransactionId() != null ? 
                        PaymentInfo.builder()
                                .transactionId(order.getPaymentTransactionId())
                                .status("SUCCESS")
                                .build() : null)
                .shipment(order.getTrackingNumber() != null ?
                        ShipmentInfo.builder()
                                .trackingNumber(order.getTrackingNumber())
                                .carrier("DemoShip")
                                .status(order.getStatus() == Order.OrderStatus.DELIVERED ? "DELIVERED" : "SHIPPED")
                                .build() : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private static OrderItemResponse fromItem(OrderItem item) {
        return OrderItemResponse.builder()
                .productId(item.getProductId().toString())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
