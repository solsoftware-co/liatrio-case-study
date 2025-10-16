package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.CarDto;
import com.liatrio.parkinggarage.entity.Car;
import com.liatrio.parkinggarage.exception.ResourceAlreadyExistsException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.mapper.EntityMapper;
import com.liatrio.parkinggarage.repository.CarRepository;
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
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private CarService carService;

    private Car car;
    private CarDto carDto;

    @BeforeEach
    void setUp() {
        car = Car.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .make("Toyota")
                .model("Camry")
                .color("Blue")
                .build();

        carDto = CarDto.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .make("Toyota")
                .model("Camry")
                .color("Blue")
                .currentlyParked(false)
                .build();
    }

    @Test
    void getAllCars_ShouldReturnAllCars() {
        // Arrange
        when(carRepository.findAll()).thenReturn(Arrays.asList(car));
        when(entityMapper.toCarDto(car)).thenReturn(carDto);

        // Act
        List<CarDto> result = carService.getAllCars();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ABC-123", result.get(0).getLicensePlate());
        verify(carRepository, times(1)).findAll();
    }

    @Test
    void getCurrentlyParkedCars_ShouldReturnParkedCars() {
        // Arrange
        CarDto parkedCarDto = CarDto.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .make("Toyota")
                .currentlyParked(true)
                .build();

        when(carRepository.findCurrentlyParkedCars()).thenReturn(Arrays.asList(car));
        when(entityMapper.toCarDto(car)).thenReturn(parkedCarDto);

        // Act
        List<CarDto> result = carService.getCurrentlyParkedCars();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getCurrentlyParked());
        verify(carRepository, times(1)).findCurrentlyParkedCars();
    }

    @Test
    void getCarById_WhenExists_ShouldReturnCar() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(entityMapper.toCarDto(car)).thenReturn(carDto);

        // Act
        CarDto result = carService.getCarById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(carRepository, times(1)).findById(1L);
    }

    @Test
    void getCarById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> carService.getCarById(1L));
    }

    @Test
    void getCarByLicensePlate_WhenExists_ShouldReturnCar() {
        // Arrange
        when(carRepository.findByLicensePlate("ABC-123")).thenReturn(Optional.of(car));
        when(entityMapper.toCarDto(car)).thenReturn(carDto);

        // Act
        CarDto result = carService.getCarByLicensePlate("ABC-123");

        // Assert
        assertNotNull(result);
        assertEquals("ABC-123", result.getLicensePlate());
        verify(carRepository, times(1)).findByLicensePlate("ABC-123");
    }

    @Test
    void getCarByLicensePlate_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(carRepository.findByLicensePlate("ABC-123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> carService.getCarByLicensePlate("ABC-123"));
    }

    @Test
    void createCar_WhenValidData_ShouldCreateCar() {
        // Arrange
        when(carRepository.existsByLicensePlate("ABC-123")).thenReturn(false);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(entityMapper.toCarDto(car)).thenReturn(carDto);

        // Act
        CarDto result = carService.createCar(carDto);

        // Assert
        assertNotNull(result);
        assertEquals("ABC-123", result.getLicensePlate());
        verify(carRepository, times(1)).save(any(Car.class));
    }

    @Test
    void createCar_WhenLicensePlateExists_ShouldThrowException() {
        // Arrange
        when(carRepository.existsByLicensePlate("ABC-123")).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> carService.createCar(carDto));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void updateCar_WhenValidData_ShouldUpdateCar() {
        // Arrange
        CarDto updateDto = CarDto.builder()
                .licensePlate("ABC-123")
                .make("Honda")
                .model("Accord")
                .color("Red")
                .build();

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(entityMapper.toCarDto(any(Car.class))).thenReturn(updateDto);

        // Act
        CarDto result = carService.updateCar(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(carRepository, times(1)).save(any(Car.class));
    }

    @Test
    void updateCar_WhenCarNotExists_ShouldThrowException() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> carService.updateCar(1L, carDto));
    }

    @Test
    void updateCar_WhenLicensePlateChanged_AndExists_ShouldThrowException() {
        // Arrange
        CarDto updateDto = CarDto.builder()
                .licensePlate("XYZ-789")
                .make("Toyota")
                .build();

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carRepository.existsByLicensePlate("XYZ-789")).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> carService.updateCar(1L, updateDto));
    }

    @Test
    void deleteCar_WhenExists_ShouldDeleteCar() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        doNothing().when(carRepository).delete(car);

        // Act
        carService.deleteCar(1L);

        // Assert
        verify(carRepository, times(1)).delete(car);
    }

    @Test
    void deleteCar_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> carService.deleteCar(1L));
        verify(carRepository, never()).delete(any(Car.class));
    }
}
