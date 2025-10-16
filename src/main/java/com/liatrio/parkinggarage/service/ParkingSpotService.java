package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.ParkingSpotDto;
import com.liatrio.parkinggarage.entity.Bay;
import com.liatrio.parkinggarage.entity.ParkingSpot;
import com.liatrio.parkinggarage.entity.SpotType;
import com.liatrio.parkinggarage.exception.ResourceAlreadyExistsException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.mapper.EntityMapper;
import com.liatrio.parkinggarage.repository.BayRepository;
import com.liatrio.parkinggarage.repository.ParkingSpotRepository;
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
public class ParkingSpotService {

    private final ParkingSpotRepository parkingSpotRepository;
    private final BayRepository bayRepository;
    private final SpotTypeRepository spotTypeRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<ParkingSpotDto> getAllParkingSpots() {
        log.debug("Fetching all parking spots");
        return parkingSpotRepository.findAll().stream()
                .map(entityMapper::toParkingSpotDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingSpotDto> getAvailableParkingSpots() {
        log.debug("Fetching available parking spots");
        return parkingSpotRepository.findAvailableSpots().stream()
                .map(entityMapper::toParkingSpotDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingSpotDto> getOccupiedParkingSpots() {
        log.debug("Fetching occupied parking spots");
        return parkingSpotRepository.findOccupiedSpots().stream()
                .map(entityMapper::toParkingSpotDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingSpotDto> getAvailableSpotsByFloor(Long floorId) {
        log.debug("Fetching available parking spots for floor: {}", floorId);
        return parkingSpotRepository.findAvailableSpotsByFloorId(floorId).stream()
                .map(entityMapper::toParkingSpotDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParkingSpotDto getParkingSpotById(Long id) {
        log.debug("Fetching parking spot with id: {}", id);
        ParkingSpot spot = parkingSpotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "id", id));
        return entityMapper.toParkingSpotDto(spot);
    }

    @Transactional(readOnly = true)
    public ParkingSpotDto getParkingSpotByIdentifier(String spotIdentifier) {
        log.debug("Fetching parking spot with identifier: {}", spotIdentifier);
        ParkingSpot spot = parkingSpotRepository.findBySpotIdentifier(spotIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "spotIdentifier", spotIdentifier));
        return entityMapper.toParkingSpotDto(spot);
    }

    @Transactional
    public ParkingSpotDto createParkingSpot(ParkingSpotDto spotDto) {
        log.debug("Creating parking spot: {}", spotDto);
        
        Bay bay = bayRepository.findById(spotDto.getBayId())
                .orElseThrow(() -> new ResourceNotFoundException("Bay", "id", spotDto.getBayId()));
        
        SpotType spotType = spotTypeRepository.findById(spotDto.getSpotTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("SpotType", "id", spotDto.getSpotTypeId()));
        
        if (parkingSpotRepository.findBySpotIdentifier(spotDto.getSpotIdentifier()).isPresent()) {
            throw new ResourceAlreadyExistsException("ParkingSpot", "spotIdentifier", spotDto.getSpotIdentifier());
        }
        
        ParkingSpot spot = ParkingSpot.builder()
                .spotIdentifier(spotDto.getSpotIdentifier())
                .spotNumber(spotDto.getSpotNumber())
                .spotType(spotType)
                .bay(bay)
                .active(true)
                .build();
        
        ParkingSpot savedSpot = parkingSpotRepository.save(spot);
        log.info("Created parking spot with id: {}", savedSpot.getId());
        
        return entityMapper.toParkingSpotDto(savedSpot);
    }

    @Transactional
    public ParkingSpotDto updateParkingSpot(Long id, ParkingSpotDto spotDto) {
        log.debug("Updating parking spot with id: {}", id);
        
        ParkingSpot spot = parkingSpotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "id", id));
        
        Bay bay = bayRepository.findById(spotDto.getBayId())
                .orElseThrow(() -> new ResourceNotFoundException("Bay", "id", spotDto.getBayId()));
        
        SpotType spotType = spotTypeRepository.findById(spotDto.getSpotTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("SpotType", "id", spotDto.getSpotTypeId()));
        
        // Check if spot identifier is being changed and if it already exists
        if (!spot.getSpotIdentifier().equals(spotDto.getSpotIdentifier()) &&
                parkingSpotRepository.findBySpotIdentifier(spotDto.getSpotIdentifier()).isPresent()) {
            throw new ResourceAlreadyExistsException("ParkingSpot", "spotIdentifier", spotDto.getSpotIdentifier());
        }
        
        spot.setSpotIdentifier(spotDto.getSpotIdentifier());
        spot.setSpotNumber(spotDto.getSpotNumber());
        spot.setSpotType(spotType);
        spot.setBay(bay);
        if (spotDto.getActive() != null) {
            spot.setActive(spotDto.getActive());
        }
        
        ParkingSpot updatedSpot = parkingSpotRepository.save(spot);
        log.info("Updated parking spot with id: {}", updatedSpot.getId());
        
        return entityMapper.toParkingSpotDto(updatedSpot);
    }

    @Transactional
    public void deleteParkingSpot(Long id) {
        log.debug("Deleting parking spot with id: {}", id);
        
        ParkingSpot spot = parkingSpotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "id", id));
        
        spot.setActive(false);
        parkingSpotRepository.save(spot);
        
        log.info("Soft deleted parking spot with id: {}", id);
    }
}
