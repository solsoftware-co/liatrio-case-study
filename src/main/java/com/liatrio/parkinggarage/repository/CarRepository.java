package com.liatrio.parkinggarage.repository;

import com.liatrio.parkinggarage.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    
    Optional<Car> findByLicensePlate(String licensePlate);
    
    boolean existsByLicensePlate(String licensePlate);
    
    /**
     * Find all currently parked cars (with active transactions)
     */
    @Query("""
        SELECT DISTINCT c FROM Car c 
        JOIN c.transactions t 
        WHERE t.checkOutTime IS NULL
    """)
    List<Car> findCurrentlyParkedCars();
}
