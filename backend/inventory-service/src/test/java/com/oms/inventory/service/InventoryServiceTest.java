package com.oms.inventory.service;

import com.oms.common.exception.ApiException;
import com.oms.inventory.dto.*;
import com.oms.inventory.entity.Inventory;
import com.oms.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private UUID productId;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testInventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(100)
                .reservedQuantity(10)
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Get Inventory Tests")
    class GetInventoryTests {

        @Test
        @DisplayName("Should return inventory when found")
        void getInventory_WhenFound_ShouldReturnInventory() {
            // Given
            when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(testInventory));

            // When
            InventoryResponse response = inventoryService.getInventory(productId.toString());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getProductId()).isEqualTo(productId.toString());
            assertThat(response.getQuantity()).isEqualTo(100);
            assertThat(response.getReservedQuantity()).isEqualTo(10);
            assertThat(response.getAvailableQuantity()).isEqualTo(90);
        }

        @Test
        @DisplayName("Should throw exception when inventory not found")
        void getInventory_WhenNotFound_ShouldThrowException() {
            // Given
            when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> inventoryService.getInventory(productId.toString()))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Inventory not found");
        }

        @Test
        @DisplayName("Should return all inventory")
        void getAllInventory_ShouldReturnAllItems() {
            // Given
            when(inventoryRepository.findAll()).thenReturn(List.of(testInventory));

            // When
            List<InventoryResponse> response = inventoryService.getAllInventory();

            // Then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).getProductId()).isEqualTo(productId.toString());
        }
    }

    @Nested
    @DisplayName("Initialize Inventory Tests")
    class InitializeInventoryTests {

        @Test
        @DisplayName("Should initialize new inventory")
        void initializeInventory_WhenNew_ShouldCreate() {
            // Given
            InventoryInitRequest request = new InventoryInitRequest();
            request.setProductId(productId.toString());
            request.setQuantity(50);

            when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());
            when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
                Inventory inv = invocation.getArgument(0);
                inv.setId(UUID.randomUUID());
                return inv;
            });

            // When
            InventoryResponse response = inventoryService.initializeInventory(request);

            // Then
            assertThat(response).isNotNull();
            verify(inventoryRepository).save(argThat(inv ->
                    inv.getProductId().equals(productId) &&
                    inv.getQuantity() == 50 &&
                    inv.getReservedQuantity() == 0
            ));
        }

        @Test
        @DisplayName("Should update existing inventory on re-initialization")
        void initializeInventory_WhenExists_ShouldUpdate() {
            // Given
            InventoryInitRequest request = new InventoryInitRequest();
            request.setProductId(productId.toString());
            request.setQuantity(200);

            when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(testInventory));
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

            // When
            inventoryService.initializeInventory(request);

            // Then
            verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 200));
        }
    }

    @Nested
    @DisplayName("Reserve Stock Tests")
    class ReserveStockTests {

        @Test
        @DisplayName("Should reserve stock successfully")
        void reserveStock_WithSufficientStock_ShouldReserve() {
            // Given
            ReserveRequest.Item item = new ReserveRequest.Item();
            item.setProductId(productId.toString());
            item.setQuantity(5);

            ReserveRequest request = new ReserveRequest();
            request.setOrderId("ORDER123");
            request.setItems(List.of(item));

            when(inventoryRepository.findWithLockByProductId(productId)).thenReturn(Optional.of(testInventory));
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

            // When
            ReserveResponse response = inventoryService.reserveStock(request);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getReservations()).hasSize(1);
            assertThat(response.getReservations().get(0).isReserved()).isTrue();
        }

        @Test
        @DisplayName("Should fail when insufficient stock")
        void reserveStock_WithInsufficientStock_ShouldFail() {
            // Given
            testInventory.setQuantity(10);
            testInventory.setReservedQuantity(8);

            ReserveRequest.Item item = new ReserveRequest.Item();
            item.setProductId(productId.toString());
            item.setQuantity(5); // Only 2 available

            ReserveRequest request = new ReserveRequest();
            request.setOrderId("ORDER123");
            request.setItems(List.of(item));

            when(inventoryRepository.findWithLockByProductId(productId)).thenReturn(Optional.of(testInventory));

            // When
            ReserveResponse response = inventoryService.reserveStock(request);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getReservations().get(0).isReserved()).isFalse();
            assertThat(response.getReservations().get(0).getError()).contains("Insufficient stock");
        }

        @Test
        @DisplayName("Should fail when product not in inventory")
        void reserveStock_WithNonExistentProduct_ShouldFail() {
            // Given
            ReserveRequest.Item item = new ReserveRequest.Item();
            item.setProductId(productId.toString());
            item.setQuantity(5);

            ReserveRequest request = new ReserveRequest();
            request.setOrderId("ORDER123");
            request.setItems(List.of(item));

            when(inventoryRepository.findWithLockByProductId(productId)).thenReturn(Optional.empty());

            // When
            ReserveResponse response = inventoryService.reserveStock(request);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getReservations().get(0).getError()).contains("not found");
        }
    }

    @Nested
    @DisplayName("Release Stock Tests")
    class ReleaseStockTests {

        @Test
        @DisplayName("Should release reserved stock")
        void releaseStock_ShouldReleaseReservedQuantity() {
            // Given
            ReserveRequest.Item item = new ReserveRequest.Item();
            item.setProductId(productId.toString());
            item.setQuantity(5);

            ReserveRequest request = new ReserveRequest();
            request.setOrderId("ORDER123");
            request.setItems(List.of(item));

            when(inventoryRepository.findWithLockByProductId(productId)).thenReturn(Optional.of(testInventory));
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

            // When
            ReserveResponse response = inventoryService.releaseStock(request);

            // Then
            assertThat(response.isSuccess()).isTrue();
            verify(inventoryRepository).save(argThat(inv -> inv.getReservedQuantity() == 5)); // 10 - 5 = 5
        }
    }

    @Nested
    @DisplayName("Update Inventory Tests")
    class UpdateInventoryTests {

        @Test
        @DisplayName("Should update inventory as admin")
        void updateInventory_AsAdmin_ShouldUpdate() {
            // Given
            InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                    .quantity(150)
                    .reason("Restocking")
                    .build();

            when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(testInventory));
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

            // When
            InventoryResponse response = inventoryService.updateInventory(productId.toString(), request, true);

            // Then
            verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 150));
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to update")
        void updateInventory_AsNonAdmin_ShouldThrowException() {
            // Given
            InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                    .quantity(150)
                    .reason("Restocking")
                    .build();

            // When & Then
            assertThatThrownBy(() -> inventoryService.updateInventory(productId.toString(), request, false))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Only admins can update inventory");
        }
    }
}
