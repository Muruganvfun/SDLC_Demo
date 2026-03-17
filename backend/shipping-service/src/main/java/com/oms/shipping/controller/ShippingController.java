package com.oms.shipping.controller;

import com.oms.common.dto.ApiResponse;
import com.oms.common.security.UserContext;
import com.oms.shipping.dto.ShipmentRequest;
import com.oms.shipping.dto.ShipmentResponse;
import com.oms.shipping.dto.UpdateStatusRequest;
import com.oms.shipping.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShipmentResponse>> createShipment(
            @Valid @RequestBody ShipmentRequest request) {
        log.info("Creating shipment for order: {}", request.getOrderId());
        ShipmentResponse response = shippingService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentByOrder(
            @PathVariable String orderId) {
        log.info("Getting shipment for order: {}", orderId);
        ShipmentResponse response = shippingService.getShipmentByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentByTracking(
            @PathVariable String trackingNumber) {
        log.info("Getting shipment by tracking: {}", trackingNumber);
        ShipmentResponse response = shippingService.getShipmentByTracking(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request,
            UserContext userContext) {
        log.info("Updating shipment {} status to: {}", id, request.getStatus());
        ShipmentResponse response = shippingService.updateStatus(id, request, userContext.isAdmin());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
