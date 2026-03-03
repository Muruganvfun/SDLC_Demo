package com.oms.inventory.controller;

import com.oms.common.dto.ApiResponse;
import com.oms.inventory.dto.*;
import com.oms.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventory() {
        log.info("Getting all inventory");
        List<InventoryResponse> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String productId) {
        log.info("Getting inventory for product: {}", productId);
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @PostMapping("/initialize")
    public ResponseEntity<ApiResponse<InventoryResponse>> initializeInventory(
            @Valid @RequestBody InventoryInitRequest request) {
        log.info("Initializing inventory for product: {}", request.getProductId());
        InventoryResponse response = inventoryService.initializeInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<ReserveResponse>> reserveStock(
            @Valid @RequestBody ReserveRequest request) {
        log.info("Reserving stock for order: {}", request.getOrderId());
        ReserveResponse response = inventoryService.reserveStock(request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Failed to reserve stock"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/release")
    public ResponseEntity<ApiResponse<ReserveResponse>> releaseStock(
            @Valid @RequestBody ReserveRequest request) {
        log.info("Releasing stock for order: {}", request.getOrderId());
        ReserveResponse response = inventoryService.releaseStock(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
            @PathVariable String productId,
            @Valid @RequestBody InventoryUpdateRequest request,
            @RequestHeader(value = "X-User-Roles", defaultValue = "") String roles) {
        log.info("Updating inventory for product: {}", productId);
        boolean isAdmin = roles.contains("ADMIN");
        InventoryResponse response = inventoryService.updateInventory(productId, request, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
