package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.BayDto;
import com.liatrio.parkinggarage.entity.Bay;
import com.liatrio.parkinggarage.entity.Floor;
import com.liatrio.parkinggarage.exception.ResourceAlreadyExistsException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.mapper.EntityMapper;
import com.liatrio.parkinggarage.repository.BayRepository;
import com.liatrio.parkinggarage.repository.FloorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BayServiceTest {

    @Mock
    private BayRepository bayRepository;

    @Mock
    private FloorRepository floorRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private BayService bayService;

    private Floor floor;
    private Bay bay;
    private BayDto bayDto;

    @BeforeEach
    void setUp() {
        floor = Floor.builder()
                .id(1L)
                .floorNumber(1)
                .name("Ground Floor")
                .active(true)
                .build();

        bay = Bay.builder()
                .id(1L)
                .bayIdentifier("A")
                .name("Bay A")
                .floor(floor)
                .active(true)
                .build();

        bayDto = BayDto.builder()
                .id(1L)
                .bayIdentifier("A")
                .name("Bay A")
                .floorId(1L)
                .floorNumber(1)
                .active(true)
                .build();
    }

    @Test
    void getAllBays_ShouldReturnAllBays() {
        // Arrange
        when(bayRepository.findAll()).thenReturn(Arrays.asList(bay));
        when(entityMapper.toBayDto(bay)).thenReturn(bayDto);

        // Act
        List<BayDto> result = bayService.getAllBays();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getBayIdentifier());
        verify(bayRepository, times(1)).findAll();
    }

    @Test
    void getBaysByFloorId_ShouldReturnBaysForFloor() {
        // Arrange
        when(bayRepository.findByFloorId(1L)).thenReturn(Arrays.asList(bay));
        when(entityMapper.toBayDto(bay)).thenReturn(bayDto);

        // Act
        List<BayDto> result = bayService.getBaysByFloorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getFloorId());
        verify(bayRepository, times(1)).findByFloorId(1L);
    }

    @Test
    void getBayById_WhenExists_ShouldReturnBay() {
        // Arrange
        when(bayRepository.findById(1L)).thenReturn(Optional.of(bay));
        when(entityMapper.toBayDto(bay)).thenReturn(bayDto);

        // Act
        BayDto result = bayService.getBayById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bayRepository, times(1)).findById(1L);
    }

    @Test
    void getBayById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(bayRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bayService.getBayById(1L));
    }

    @Test
    void createBay_WhenValidData_ShouldCreateBay() {
        // Arrange
        when(floorRepository.findById(1L)).thenReturn(Optional.of(floor));
        when(bayRepository.findByFloorIdAndBayIdentifier(1L, "A")).thenReturn(Optional.empty());
        when(bayRepository.save(any(Bay.class))).thenReturn(bay);
        when(entityMapper.toBayDto(bay)).thenReturn(bayDto);

        // Act
        BayDto result = bayService.createBay(bayDto);

        // Assert
        assertNotNull(result);
        assertEquals("A", result.getBayIdentifier());
        verify(bayRepository, times(1)).save(any(Bay.class));
    }

    @Test
    void createBay_WhenFloorNotExists_ShouldThrowException() {
        // Arrange
        when(floorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bayService.createBay(bayDto));
        verify(bayRepository, never()).save(any(Bay.class));
    }

    @Test
    void createBay_WhenBayAlreadyExists_ShouldThrowException() {
        // Arrange
        when(floorRepository.findById(1L)).thenReturn(Optional.of(floor));
        when(bayRepository.findByFloorIdAndBayIdentifier(1L, "A")).thenReturn(Optional.of(bay));

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> bayService.createBay(bayDto));
        verify(bayRepository, never()).save(any(Bay.class));
    }

    @Test
    void updateBay_WhenValidData_ShouldUpdateBay() {
        // Arrange
        BayDto updateDto = BayDto.builder()
                .bayIdentifier("A")
                .name("Updated Bay")
                .floorId(1L)
                .active(true)
                .build();

        when(bayRepository.findById(1L)).thenReturn(Optional.of(bay));
        when(floorRepository.findById(1L)).thenReturn(Optional.of(floor));
        when(bayRepository.save(any(Bay.class))).thenReturn(bay);
        when(entityMapper.toBayDto(any(Bay.class))).thenReturn(updateDto);

        // Act
        BayDto result = bayService.updateBay(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(bayRepository, times(1)).save(any(Bay.class));
    }

    @Test
    void updateBay_WhenBayNotExists_ShouldThrowException() {
        // Arrange
        when(bayRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bayService.updateBay(1L, bayDto));
    }

    @Test
    void updateBay_WhenFloorNotExists_ShouldThrowException() {
        // Arrange
        when(bayRepository.findById(1L)).thenReturn(Optional.of(bay));
        when(floorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bayService.updateBay(1L, bayDto));
    }

    @Test
    void deleteBay_WhenExists_ShouldSoftDelete() {
        // Arrange
        when(bayRepository.findById(1L)).thenReturn(Optional.of(bay));
        when(bayRepository.save(any(Bay.class))).thenReturn(bay);

        // Act
        bayService.deleteBay(1L);

        // Assert
        verify(bayRepository, times(1)).save(any(Bay.class));
        assertFalse(bay.getActive());
    }

    @Test
    void deleteBay_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(bayRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bayService.deleteBay(1L));
    }
}
