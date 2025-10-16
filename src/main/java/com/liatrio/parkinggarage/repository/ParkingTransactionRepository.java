package com.liatrio.parkinggarage.repository;

import com.liatrio.parkinggarage.entity.ParkingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingTransactionRepository extends JpaRepository<ParkingTransaction, Long> {
    
    List<ParkingTransaction> findByCarId(Long carId);
    
    List<ParkingTransaction> findByParkingSpotId(Long parkingSpotId);
    
    /**
     * Find active transaction for a parking spot
     */
    Optional<ParkingTransaction> findByParkingSpotIdAndCheckOutTimeIsNull(Long parkingSpotId);
    
    /**
     * Find active transaction for a car
     */
    Optional<ParkingTransaction> findByCarIdAndCheckOutTimeIsNull(Long carId);
    
    /**
     * Find all active (ongoing) transactions
     */
    @Query("SELECT pt FROM ParkingTransaction pt WHERE pt.checkOutTime IS NULL ORDER BY pt.checkInTime DESC")
    List<ParkingTransaction> findActiveTransactions();
    
    /**
     * Find all completed transactions
     */
    @Query("SELECT pt FROM ParkingTransaction pt WHERE pt.checkOutTime IS NOT NULL ORDER BY pt.checkOutTime DESC")
    List<ParkingTransaction> findCompletedTransactions();
}
