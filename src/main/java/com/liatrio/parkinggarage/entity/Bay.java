package com.liatrio.parkinggarage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bays", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"floor_id", "bay_identifier"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bayIdentifier;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "bay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ParkingSpot> parkingSpots = new ArrayList<>();
}
