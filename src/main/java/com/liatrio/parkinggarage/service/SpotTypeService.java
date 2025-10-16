package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.SpotTypeDto;
import com.liatrio.parkinggarage.entity.SpotType;
import com.liatrio.parkinggarage.exception.ResourceAlreadyExistsException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.repository.SpotTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotTypeService {

    private final SpotTypeRepository spotTypeRepository;

    @Transactional(readOnly = true)
    public List<SpotTypeDto> getAllSpotTypes() {
        log.debug("Fetching all spot types");
        return spotTypeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SpotTypeDto> getActiveSpotTypes() {
        log.debug("Fetching active spot types");
        return spotTypeRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SpotTypeDto getSpotTypeById(Long id) {
        log.debug("Fetching spot type with id: {}", id);
        SpotType spotType = spotTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SpotType", "id", id));
        return toDto(spotType);
    }

    @Transactional(readOnly = true)
    public SpotTypeDto getSpotTypeByName(String name) {
        log.debug("Fetching spot type with name: {}", name);
        SpotType spotType = spotTypeRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("SpotType", "name", name));
        return toDto(spotType);
    }

    @Transactional
    public SpotTypeDto createSpotType(SpotTypeDto spotTypeDto) {
        log.debug("Creating spot type: {}", spotTypeDto);
        
        if (spotTypeRepository.existsByName(spotTypeDto.getName())) {
            throw new ResourceAlreadyExistsException("SpotType", "name", spotTypeDto.getName());
        }
        
        SpotType spotType = SpotType.builder()
                .name(spotTypeDto.getName())
                .description(spotTypeDto.getDescription())
                .active(true)
                .build();
        
        SpotType savedSpotType = spotTypeRepository.save(spotType);
        log.info("Created spot type with id: {}", savedSpotType.getId());
        
        return toDto(savedSpotType);
    }

    @Transactional
    public SpotTypeDto updateSpotType(Long id, SpotTypeDto spotTypeDto) {
        log.debug("Updating spot type with id: {}", id);
        
        SpotType spotType = spotTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SpotType", "id", id));
        
        // Check if name is being changed and if it already exists
        if (!spotType.getName().equals(spotTypeDto.getName()) &&
                spotTypeRepository.existsByName(spotTypeDto.getName())) {
            throw new ResourceAlreadyExistsException("SpotType", "name", spotTypeDto.getName());
        }
        
        spotType.setName(spotTypeDto.getName());
        spotType.setDescription(spotTypeDto.getDescription());
        if (spotTypeDto.getActive() != null) {
            spotType.setActive(spotTypeDto.getActive());
        }
        
        SpotType updatedSpotType = spotTypeRepository.save(spotType);
        log.info("Updated spot type with id: {}", updatedSpotType.getId());
        
        return toDto(updatedSpotType);
    }

    @Transactional
    public void deleteSpotType(Long id) {
        log.debug("Deleting spot type with id: {}", id);
        
        SpotType spotType = spotTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SpotType", "id", id));
        
        spotType.setActive(false);
        spotTypeRepository.save(spotType);
        
        log.info("Soft deleted spot type with id: {}", id);
    }

    private SpotTypeDto toDto(SpotType spotType) {
        return SpotTypeDto.builder()
                .id(spotType.getId())
                .name(spotType.getName())
                .description(spotType.getDescription())
                .active(spotType.getActive())
                .build();
    }
}
