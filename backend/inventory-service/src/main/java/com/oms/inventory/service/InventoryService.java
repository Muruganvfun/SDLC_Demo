package com.oms.inventory.service;

import com.oms.common.exception.ApiException;
import com.oms.inventory.dto.*;
import com.oms.inventory.entity.Inventory;
import com.oms.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

    public InventoryResponse getInventory(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(UUID.fromString(productId))
                .orElseThrow(() -> ApiException.notFound("Inventory not found for product: " + productId));
        return InventoryResponse.from(inventory);
    }

    @Transactional
    public InventoryResponse initializeInventory(InventoryInitRequest request) {
        UUID productId = UUID.fromString(request.getProductId());
        
        if (inventoryRepository.findByProductId(productId).isPresent()) {
            log.info("Inventory already exists for product: {}", productId);
            return updateInventory(request.getProductId(), 
                    InventoryUpdateRequest.builder()
                            .quantity(request.getQuantity())
                            .reason("Re-initialization")
                            .build(), true);
        }

        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantity(request.getQuantity())
                .reservedQuantity(0)
                .build();

        inventory = inventoryRepository.save(inventory);
        log.info("Inventory initialized for product {}: {} units", productId, request.getQuantity());

        return InventoryResponse.from(inventory);
    }

    @Transactional
    public ReserveResponse reserveStock(ReserveRequest request) {
        log.info("Reserving stock for order: {}", request.getOrderId());
        
        List<ReserveResponse.ReservationResult> results = new ArrayList<>();
        boolean allSuccess = true;

        for (ReserveRequest.Item item : request.getItems()) {
            UUID productId = UUID.fromString(item.getProductId());
            
            try {
                Inventory inventory = inventoryRepository.findWithLockByProductId(productId)
                        .orElse(null);

                if (inventory == null) {
                    results.add(ReserveResponse.ReservationResult.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .reserved(false)
                            .error("Product not found in inventory")
                            .build());
                    allSuccess = false;
                    continue;
                }

                if (!inventory.canReserve(item.getQuantity())) {
                    results.add(ReserveResponse.ReservationResult.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .reserved(false)
                            .error("Insufficient stock. Available: " + inventory.getAvailableQuantity())
                            .build());
                    allSuccess = false;
                    continue;
                }

                inventory.reserve(item.getQuantity());
                inventoryRepository.save(inventory);

                results.add(ReserveResponse.ReservationResult.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .reserved(true)
                        .build());

                log.info("Reserved {} units of product {} for order {}", 
                        item.getQuantity(), item.getProductId(), request.getOrderId());

            } catch (Exception e) {
                log.error("Error reserving stock for product {}: {}", item.getProductId(), e.getMessage());
                results.add(ReserveResponse.ReservationResult.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .reserved(false)
                        .error(e.getMessage())
                        .build());
                allSuccess = false;
            }
        }

        if (!allSuccess) {
            releaseReservedItems(results);
        }

        return ReserveResponse.builder()
                .success(allSuccess)
                .orderId(request.getOrderId())
                .reservations(results)
                .error(allSuccess ? null : "Some items could not be reserved")
                .build();
    }

    @Transactional
    public ReserveResponse releaseStock(ReserveRequest request) {
        log.info("Releasing stock for order: {}", request.getOrderId());

        List<ReserveResponse.ReservationResult> results = new ArrayList<>();

        for (ReserveRequest.Item item : request.getItems()) {
            UUID productId = UUID.fromString(item.getProductId());

            try {
                Inventory inventory = inventoryRepository.findWithLockByProductId(productId)
                        .orElse(null);

                if (inventory != null) {
                    inventory.release(item.getQuantity());
                    inventoryRepository.save(inventory);
                    log.info("Released {} units of product {} for order {}", 
                            item.getQuantity(), item.getProductId(), request.getOrderId());
                }

                results.add(ReserveResponse.ReservationResult.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .reserved(false)
                        .build());

            } catch (Exception e) {
                log.error("Error releasing stock for product {}: {}", item.getProductId(), e.getMessage());
            }
        }

        return ReserveResponse.builder()
                .success(true)
                .orderId(request.getOrderId())
                .reservations(results)
                .build();
    }

    @Transactional
    public InventoryResponse updateInventory(String productId, InventoryUpdateRequest request, boolean isAdmin) {
        if (!isAdmin) {
            throw ApiException.forbidden("Only admins can update inventory");
        }

        Inventory inventory = inventoryRepository.findByProductId(UUID.fromString(productId))
                .orElseThrow(() -> ApiException.notFound("Inventory not found for product: " + productId));

        inventory.setQuantity(request.getQuantity());
        inventory = inventoryRepository.save(inventory);

        log.info("Inventory updated for product {}: {} units. Reason: {}", 
                productId, request.getQuantity(), request.getReason());

        return InventoryResponse.from(inventory);
    }

    private void releaseReservedItems(List<ReserveResponse.ReservationResult> results) {
        for (ReserveResponse.ReservationResult result : results) {
            if (result.isReserved()) {
                try {
                    UUID productId = UUID.fromString(result.getProductId());
                    Inventory inventory = inventoryRepository.findByProductId(productId).orElse(null);
                    if (inventory != null) {
                        inventory.release(result.getQuantity());
                        inventoryRepository.save(inventory);
                    }
                } catch (Exception e) {
                    log.error("Error rolling back reservation for product {}: {}", 
                            result.getProductId(), e.getMessage());
                }
            }
        }
    }
}
