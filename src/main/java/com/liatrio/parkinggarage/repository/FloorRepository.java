package com.liatrio.parkinggarage.repository;

import com.liatrio.parkinggarage.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {
    
    Optional<Floor> findByFloorNumber(Integer floorNumber);
    
    List<Floor> findByActiveTrue();
    
    boolean existsByFloorNumber(Integer floorNumber);
}
