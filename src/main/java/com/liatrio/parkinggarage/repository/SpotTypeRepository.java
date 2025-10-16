package com.liatrio.parkinggarage.repository;

import com.liatrio.parkinggarage.entity.SpotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpotTypeRepository extends JpaRepository<SpotType, Long> {
    
    Optional<SpotType> findByName(String name);
    
    List<SpotType> findByActiveTrue();
    
    boolean existsByName(String name);
}
