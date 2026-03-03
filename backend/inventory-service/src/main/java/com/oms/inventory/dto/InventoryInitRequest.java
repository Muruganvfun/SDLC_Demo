package com.oms.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryInitRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;
}
