package com.liatrio.parkinggarage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_spot_id", nullable = false)
    private ParkingSpot parkingSpot;

    @Column(nullable = false)
    private LocalDateTime checkInTime;

    @Column
    private LocalDateTime checkOutTime;

    @Column
    private Double parkingFee;

    @Column
    private String notes;

    /**
     * Check if this transaction is active (car still parked)
     */
    @Transient
    public boolean isActive() {
        return checkOutTime == null;
    }
    
    /**
     * Get parking duration in hours
     */
    @Transient
    public Double getDurationInHours() {
        if (checkInTime == null) return 0.0;
        LocalDateTime endTime = checkOutTime != null ? checkOutTime : LocalDateTime.now();
        long minutes = java.time.Duration.between(checkInTime, endTime).toMinutes();
        return minutes / 60.0;
    }
}
