package com.oms.order.controller;

import com.oms.common.dto.ApiResponse;
import com.oms.common.dto.PageResponse;
import com.oms.order.dto.*;
import com.oms.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Creating order for user: {}", userId);
        OrderResponse order = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getOrders(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting orders for user: {}", userId);
        PageResponse<OrderSummaryResponse> orders = orderService.getOrders(userId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Roles", defaultValue = "") String roles) {
        log.info("Getting order: {} for user: {}", id, userId);
        boolean isAdmin = roles.contains("ADMIN");
        OrderResponse order = orderService.getOrder(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Cancelling order: {} for user: {}", id, userId);
        OrderResponse order = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled successfully"));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<OrderResponse>> payOrder(
            @PathVariable String id,
            @RequestBody(required = false) PaymentRequest request,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Processing payment for order: {}", id);
        if (request == null) {
            request = PaymentRequest.builder().paymentMethod("CREDIT_CARD").build();
        }
        OrderResponse order = orderService.processPayment(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(order, "Payment processed successfully"));
    }
}
