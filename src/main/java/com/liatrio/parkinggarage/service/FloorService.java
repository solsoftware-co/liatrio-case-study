package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.FloorDto;
import com.liatrio.parkinggarage.entity.Floor;
import com.liatrio.parkinggarage.exception.ResourceAlreadyExistsException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.mapper.EntityMapper;
import com.liatrio.parkinggarage.repository.FloorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FloorService {

    private final FloorRepository floorRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<FloorDto> getAllFloors() {
        log.debug("Fetching all floors");
        return floorRepository.findAll().stream()
                .map(entityMapper::toFloorDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FloorDto> getActiveFloors() {
        log.debug("Fetching active floors");
        return floorRepository.findByActiveTrue().stream()
                .map(entityMapper::toFloorDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FloorDto getFloorById(Long id) {
        log.debug("Fetching floor with id: {}", id);
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", id));
        return entityMapper.toFloorDto(floor);
    }

    @Transactional
    public FloorDto createFloor(FloorDto floorDto) {
        log.debug("Creating floor: {}", floorDto);
        
        if (floorRepository.existsByFloorNumber(floorDto.getFloorNumber())) {
            throw new ResourceAlreadyExistsException("Floor", "floorNumber", floorDto.getFloorNumber());
        }
        
        Floor floor = Floor.builder()
                .floorNumber(floorDto.getFloorNumber())
                .name(floorDto.getName())
                .active(true)
                .build();
        
        Floor savedFloor = floorRepository.save(floor);
        log.info("Created floor with id: {}", savedFloor.getId());
        
        return entityMapper.toFloorDto(savedFloor);
    }

    @Transactional
    public FloorDto updateFloor(Long id, FloorDto floorDto) {
        log.debug("Updating floor with id: {}", id);
        
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", id));
        
        // Check if floor number is being changed and if it already exists
        if (!floor.getFloorNumber().equals(floorDto.getFloorNumber()) &&
                floorRepository.existsByFloorNumber(floorDto.getFloorNumber())) {
            throw new ResourceAlreadyExistsException("Floor", "floorNumber", floorDto.getFloorNumber());
        }
        
        floor.setFloorNumber(floorDto.getFloorNumber());
        floor.setName(floorDto.getName());
        if (floorDto.getActive() != null) {
            floor.setActive(floorDto.getActive());
        }
        
        Floor updatedFloor = floorRepository.save(floor);
        log.info("Updated floor with id: {}", updatedFloor.getId());
        
        return entityMapper.toFloorDto(updatedFloor);
    }

    @Transactional
    public void deleteFloor(Long id) {
        log.debug("Deleting floor with id: {}", id);
        
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", id));
        
        floor.setActive(false);
        floorRepository.save(floor);
        
        log.info("Soft deleted floor with id: {}", id);
    }
}
