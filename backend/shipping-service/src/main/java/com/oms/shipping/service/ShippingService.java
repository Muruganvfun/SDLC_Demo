package com.oms.shipping.service;

import com.oms.common.exception.ApiException;
import com.oms.shipping.dto.ShipmentRequest;
import com.oms.shipping.dto.ShipmentResponse;
import com.oms.shipping.dto.UpdateStatusRequest;
import com.oms.shipping.entity.Shipment;
import com.oms.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShipmentRepository shipmentRepository;

    @Transactional
    public ShipmentResponse createShipment(ShipmentRequest request) {
        log.info("Creating shipment for order: {}", request.getOrderId());

        String trackingNumber = "TRK" + System.currentTimeMillis() + 
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .trackingNumber(trackingNumber)
                .carrier("DemoShip")
                .status(Shipment.ShipmentStatus.PROCESSING)
                .shippingAddress(request.getAddress())
                .estimatedDelivery(LocalDate.now().plusDays(5))
                .build();

        shipment = shipmentRepository.save(shipment);

        log.info("Shipment created: {} for order {}", trackingNumber, request.getOrderId());
        return ShipmentResponse.from(shipment);
    }

    public ShipmentResponse getShipmentByOrder(String orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> ApiException.notFound("Shipment not found for order: " + orderId));
        return ShipmentResponse.from(shipment);
    }

    public ShipmentResponse getShipmentByTracking(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> ApiException.notFound("Shipment not found: " + trackingNumber));
        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse updateStatus(String id, UpdateStatusRequest request, boolean isAdmin) {
        if (!isAdmin) {
            throw ApiException.forbidden("Only admins can update shipment status");
        }

        Shipment shipment = shipmentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> ApiException.notFound("Shipment not found: " + id));

        try {
            Shipment.ShipmentStatus newStatus = Shipment.ShipmentStatus.valueOf(request.getStatus());
            shipment.setStatus(newStatus);
            shipment = shipmentRepository.save(shipment);
            
            log.info("Shipment {} status updated to {}", id, newStatus);
            return ShipmentResponse.from(shipment);
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid status: " + request.getStatus());
        }
    }
}
