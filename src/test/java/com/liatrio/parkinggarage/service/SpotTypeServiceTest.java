package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.SpotTypeDto;
import com.liatrio.parkinggarage.entity.SpotType;
import com.liatrio.parkinggarage.exception.ResourceAlreadyExistsException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.repository.SpotTypeRepository;
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
class SpotTypeServiceTest {

    @Mock
    private SpotTypeRepository spotTypeRepository;

    @InjectMocks
    private SpotTypeService spotTypeService;

    private SpotType spotType;
    private SpotTypeDto spotTypeDto;

    @BeforeEach
    void setUp() {
        spotType = SpotType.builder()
                .id(1L)
                .name("REGULAR")
                .description("Regular parking spot")
                .active(true)
                .build();

        spotTypeDto = SpotTypeDto.builder()
                .id(1L)
                .name("REGULAR")
                .description("Regular parking spot")
                .active(true)
                .build();
    }

    @Test
    void getAllSpotTypes_ShouldReturnAllSpotTypes() {
        // Arrange
        when(spotTypeRepository.findAll()).thenReturn(Arrays.asList(spotType));

        // Act
        List<SpotTypeDto> result = spotTypeService.getAllSpotTypes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("REGULAR", result.get(0).getName());
        verify(spotTypeRepository, times(1)).findAll();
    }

    @Test
    void getActiveSpotTypes_ShouldReturnOnlyActiveSpotTypes() {
        // Arrange
        when(spotTypeRepository.findByActiveTrue()).thenReturn(Arrays.asList(spotType));

        // Act
        List<SpotTypeDto> result = spotTypeService.getActiveSpotTypes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
        verify(spotTypeRepository, times(1)).findByActiveTrue();
    }

    @Test
    void getSpotTypeById_WhenExists_ShouldReturnSpotType() {
        // Arrange
        when(spotTypeRepository.findById(1L)).thenReturn(Optional.of(spotType));

        // Act
        SpotTypeDto result = spotTypeService.getSpotTypeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("REGULAR", result.getName());
        verify(spotTypeRepository, times(1)).findById(1L);
    }

    @Test
    void getSpotTypeById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(spotTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> spotTypeService.getSpotTypeById(1L));
    }

    @Test
    void getSpotTypeByName_WhenExists_ShouldReturnSpotType() {
        // Arrange
        when(spotTypeRepository.findByName("REGULAR")).thenReturn(Optional.of(spotType));

        // Act
        SpotTypeDto result = spotTypeService.getSpotTypeByName("REGULAR");

        // Assert
        assertNotNull(result);
        assertEquals("REGULAR", result.getName());
        verify(spotTypeRepository, times(1)).findByName("REGULAR");
    }

    @Test
    void getSpotTypeByName_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(spotTypeRepository.findByName("REGULAR")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> spotTypeService.getSpotTypeByName("REGULAR"));
    }

    @Test
    void createSpotType_WhenValidData_ShouldCreateSpotType() {
        // Arrange
        when(spotTypeRepository.existsByName("REGULAR")).thenReturn(false);
        when(spotTypeRepository.save(any(SpotType.class))).thenReturn(spotType);

        // Act
        SpotTypeDto result = spotTypeService.createSpotType(spotTypeDto);

        // Assert
        assertNotNull(result);
        assertEquals("REGULAR", result.getName());
        verify(spotTypeRepository, times(1)).save(any(SpotType.class));
    }

    @Test
    void createSpotType_WhenNameExists_ShouldThrowException() {
        // Arrange
        when(spotTypeRepository.existsByName("REGULAR")).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> spotTypeService.createSpotType(spotTypeDto));
        verify(spotTypeRepository, never()).save(any(SpotType.class));
    }

    @Test
    void updateSpotType_WhenValidData_ShouldUpdateSpotType() {
        // Arrange
        SpotTypeDto updateDto = SpotTypeDto.builder()
                .name("REGULAR")
                .description("Updated description")
                .active(true)
                .build();

        when(spotTypeRepository.findById(1L)).thenReturn(Optional.of(spotType));
        when(spotTypeRepository.save(any(SpotType.class))).thenReturn(spotType);

        // Act
        SpotTypeDto result = spotTypeService.updateSpotType(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(spotTypeRepository, times(1)).save(any(SpotType.class));
    }

    @Test
    void updateSpotType_WhenSpotTypeNotExists_ShouldThrowException() {
        // Arrange
        when(spotTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> spotTypeService.updateSpotType(1L, spotTypeDto));
    }

    @Test
    void updateSpotType_WhenNameChanged_AndExists_ShouldThrowException() {
        // Arrange
        SpotTypeDto updateDto = SpotTypeDto.builder()
                .name("COMPACT")
                .description("Compact spot")
                .active(true)
                .build();

        when(spotTypeRepository.findById(1L)).thenReturn(Optional.of(spotType));
        when(spotTypeRepository.existsByName("COMPACT")).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> spotTypeService.updateSpotType(1L, updateDto));
    }

    @Test
    void deleteSpotType_WhenExists_ShouldSoftDelete() {
        // Arrange
        when(spotTypeRepository.findById(1L)).thenReturn(Optional.of(spotType));
        when(spotTypeRepository.save(any(SpotType.class))).thenReturn(spotType);

        // Act
        spotTypeService.deleteSpotType(1L);

        // Assert
        verify(spotTypeRepository, times(1)).save(any(SpotType.class));
        assertFalse(spotType.getActive());
    }

    @Test
    void deleteSpotType_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(spotTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> spotTypeService.deleteSpotType(1L));
    }
}
