package com.liatrio.parkinggarage.repository;

import com.liatrio.parkinggarage.entity.Bay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BayRepository extends JpaRepository<Bay, Long> {
    
    List<Bay> findByFloorId(Long floorId);
    
    List<Bay> findByActiveTrue();
    
    Optional<Bay> findByFloorIdAndBayIdentifier(Long floorId, String bayIdentifier);
    
    @Query("SELECT b FROM Bay b WHERE b.floor.id = :floorId AND b.active = true")
    List<Bay> findActiveByFloorId(Long floorId);
}
