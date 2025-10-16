package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.FloorDto;
import com.liatrio.parkinggarage.entity.Floor;
import com.liatrio.parkinggarage.exception.ResourceAlreadyExistsException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.mapper.EntityMapper;
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
class FloorServiceTest {

    @Mock
    private FloorRepository floorRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private FloorService floorService;

    private Floor floor;
    private FloorDto floorDto;

    @BeforeEach
    void setUp() {
        floor = Floor.builder()
                .id(1L)
                .floorNumber(1)
                .name("Ground Floor")
                .active(true)
                .build();

        floorDto = FloorDto.builder()
                .id(1L)
                .floorNumber(1)
                .name("Ground Floor")
                .active(true)
                .build();
    }

    @Test
    void getAllFloors_ShouldReturnAllFloors() {
        // Arrange
        when(floorRepository.findAll()).thenReturn(Arrays.asList(floor));
        when(entityMapper.toFloorDto(floor)).thenReturn(floorDto);

        // Act
        List<FloorDto> result = floorService.getAllFloors();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(floorDto.getFloorNumber(), result.get(0).getFloorNumber());
        verify(floorRepository, times(1)).findAll();
    }

    @Test
    void getActiveFloors_ShouldReturnOnlyActiveFloors() {
        // Arrange
        when(floorRepository.findByActiveTrue()).thenReturn(Arrays.asList(floor));
        when(entityMapper.toFloorDto(floor)).thenReturn(floorDto);

        // Act
        List<FloorDto> result = floorService.getActiveFloors();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
        verify(floorRepository, times(1)).findByActiveTrue();
    }

    @Test
    void getFloorById_WhenExists_ShouldReturnFloor() {
        // Arrange
        when(floorRepository.findById(1L)).thenReturn(Optional.of(floor));
        when(entityMapper.toFloorDto(floor)).thenReturn(floorDto);

        // Act
        FloorDto result = floorService.getFloorById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(floorRepository, times(1)).findById(1L);
    }

    @Test
    void getFloorById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(floorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> floorService.getFloorById(1L));
    }

    @Test
    void createFloor_WhenValidData_ShouldCreateFloor() {
        // Arrange
        when(floorRepository.existsByFloorNumber(1)).thenReturn(false);
        when(floorRepository.save(any(Floor.class))).thenReturn(floor);
        when(entityMapper.toFloorDto(floor)).thenReturn(floorDto);

        // Act
        FloorDto result = floorService.createFloor(floorDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getFloorNumber());
        verify(floorRepository, times(1)).save(any(Floor.class));
    }

    @Test
    void createFloor_WhenFloorNumberExists_ShouldThrowException() {
        // Arrange
        when(floorRepository.existsByFloorNumber(1)).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> floorService.createFloor(floorDto));
        verify(floorRepository, never()).save(any(Floor.class));
    }

    @Test
    void updateFloor_WhenValidData_ShouldUpdateFloor() {
        // Arrange
        FloorDto updateDto = FloorDto.builder()
                .floorNumber(1)
                .name("Updated Floor")
                .active(true)
                .build();

        when(floorRepository.findById(1L)).thenReturn(Optional.of(floor));
        when(floorRepository.save(any(Floor.class))).thenReturn(floor);
        when(entityMapper.toFloorDto(any(Floor.class))).thenReturn(updateDto);

        // Act
        FloorDto result = floorService.updateFloor(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(floorRepository, times(1)).save(any(Floor.class));
    }

    @Test
    void updateFloor_WhenFloorNotExists_ShouldThrowException() {
        // Arrange
        when(floorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> floorService.updateFloor(1L, floorDto));
    }

    @Test
    void updateFloor_WhenFloorNumberChanged_AndExists_ShouldThrowException() {
        // Arrange
        FloorDto updateDto = FloorDto.builder()
                .floorNumber(2)
                .name("Second Floor")
                .active(true)
                .build();

        when(floorRepository.findById(1L)).thenReturn(Optional.of(floor));
        when(floorRepository.existsByFloorNumber(2)).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> floorService.updateFloor(1L, updateDto));
    }

    @Test
    void deleteFloor_WhenExists_ShouldSoftDelete() {
        // Arrange
        when(floorRepository.findById(1L)).thenReturn(Optional.of(floor));
        when(floorRepository.save(any(Floor.class))).thenReturn(floor);

        // Act
        floorService.deleteFloor(1L);

        // Assert
        verify(floorRepository, times(1)).save(any(Floor.class));
        assertFalse(floor.getActive());
    }

    @Test
    void deleteFloor_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(floorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> floorService.deleteFloor(1L));
    }
}
