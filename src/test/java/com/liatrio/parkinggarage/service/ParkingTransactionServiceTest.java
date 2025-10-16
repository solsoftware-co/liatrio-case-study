package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.CheckInRequest;
import com.liatrio.parkinggarage.dto.CheckOutRequest;
import com.liatrio.parkinggarage.dto.ParkingTransactionDto;
import com.liatrio.parkinggarage.entity.*;
import com.liatrio.parkinggarage.exception.BusinessLogicException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.mapper.EntityMapper;
import com.liatrio.parkinggarage.repository.CarRepository;
import com.liatrio.parkinggarage.repository.ParkingSpotRepository;
import com.liatrio.parkinggarage.repository.ParkingTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingTransactionServiceTest {

    @Mock
    private ParkingTransactionRepository transactionRepository;

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private ParkingTransactionService parkingTransactionService;

    private Floor floor;
    private Bay bay;
    private SpotType spotType;
    private ParkingSpot parkingSpot;
    private Car car;
    private ParkingTransaction transaction;
    private CheckInRequest checkInRequest;
    private CheckOutRequest checkOutRequest;
    private ParkingTransactionDto transactionDto;

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

        car = Car.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .make("Toyota")
                .model("Camry")
                .color("Blue")
                .transactions(new ArrayList<>())
                .build();

        transaction = ParkingTransaction.builder()
                .id(1L)
                .car(car)
                .parkingSpot(parkingSpot)
                .checkInTime(LocalDateTime.now())
                .build();

        checkInRequest = CheckInRequest.builder()
                .licensePlate("ABC-123")
                .spotIdentifier("F1-A-01")
                .make("Toyota")
                .model("Camry")
                .color("Blue")
                .build();

        checkOutRequest = CheckOutRequest.builder()
                .spotIdentifier("F1-A-01")
                .build();

        transactionDto = ParkingTransactionDto.builder()
                .id(1L)
                .carId(1L)
                .licensePlate("ABC-123")
                .parkingSpotId(1L)
                .spotIdentifier("F1-A-01")
                .active(true)
                .build();
    }

    @Test
    void checkIn_WhenValidRequest_ShouldCreateTransaction() {
        // Arrange
        when(parkingSpotRepository.findBySpotIdentifier("F1-A-01")).thenReturn(Optional.of(parkingSpot));
        when(carRepository.findByLicensePlate("ABC-123")).thenReturn(Optional.of(car));
        when(transactionRepository.save(any(ParkingTransaction.class))).thenReturn(transaction);
        when(entityMapper.toTransactionDto(transaction)).thenReturn(transactionDto);

        // Act
        ParkingTransactionDto result = parkingTransactionService.checkIn(checkInRequest);

        // Assert
        assertNotNull(result);
        assertEquals("ABC-123", result.getLicensePlate());
        verify(transactionRepository, times(1)).save(any(ParkingTransaction.class));
    }

    @Test
    void checkIn_WhenSpotNotFound_ShouldThrowException() {
        // Arrange
        when(parkingSpotRepository.findBySpotIdentifier("F1-A-01")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
                parkingTransactionService.checkIn(checkInRequest));
    }

    @Test
    void checkIn_WhenSpotOccupied_ShouldThrowException() {
        // Arrange
        ParkingTransaction activeTransaction = ParkingTransaction.builder()
                .id(2L)
                .car(car)
                .parkingSpot(parkingSpot)
                .checkInTime(LocalDateTime.now())
                .build();
        parkingSpot.getTransactions().add(activeTransaction);
        
        when(parkingSpotRepository.findBySpotIdentifier("F1-A-01")).thenReturn(Optional.of(parkingSpot));

        // Act & Assert
        assertThrows(BusinessLogicException.class, () -> 
                parkingTransactionService.checkIn(checkInRequest));
    }

    @Test
    void checkIn_WhenCarNotExists_ShouldCreateCarAndTransaction() {
        // Arrange
        when(parkingSpotRepository.findBySpotIdentifier("F1-A-01")).thenReturn(Optional.of(parkingSpot));
        when(carRepository.findByLicensePlate("ABC-123")).thenReturn(Optional.empty());
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(transactionRepository.save(any(ParkingTransaction.class))).thenReturn(transaction);
        when(entityMapper.toTransactionDto(transaction)).thenReturn(transactionDto);

        // Act
        ParkingTransactionDto result = parkingTransactionService.checkIn(checkInRequest);

        // Assert
        assertNotNull(result);
        verify(carRepository, times(1)).save(any(Car.class));
        verify(transactionRepository, times(1)).save(any(ParkingTransaction.class));
    }

    @Test
    void checkOut_WhenValidRequest_ShouldCompleteTransaction() {
        // Arrange
        when(parkingSpotRepository.findBySpotIdentifier("F1-A-01")).thenReturn(Optional.of(parkingSpot));
        when(transactionRepository.findByParkingSpotIdAndCheckOutTimeIsNull(1L))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(ParkingTransaction.class))).thenReturn(transaction);
        when(entityMapper.toTransactionDto(transaction)).thenReturn(transactionDto);

        // Act
        ParkingTransactionDto result = parkingTransactionService.checkOut(checkOutRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(transaction.getCheckOutTime());
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void checkOut_WhenNoActiveTransaction_ShouldThrowException() {
        // Arrange
        when(parkingSpotRepository.findBySpotIdentifier("F1-A-01")).thenReturn(Optional.of(parkingSpot));
        when(transactionRepository.findByParkingSpotIdAndCheckOutTimeIsNull(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessLogicException.class, () -> 
                parkingTransactionService.checkOut(checkOutRequest));
    }

    @Test
    void checkOutByLicensePlate_WhenCarParked_ShouldCompleteTransaction() {
        // Arrange
        when(carRepository.findByLicensePlate("ABC-123")).thenReturn(Optional.of(car));
        when(transactionRepository.findByCarIdAndCheckOutTimeIsNull(1L))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(ParkingTransaction.class))).thenReturn(transaction);
        when(entityMapper.toTransactionDto(transaction)).thenReturn(transactionDto);

        // Act
        ParkingTransactionDto result = parkingTransactionService.checkOutByLicensePlate("ABC-123");

        // Assert
        assertNotNull(result);
        assertNotNull(transaction.getCheckOutTime());
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void checkOutByLicensePlate_WhenCarNotParked_ShouldThrowException() {
        // Arrange
        when(carRepository.findByLicensePlate("ABC-123")).thenReturn(Optional.of(car));
        when(transactionRepository.findByCarIdAndCheckOutTimeIsNull(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessLogicException.class, () -> 
                parkingTransactionService.checkOutByLicensePlate("ABC-123"));
    }
}
