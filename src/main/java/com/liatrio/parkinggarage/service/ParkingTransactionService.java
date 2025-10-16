package com.liatrio.parkinggarage.service;

import com.liatrio.parkinggarage.dto.CheckInRequest;
import com.liatrio.parkinggarage.dto.CheckOutRequest;
import com.liatrio.parkinggarage.dto.ParkingTransactionDto;
import com.liatrio.parkinggarage.entity.Car;
import com.liatrio.parkinggarage.entity.ParkingSpot;
import com.liatrio.parkinggarage.entity.ParkingTransaction;
import com.liatrio.parkinggarage.exception.BusinessLogicException;
import com.liatrio.parkinggarage.exception.ResourceNotFoundException;
import com.liatrio.parkinggarage.mapper.EntityMapper;
import com.liatrio.parkinggarage.repository.CarRepository;
import com.liatrio.parkinggarage.repository.ParkingSpotRepository;
import com.liatrio.parkinggarage.repository.ParkingTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingTransactionService {

    private final ParkingTransactionRepository transactionRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final CarRepository carRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<ParkingTransactionDto> getAllTransactions() {
        log.debug("Fetching all transactions");
        return transactionRepository.findAll().stream()
                .map(entityMapper::toTransactionDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingTransactionDto> getActiveTransactions() {
        log.debug("Fetching active transactions");
        return transactionRepository.findActiveTransactions().stream()
                .map(entityMapper::toTransactionDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkingTransactionDto> getCompletedTransactions() {
        log.debug("Fetching completed transactions");
        return transactionRepository.findCompletedTransactions().stream()
                .map(entityMapper::toTransactionDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParkingTransactionDto getTransactionById(Long id) {
        log.debug("Fetching transaction with id: {}", id);
        ParkingTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ParkingTransaction", "id", id));
        return entityMapper.toTransactionDto(transaction);
    }

    @Transactional(readOnly = true)
    public List<ParkingTransactionDto> getTransactionsByCarId(Long carId) {
        log.debug("Fetching transactions for car: {}", carId);
        return transactionRepository.findByCarId(carId).stream()
                .map(entityMapper::toTransactionDto)
                .collect(Collectors.toList());
    }

    /**
     * Check in a car to a parking spot
     */
    @Transactional
    public ParkingTransactionDto checkIn(CheckInRequest request) {
        log.debug("Processing check-in for license plate: {} at spot: {}", 
                request.getLicensePlate(), request.getSpotIdentifier());
        
        // Validate parking spot
        ParkingSpot parkingSpot = parkingSpotRepository.findBySpotIdentifier(request.getSpotIdentifier())
                .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "spotIdentifier", request.getSpotIdentifier()));
        
        if (!parkingSpot.getActive()) {
            throw new BusinessLogicException("Parking spot " + request.getSpotIdentifier() + " is not active");
        }
        
        // Check if spot is already occupied
        if (parkingSpot.isOccupied()) {
            throw new BusinessLogicException("Parking spot " + request.getSpotIdentifier() + " is already occupied");
        }
        
        // Get or create car
        Car car = carRepository.findByLicensePlate(request.getLicensePlate())
                .orElseGet(() -> {
                    log.info("Creating new car with license plate: {}", request.getLicensePlate());
                    Car newCar = Car.builder()
                            .licensePlate(request.getLicensePlate())
                            .make(request.getMake())
                            .model(request.getModel())
                            .color(request.getColor())
                            .build();
                    return carRepository.save(newCar);
                });
        
        // Check if car is already parked elsewhere
        if (car.isCurrentlyParked()) {
            ParkingTransaction currentTransaction = car.getCurrentTransaction();
            throw new BusinessLogicException(
                    String.format("Car %s is already parked at spot %s", 
                            request.getLicensePlate(),
                            currentTransaction.getParkingSpot().getSpotIdentifier())
            );
        }
        
        // Create parking transaction
        ParkingTransaction transaction = ParkingTransaction.builder()
                .car(car)
                .parkingSpot(parkingSpot)
                .checkInTime(LocalDateTime.now())
                .notes(request.getNotes())
                .build();
        
        ParkingTransaction savedTransaction = transactionRepository.save(transaction);
        log.info("Car {} checked in at spot {} with transaction id: {}", 
                request.getLicensePlate(), request.getSpotIdentifier(), savedTransaction.getId());
        
        return entityMapper.toTransactionDto(savedTransaction);
    }

    /**
     * Check out a car from a parking spot
     */
    @Transactional
    public ParkingTransactionDto checkOut(CheckOutRequest request) {
        log.debug("Processing check-out for spot: {}", request.getSpotIdentifier());
        
        // Validate parking spot
        ParkingSpot parkingSpot = parkingSpotRepository.findBySpotIdentifier(request.getSpotIdentifier())
                .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "spotIdentifier", request.getSpotIdentifier()));
        
        // Get active transaction for this spot
        ParkingTransaction transaction = transactionRepository
                .findByParkingSpotIdAndCheckOutTimeIsNull(parkingSpot.getId())
                .orElseThrow(() -> new BusinessLogicException(
                        "No active parking session found for spot " + request.getSpotIdentifier()
                ));
        
        // Complete the transaction
        transaction.setCheckOutTime(LocalDateTime.now());
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            String existingNotes = transaction.getNotes();
            transaction.setNotes(existingNotes != null ? existingNotes + " | " + request.getNotes() : request.getNotes());
        }
        
        ParkingTransaction completedTransaction = transactionRepository.save(transaction);
        log.info("Car {} checked out from spot {} with transaction id: {}", 
                transaction.getCar().getLicensePlate(), request.getSpotIdentifier(), completedTransaction.getId());
        
        return entityMapper.toTransactionDto(completedTransaction);
    }

    /**
     * Check out by license plate (convenience method)
     */
    @Transactional
    public ParkingTransactionDto checkOutByLicensePlate(String licensePlate) {
        log.debug("Processing check-out for license plate: {}", licensePlate);
        
        // Find car
        Car car = carRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new ResourceNotFoundException("Car", "licensePlate", licensePlate));
        
        // Get active transaction for this car
        ParkingTransaction transaction = transactionRepository
                .findByCarIdAndCheckOutTimeIsNull(car.getId())
                .orElseThrow(() -> new BusinessLogicException(
                        "Car " + licensePlate + " is not currently parked"
                ));
        
        // Complete the transaction
        transaction.setCheckOutTime(LocalDateTime.now());
        
        ParkingTransaction completedTransaction = transactionRepository.save(transaction);
        log.info("Car {} checked out from spot {} with transaction id: {}", 
                licensePlate, transaction.getParkingSpot().getSpotIdentifier(), completedTransaction.getId());
        
        return entityMapper.toTransactionDto(completedTransaction);
    }
}
