package com.oms.order.controller;

import com.oms.common.dto.ApiResponse;
import com.oms.common.dto.PageResponse;
import com.oms.common.security.UserContext;
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
            UserContext userContext) {
        log.info("Creating order for user: {}", userContext.getUserId());
        OrderResponse order = orderService.createOrder(request, userContext.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getOrders(
            UserContext userContext,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting orders for user: {}", userContext.getUserId());
        PageResponse<OrderSummaryResponse> orders = orderService.getOrders(userContext.getUserId(), status, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable String id,
            UserContext userContext) {
        log.info("Getting order: {} for user: {}", id, userContext.getUserId());
        OrderResponse order = orderService.getOrder(id, userContext.getUserId(), userContext.isAdmin());
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable String id,
            UserContext userContext) {
        log.info("Cancelling order: {} for user: {}", id, userContext.getUserId());
        OrderResponse order = orderService.cancelOrder(id, userContext.getUserId());
        return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled successfully"));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<OrderResponse>> payOrder(
            @PathVariable String id,
            @RequestBody(required = false) PaymentRequest request,
            UserContext userContext) {
        log.info("Processing payment for order: {}", id);
        if (request == null) {
            request = PaymentRequest.builder().paymentMethod("CREDIT_CARD").build();
        }
        OrderResponse order = orderService.processPayment(id, request, userContext.getUserId());
        return ResponseEntity.ok(ApiResponse.success(order, "Payment processed successfully"));
    }
}
