package com.liatrio.parkinggarage.repository;

import com.liatrio.parkinggarage.entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    
    Optional<ParkingSpot> findBySpotIdentifier(String spotIdentifier);
    
    List<ParkingSpot> findByBayId(Long bayId);
    
    List<ParkingSpot> findByActiveTrue();
    
    List<ParkingSpot> findBySpotTypeId(Long spotTypeId);
    
    /**
     * Find available spots (active spots with no active transactions)
     */
    @Query("""
        SELECT ps FROM ParkingSpot ps 
        WHERE ps.active = true 
        AND NOT EXISTS (
            SELECT 1 FROM ParkingTransaction pt 
            WHERE pt.parkingSpot.id = ps.id 
            AND pt.checkOutTime IS NULL
        )
        ORDER BY ps.bay.floor.floorNumber, ps.bay.bayIdentifier, ps.spotNumber
    """)
    List<ParkingSpot> findAvailableSpots();
    
    /**
     * Find occupied spots (active spots with active transactions)
     */
    @Query("""
        SELECT ps FROM ParkingSpot ps 
        WHERE ps.active = true 
        AND EXISTS (
            SELECT 1 FROM ParkingTransaction pt 
            WHERE pt.parkingSpot.id = ps.id 
            AND pt.checkOutTime IS NULL
        )
        ORDER BY ps.bay.floor.floorNumber, ps.bay.bayIdentifier, ps.spotNumber
    """)
    List<ParkingSpot> findOccupiedSpots();
    
    /**
     * Find available spots by floor
     */
    @Query("""
        SELECT ps FROM ParkingSpot ps 
        WHERE ps.bay.floor.id = :floorId 
        AND ps.active = true 
        AND NOT EXISTS (
            SELECT 1 FROM ParkingTransaction pt 
            WHERE pt.parkingSpot.id = ps.id 
            AND pt.checkOutTime IS NULL
        )
        ORDER BY ps.bay.bayIdentifier, ps.spotNumber
    """)
    List<ParkingSpot> findAvailableSpotsByFloorId(Long floorId);
}
