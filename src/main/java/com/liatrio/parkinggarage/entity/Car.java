package com.liatrio.parkinggarage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Column
    private String make;

    @Column
    private String model;

    @Column
    private String color;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ParkingTransaction> transactions = new ArrayList<>();

    /**
     * Check if car is currently parked (has active transaction)
     */
    @Transient
    public boolean isCurrentlyParked() {
        return transactions.stream()
            .anyMatch(t -> t.getCheckOutTime() == null);
    }

    /**
     * Get current parking transaction if car is parked
     */
    @Transient
    public ParkingTransaction getCurrentTransaction() {
        return transactions.stream()
            .filter(t -> t.getCheckOutTime() == null)
            .findFirst()
            .orElse(null);
    }
}
