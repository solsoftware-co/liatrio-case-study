package com.liatrio.parkinggarage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parking_spots", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"bay_id", "spot_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String spotIdentifier;

    @Column(nullable = false)
    private String spotNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_type_id", nullable = false)
    private SpotType spotType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id", nullable = false)
    private Bay bay;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Version
    private Long version;

    @OneToMany(mappedBy = "parkingSpot", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ParkingTransaction> transactions = new ArrayList<>();

    /**
     * Derived property: spot is occupied if there's an active (not checked out) transaction
     */
    @Transient
    public boolean isOccupied() {
        return transactions.stream()
            .anyMatch(t -> t.getCheckOutTime() == null);
    }

    /**
     * Get current active transaction if spot is occupied
     */
    @Transient
    public ParkingTransaction getActiveTransaction() {
        return transactions.stream()
            .filter(t -> t.getCheckOutTime() == null)
            .findFirst()
            .orElse(null);
    }
}
