package com.oms.order.service;

import com.oms.common.client.dto.PaymentInfo;
import com.oms.common.client.dto.ProductInfo;
import com.oms.common.client.dto.ReserveStockRequest.ReserveStockItem;
import com.oms.common.client.dto.ReserveStockResponse;
import com.oms.common.client.dto.ShipmentInfo;
import com.oms.common.dto.PageResponse;
import com.oms.common.exception.ApiException;
import com.oms.order.client.*;
import com.oms.order.dto.*;
import com.oms.order.entity.Order;
import com.oms.order.entity.OrderItem;
import com.oms.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final ShippingClient shippingClient;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userId) {
        log.info("Creating order for user: {}", userId);

        Order order = Order.builder()
                .userId(UUID.fromString(userId))
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .shippingAddress(request.getShippingAddress() != null ? 
                        request.getShippingAddress().toString() : null)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            ProductInfo product = catalogClient.getProduct(itemRequest.getProductId());
            
            if (product == null) {
                throw ApiException.badRequest("Product not found: " + itemRequest.getProductId());
            }

            if (!product.isActive()) {
                throw ApiException.badRequest("Product is not available: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .productId(UUID.fromString(itemRequest.getProductId()))
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            order.addItem(item);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        order = orderRepository.save(order);

        List<ReserveStockItem> reserveItems = request.getItems().stream()
                .map(i -> new ReserveStockItem(i.getProductId(), i.getQuantity()))
                .collect(Collectors.toList());

        ReserveStockResponse reserveResponse = inventoryClient.reserveStock(
                order.getId().toString(), reserveItems);

        if (reserveResponse == null || !reserveResponse.isSuccess()) {
            orderRepository.delete(order);
            throw ApiException.badRequest("Failed to reserve stock: " + 
                    (reserveResponse != null ? reserveResponse.getError() : "Unknown error"));
        }

        order.setStatus(Order.OrderStatus.CONFIRMED);
        order = orderRepository.save(order);

        log.info("Order created successfully: {}", order.getId());
        return OrderResponse.from(order);
    }

    public PageResponse<OrderSummaryResponse> getOrders(String userId, String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 100), 
                Sort.by("createdAt").descending());

        Page<Order> orderPage;
        if (status != null && !status.isEmpty()) {
            orderPage = orderRepository.findByUserIdAndStatus(
                    UUID.fromString(userId), 
                    Order.OrderStatus.valueOf(status), 
                    pageRequest);
        } else {
            orderPage = orderRepository.findByUserId(UUID.fromString(userId), pageRequest);
        }

        List<OrderSummaryResponse> orders = orderPage.getContent().stream()
                .map(OrderSummaryResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(orders, page, size, orderPage.getTotalElements());
    }

    public OrderResponse getOrder(String orderId, String userId, boolean isAdmin) {
        Order order = orderRepository.findByIdWithItems(UUID.fromString(orderId))
                .orElseThrow(() -> ApiException.notFound("Order not found"));

        if (!isAdmin && !order.getUserId().toString().equals(userId)) {
            throw ApiException.forbidden("Access denied");
        }

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String orderId, String userId) {
        Order order = orderRepository.findByIdWithItems(UUID.fromString(orderId))
                .orElseThrow(() -> ApiException.notFound("Order not found"));

        if (!order.getUserId().toString().equals(userId)) {
            throw ApiException.forbidden("Access denied");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw ApiException.badRequest("Order cannot be cancelled in current status: " + order.getStatus());
        }

        List<ReserveStockItem> items = order.getItems().stream()
                .map(i -> new ReserveStockItem(i.getProductId().toString(), i.getQuantity()))
                .collect(Collectors.toList());

        inventoryClient.releaseStock(orderId, items);

        order.setStatus(Order.OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        log.info("Order cancelled: {}", orderId);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse processPayment(String orderId, PaymentRequest request, String userId) {
        Order order = orderRepository.findByIdWithItems(UUID.fromString(orderId))
                .orElseThrow(() -> ApiException.notFound("Order not found"));

        if (!order.getUserId().toString().equals(userId)) {
            throw ApiException.forbidden("Access denied");
        }

        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw ApiException.badRequest("Order is not in a payable status: " + order.getStatus());
        }

        PaymentInfo paymentResponse = paymentClient.processPayment(
                orderId, order.getTotalAmount(), request.getPaymentMethod());

        if (paymentResponse == null || !"SUCCESS".equals(paymentResponse.getStatus())) {
            throw ApiException.badRequest("Payment failed");
        }

        order.setPaymentTransactionId(paymentResponse.getTransactionId());
        order.setStatus(Order.OrderStatus.PAID);
        order = orderRepository.save(order);

        ShipmentInfo shipmentResponse = shippingClient.createShipment(
                orderId, order.getShippingAddress());

        if (shipmentResponse != null) {
            order.setTrackingNumber(shipmentResponse.getTrackingNumber());
            order.setStatus(Order.OrderStatus.SHIPPED);
            order = orderRepository.save(order);
        }

        log.info("Order paid and shipped: {}", orderId);
        return OrderResponse.from(order);
    }
}
