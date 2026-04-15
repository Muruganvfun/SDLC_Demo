package com.oms.order.service;

import com.oms.common.client.dto.PaymentInfo;
import com.oms.common.client.dto.ProductInfo;
import com.oms.common.client.dto.ReserveStockResponse;
import com.oms.common.client.dto.ShipmentInfo;
import com.oms.common.dto.PageResponse;
import com.oms.common.exception.ApiException;
import com.oms.order.client.*;
import com.oms.order.dto.*;
import com.oms.order.entity.Order;
import com.oms.order.entity.OrderItem;
import com.oms.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private ShippingClient shippingClient;

    @InjectMocks
    private OrderService orderService;

    private UUID userId;
    private UUID orderId;
    private UUID productId;
    private Order testOrder;
    private ProductInfo testProduct;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        productId = UUID.randomUUID();

        testProduct = new ProductInfo();
        testProduct.setId(productId.toString());
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("29.99"));
        testProduct.setActive(true);

        OrderItem orderItem = OrderItem.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("29.99"))
                .subtotal(new BigDecimal("59.98"))
                .build();

        testOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(Order.OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("59.98"))
                .shippingAddress("123 Test St, Test City")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        testOrder.setItems(List.of(orderItem));
    }

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully")
        void createOrder_WithValidData_ShouldCreateOrder() {
            // Given
            CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
            itemRequest.setProductId(productId.toString());
            itemRequest.setQuantity(2);

            CreateOrderRequest request = new CreateOrderRequest();
            request.setItems(List.of(itemRequest));

            when(catalogClient.getProduct(productId.toString())).thenReturn(testProduct);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(orderId);
                order.setCreatedAt(Instant.now());
                return order;
            });
            
            ReserveStockResponse reserveResponse = new ReserveStockResponse();
            reserveResponse.setSuccess(true);
            when(inventoryClient.reserveStock(anyString(), anyList())).thenReturn(reserveResponse);

            // When
            OrderResponse response = orderService.createOrder(request, userId.toString());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("CONFIRMED");
            verify(catalogClient).getProduct(productId.toString());
            verify(inventoryClient).reserveStock(anyString(), anyList());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void createOrder_WithInvalidProduct_ShouldThrowException() {
            // Given
            CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
            itemRequest.setProductId(productId.toString());
            itemRequest.setQuantity(1);

            CreateOrderRequest request = new CreateOrderRequest();
            request.setItems(List.of(itemRequest));

            when(catalogClient.getProduct(productId.toString())).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(request, userId.toString()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("Should throw exception when product is inactive")
        void createOrder_WithInactiveProduct_ShouldThrowException() {
            // Given
            testProduct.setActive(false);

            CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
            itemRequest.setProductId(productId.toString());
            itemRequest.setQuantity(1);

            CreateOrderRequest request = new CreateOrderRequest();
            request.setItems(List.of(itemRequest));

            when(catalogClient.getProduct(productId.toString())).thenReturn(testProduct);

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(request, userId.toString()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Product is not available");
        }

        @Test
        @DisplayName("Should rollback when inventory reservation fails")
        void createOrder_WhenInventoryFails_ShouldRollback() {
            // Given
            CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
            itemRequest.setProductId(productId.toString());
            itemRequest.setQuantity(100);

            CreateOrderRequest request = new CreateOrderRequest();
            request.setItems(List.of(itemRequest));

            when(catalogClient.getProduct(productId.toString())).thenReturn(testProduct);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(orderId);
                return order;
            });

            ReserveStockResponse reserveResponse = new ReserveStockResponse();
            reserveResponse.setSuccess(false);
            reserveResponse.setError("Insufficient stock");
            when(inventoryClient.reserveStock(anyString(), anyList())).thenReturn(reserveResponse);

            // When & Then
            assertThatThrownBy(() -> orderService.createOrder(request, userId.toString()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Failed to reserve stock");

            verify(orderRepository).delete(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Get Orders Tests")
    class GetOrdersTests {

        @Test
        @DisplayName("Should return paginated orders for user")
        void getOrders_ShouldReturnPaginatedOrders() {
            // Given
            Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
            when(orderRepository.findByUserId(eq(userId), any(PageRequest.class))).thenReturn(orderPage);

            // When
            PageResponse<OrderSummaryResponse> response = orderService.getOrders(userId.toString(), null, 0, 20);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getId()).isEqualTo(orderId.toString());
        }

        @Test
        @DisplayName("Should filter orders by status")
        void getOrders_WithStatusFilter_ShouldFilterOrders() {
            // Given
            Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
            when(orderRepository.findByUserIdAndStatus(eq(userId), eq(Order.OrderStatus.CONFIRMED), any(PageRequest.class)))
                    .thenReturn(orderPage);

            // When
            PageResponse<OrderSummaryResponse> response = orderService.getOrders(userId.toString(), "CONFIRMED", 0, 20);

            // Then
            assertThat(response.getContent()).hasSize(1);
            verify(orderRepository).findByUserIdAndStatus(eq(userId), eq(Order.OrderStatus.CONFIRMED), any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("Get Order Tests")
    class GetOrderTests {

        @Test
        @DisplayName("Should return order when user is owner")
        void getOrder_AsOwner_ShouldReturnOrder() {
            // Given
            when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

            // When
            OrderResponse response = orderService.getOrder(orderId.toString(), userId.toString(), false);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(orderId.toString());
        }

        @Test
        @DisplayName("Should return order when user is admin")
        void getOrder_AsAdmin_ShouldReturnOrder() {
            // Given
            UUID differentUserId = UUID.randomUUID();
            when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

            // When
            OrderResponse response = orderService.getOrder(orderId.toString(), differentUserId.toString(), true);

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when non-owner non-admin tries to access")
        void getOrder_AsNonOwnerNonAdmin_ShouldThrowException() {
            // Given
            UUID differentUserId = UUID.randomUUID();
            when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

            // When & Then
            assertThatThrownBy(() -> orderService.getOrder(orderId.toString(), differentUserId.toString(), false))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Access denied");
        }
    }

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel confirmed order")
        void cancelOrder_WithConfirmedOrder_ShouldCancel() {
            // Given
            when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // When
            OrderResponse response = orderService.cancelOrder(orderId.toString(), userId.toString());

            // Then
            verify(inventoryClient).releaseStock(eq(orderId.toString()), anyList());
            verify(orderRepository).save(argThat(order -> 
                order.getStatus() == Order.OrderStatus.CANCELLED
            ));
        }

        @Test
        @DisplayName("Should throw exception when order already shipped")
        void cancelOrder_WithShippedOrder_ShouldThrowException() {
            // Given
            testOrder.setStatus(Order.OrderStatus.SHIPPED);
            when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

            // When & Then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId.toString(), userId.toString()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Order cannot be cancelled");
        }

        @Test
        @DisplayName("Should throw exception when different user tries to cancel")
        void cancelOrder_AsDifferentUser_ShouldThrowException() {
            // Given
            UUID differentUserId = UUID.randomUUID();
            when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

            // When & Then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId.toString(), differentUserId.toString()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Access denied");
        }
    }

    @Nested
    @DisplayName("Process Payment Tests")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Should process payment and create shipment")
        void processPayment_WithValidOrder_ShouldProcessAndShip() {
            // Given
            PaymentRequest request = PaymentRequest.builder().paymentMethod("CREDIT_CARD").build();

            PaymentInfo paymentResponse = new PaymentInfo();
            paymentResponse.setStatus("SUCCESS");
            paymentResponse.setTransactionId("TXN123");

            ShipmentInfo shipmentResponse = new ShipmentInfo();
            shipmentResponse.setTrackingNumber("TRACK123");

            when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));
            when(paymentClient.processPayment(anyString(), any(BigDecimal.class), anyString()))
                    .thenReturn(paymentResponse);
            when(shippingClient.createShipment(anyString(), anyString())).thenReturn(shipmentResponse);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // When
            OrderResponse response = orderService.processPayment(orderId.toString(), request, userId.toString());

            // Then
            verify(paymentClient).processPayment(orderId.toString(), testOrder.getTotalAmount(), "CREDIT_CARD");
            verify(shippingClient).createShipment(eq(orderId.toString()), anyString());
        }

        @Test
        @DisplayName("Should throw exception when payment fails")
        void processPayment_WhenPaymentFails_ShouldThrowException() {
            // Given
            PaymentRequest request = PaymentRequest.builder().paymentMethod("CREDIT_CARD").build();

            PaymentInfo paymentResponse = new PaymentInfo();
            paymentResponse.setStatus("FAILED");

            when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));
            when(paymentClient.processPayment(anyString(), any(BigDecimal.class), anyString()))
                    .thenReturn(paymentResponse);

            // When & Then
            assertThatThrownBy(() -> orderService.processPayment(orderId.toString(), request, userId.toString()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Payment failed");

            verify(shippingClient, never()).createShipment(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when order not in confirmed status")
        void processPayment_WithNonConfirmedOrder_ShouldThrowException() {
            // Given
            testOrder.setStatus(Order.OrderStatus.PENDING);
            PaymentRequest request = PaymentRequest.builder().paymentMethod("CREDIT_CARD").build();

            when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

            // When & Then
            assertThatThrownBy(() -> orderService.processPayment(orderId.toString(), request, userId.toString()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("not in a payable status");
        }
    }
}
