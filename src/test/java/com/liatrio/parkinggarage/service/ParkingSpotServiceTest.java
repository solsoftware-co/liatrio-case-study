package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.ParkingSpotDto;
import com.liatrio.parkinggarage.entity.Bay;
import com.liatrio.parkinggarage.entity.Floor;
import com.liatrio.parkinggarage.entity.ParkingSpot;
import com.liatrio.parkinggarage.entity.SpotType;
import com.liatrio.parkinggarage.exception.ResourceAlreadyExistsException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.mapper.EntityMapper;
import com.liatrio.parkinggarage.repository.BayRepository;
import com.liatrio.parkinggarage.repository.ParkingSpotRepository;
import com.liatrio.parkinggarage.repository.SpotTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSpotServiceTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private BayRepository bayRepository;

    @Mock
    private SpotTypeRepository spotTypeRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private ParkingSpotService parkingSpotService;

    private Floor floor;
    private Bay bay;
    private SpotType spotType;
    private ParkingSpot parkingSpot;
    private ParkingSpotDto parkingSpotDto;

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

        spotType = SpotType.builder()
                .id(1L)
                .name("REGULAR")
                .description("Regular sized parking spot")
                .active(true)
                .build();

        parkingSpot = ParkingSpot.builder()
                .id(1L)
                .spotIdentifier("F1-A-01")
                .spotNumber("01")
                .spotType(spotType)
                .bay(bay)
                .active(true)
                .transactions(new ArrayList<>())
                .build();

        parkingSpotDto = ParkingSpotDto.builder()
                .id(1L)
                .spotIdentifier("F1-A-01")
                .spotNumber("01")
                .spotTypeId(1L)
                .spotTypeName("REGULAR")
                .bayId(1L)
                .bayIdentifier("A")
                .floorNumber(1)
                .active(true)
                .occupied(false)
                .build();
    }

    @Test
    void getAllParkingSpots_ShouldReturnAllSpots() {
        // Arrange
        List<ParkingSpot> spots = List.of(parkingSpot);
        when(parkingSpotRepository.findAll()).thenReturn(spots);
        when(entityMapper.toParkingSpotDto(parkingSpot)).thenReturn(parkingSpotDto);

        // Act
        List<ParkingSpotDto> result = parkingSpotService.getAllParkingSpots();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(parkingSpotRepository, times(1)).findAll();
    }

    @Test
    void getAvailableParkingSpots_ShouldReturnAvailableSpots() {
        // Arrange
        List<ParkingSpot> spots = List.of(parkingSpot);
        when(parkingSpotRepository.findAvailableSpots()).thenReturn(spots);
        when(entityMapper.toParkingSpotDto(parkingSpot)).thenReturn(parkingSpotDto);

        // Act
        List<ParkingSpotDto> result = parkingSpotService.getAvailableParkingSpots();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(parkingSpotRepository, times(1)).findAvailableSpots();
    }

    @Test
    void getParkingSpotById_WhenSpotExists_ShouldReturnSpot() {
        // Arrange
        when(parkingSpotRepository.findById(1L)).thenReturn(Optional.of(parkingSpot));
        when(entityMapper.toParkingSpotDto(parkingSpot)).thenReturn(parkingSpotDto);

        // Act
        ParkingSpotDto result = parkingSpotService.getParkingSpotById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("F1-A-01", result.getSpotIdentifier());
        verify(parkingSpotRepository, times(1)).findById(1L);
    }

    @Test
    void getParkingSpotById_WhenSpotDoesNotExist_ShouldThrowException() {
        // Arrange
        when(parkingSpotRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
                parkingSpotService.getParkingSpotById(1L));
    }

    @Test
    void createParkingSpot_WhenValidData_ShouldCreateSpot() {
        // Arrange
        when(bayRepository.findById(1L)).thenReturn(Optional.of(bay));
        when(spotTypeRepository.findById(1L)).thenReturn(Optional.of(spotType));
        when(parkingSpotRepository.findBySpotIdentifier("F1-A-01")).thenReturn(Optional.empty());
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(parkingSpot);
        when(entityMapper.toParkingSpotDto(parkingSpot)).thenReturn(parkingSpotDto);

        // Act
        ParkingSpotDto result = parkingSpotService.createParkingSpot(parkingSpotDto);

        // Assert
        assertNotNull(result);
        assertEquals("F1-A-01", result.getSpotIdentifier());
        verify(parkingSpotRepository, times(1)).save(any(ParkingSpot.class));
    }

    @Test
    void createParkingSpot_WhenSpotAlreadyExists_ShouldThrowException() {
        // Arrange
        when(bayRepository.findById(1L)).thenReturn(Optional.of(bay));
        when(spotTypeRepository.findById(1L)).thenReturn(Optional.of(spotType));
        when(parkingSpotRepository.findBySpotIdentifier("F1-A-01")).thenReturn(Optional.of(parkingSpot));

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> 
                parkingSpotService.createParkingSpot(parkingSpotDto));
    }

    @Test
    void updateParkingSpot_WhenValidData_ShouldUpdateSpot() {
        // Arrange
        when(parkingSpotRepository.findById(1L)).thenReturn(Optional.of(parkingSpot));
        when(bayRepository.findById(1L)).thenReturn(Optional.of(bay));
        when(spotTypeRepository.findById(1L)).thenReturn(Optional.of(spotType));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(parkingSpot);
        when(entityMapper.toParkingSpotDto(parkingSpot)).thenReturn(parkingSpotDto);

        // Act
        ParkingSpotDto result = parkingSpotService.updateParkingSpot(1L, parkingSpotDto);

        // Assert
        assertNotNull(result);
        verify(parkingSpotRepository, times(1)).save(any(ParkingSpot.class));
    }

    @Test
    void deleteParkingSpot_WhenSpotExists_ShouldSoftDeleteSpot() {
        // Arrange
        when(parkingSpotRepository.findById(1L)).thenReturn(Optional.of(parkingSpot));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(parkingSpot);

        // Act
        parkingSpotService.deleteParkingSpot(1L);

        // Assert
        verify(parkingSpotRepository, times(1)).save(parkingSpot);
        assertFalse(parkingSpot.getActive());
    }
}
