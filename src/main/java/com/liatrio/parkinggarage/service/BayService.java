package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.BayDto;
import com.liatrio.parkinggarage.entity.Bay;
import com.liatrio.parkinggarage.entity.Floor;
import com.liatrio.parkinggarage.exception.ResourceAlreadyExistsException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.mapper.EntityMapper;
import com.liatrio.parkinggarage.repository.BayRepository;
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
public class BayService {

    private final BayRepository bayRepository;
    private final FloorRepository floorRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<BayDto> getAllBays() {
        log.debug("Fetching all bays");
        return bayRepository.findAll().stream()
                .map(entityMapper::toBayDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BayDto> getBaysByFloorId(Long floorId) {
        log.debug("Fetching bays for floor: {}", floorId);
        return bayRepository.findByFloorId(floorId).stream()
                .map(entityMapper::toBayDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BayDto getBayById(Long id) {
        log.debug("Fetching bay with id: {}", id);
        Bay bay = bayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bay", "id", id));
        return entityMapper.toBayDto(bay);
    }

    @Transactional
    public BayDto createBay(BayDto bayDto) {
        log.debug("Creating bay: {}", bayDto);
        
        Floor floor = floorRepository.findById(bayDto.getFloorId())
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", bayDto.getFloorId()));
        
        if (bayRepository.findByFloorIdAndBayIdentifier(bayDto.getFloorId(), bayDto.getBayIdentifier()).isPresent()) {
            throw new ResourceAlreadyExistsException("Bay", "bayIdentifier", bayDto.getBayIdentifier());
        }
        
        Bay bay = Bay.builder()
                .bayIdentifier(bayDto.getBayIdentifier())
                .name(bayDto.getName())
                .floor(floor)
                .active(true)
                .build();
        
        Bay savedBay = bayRepository.save(bay);
        log.info("Created bay with id: {}", savedBay.getId());
        
        return entityMapper.toBayDto(savedBay);
    }

    @Transactional
    public BayDto updateBay(Long id, BayDto bayDto) {
        log.debug("Updating bay with id: {}", id);
        
        Bay bay = bayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bay", "id", id));
        
        Floor floor = floorRepository.findById(bayDto.getFloorId())
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", bayDto.getFloorId()));
        
        // Check if bay identifier is being changed and if it already exists on this floor
        if (!bay.getBayIdentifier().equals(bayDto.getBayIdentifier()) &&
                bayRepository.findByFloorIdAndBayIdentifier(bayDto.getFloorId(), bayDto.getBayIdentifier()).isPresent()) {
            throw new ResourceAlreadyExistsException("Bay", "bayIdentifier", bayDto.getBayIdentifier());
        }
        
        bay.setBayIdentifier(bayDto.getBayIdentifier());
        bay.setName(bayDto.getName());
        bay.setFloor(floor);
        if (bayDto.getActive() != null) {
            bay.setActive(bayDto.getActive());
        }
        
        Bay updatedBay = bayRepository.save(bay);
        log.info("Updated bay with id: {}", updatedBay.getId());
        
        return entityMapper.toBayDto(updatedBay);
    }

    @Transactional
    public void deleteBay(Long id) {
        log.debug("Deleting bay with id: {}", id);
        
        Bay bay = bayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bay", "id", id));
        
        bay.setActive(false);
        bayRepository.save(bay);
        
        log.info("Soft deleted bay with id: {}", id);
    }
}
