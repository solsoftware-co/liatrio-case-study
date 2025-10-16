package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.CarDto;
import com.liatrio.parkinggarage.entity.Car;
import com.liatrio.parkinggarage.exception.ResourceAlreadyExistsException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.mapper.EntityMapper;
import com.liatrio.parkinggarage.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarService {

    private final CarRepository carRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<CarDto> getAllCars() {
        log.debug("Fetching all cars");
        return carRepository.findAll().stream()
                .map(entityMapper::toCarDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CarDto> getCurrentlyParkedCars() {
        log.debug("Fetching currently parked cars");
        return carRepository.findCurrentlyParkedCars().stream()
                .map(entityMapper::toCarDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CarDto getCarById(Long id) {
        log.debug("Fetching car with id: {}", id);
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));
        return entityMapper.toCarDto(car);
    }

    @Transactional(readOnly = true)
    public CarDto getCarByLicensePlate(String licensePlate) {
        log.debug("Fetching car with license plate: {}", licensePlate);
        Car car = carRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "licensePlate", licensePlate));
        return entityMapper.toCarDto(car);
    }

    @Transactional
    public CarDto createCar(CarDto carDto) {
        log.debug("Creating car: {}", carDto);
        
        if (carRepository.existsByLicensePlate(carDto.getLicensePlate())) {
            throw new ResourceAlreadyExistsException("Car", "licensePlate", carDto.getLicensePlate());
        }
        
        Car car = Car.builder()
                .licensePlate(carDto.getLicensePlate())
                .make(carDto.getMake())
                .model(carDto.getModel())
                .color(carDto.getColor())
                .build();
        
        Car savedCar = carRepository.save(car);
        log.info("Created car with id: {}", savedCar.getId());
        
        return entityMapper.toCarDto(savedCar);
    }

    @Transactional
    public CarDto updateCar(Long id, CarDto carDto) {
        log.debug("Updating car with id: {}", id);
        
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));
        
        // Check if license plate is being changed and if it already exists
        if (!car.getLicensePlate().equals(carDto.getLicensePlate()) &&
                carRepository.existsByLicensePlate(carDto.getLicensePlate())) {
            throw new ResourceAlreadyExistsException("Car", "licensePlate", carDto.getLicensePlate());
        }
        
        car.setLicensePlate(carDto.getLicensePlate());
        car.setMake(carDto.getMake());
        car.setModel(carDto.getModel());
        car.setColor(carDto.getColor());
        
        Car updatedCar = carRepository.save(car);
        log.info("Updated car with id: {}", updatedCar.getId());
        
        return entityMapper.toCarDto(updatedCar);
    }

    @Transactional
    public void deleteCar(Long id) {
        log.debug("Deleting car with id: {}", id);
        
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));
        
        carRepository.delete(car);
        
        log.info("Deleted car with id: {}", id);
    }
}
