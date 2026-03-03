package com.oms.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID productId;

    @Column(nullable = false)
    @Builder.Default
    private int quantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private int reservedQuantity = 0;

    @UpdateTimestamp
    private Instant updatedAt;

    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public boolean canReserve(int requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }

    public void reserve(int amount) {
        if (!canReserve(amount)) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.reservedQuantity += amount;
    }

    public void release(int amount) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - amount);
    }

    public void confirmReservation(int amount) {
        this.quantity -= amount;
        this.reservedQuantity -= amount;
    }
}
