package com.liatrio.parkinggarage.controller;

import com.liatrio.parkinggarage.dto.CheckInRequest;
import com.liatrio.parkinggarage.dto.CheckOutRequest;
import com.liatrio.parkinggarage.dto.ParkingTransactionDto;
import com.liatrio.parkinggarage.service.ParkingTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
@Tag(name = "Parking Operations", description = "Check-in and check-out operations")
public class ParkingController {

    private final ParkingTransactionService parkingTransactionService;

    @PostMapping("/check-in")
    @Operation(summary = "Check in a car to a parking spot")
    public ResponseEntity<ParkingTransactionDto> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingTransactionService.checkIn(request));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Check out a car from a parking spot")
    public ResponseEntity<ParkingTransactionDto> checkOut(@Valid @RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(parkingTransactionService.checkOut(request));
    }

    @PostMapping("/check-out/license-plate/{licensePlate}")
    @Operation(summary = "Check out a car by license plate")
    public ResponseEntity<ParkingTransactionDto> checkOutByLicensePlate(@PathVariable String licensePlate) {
        return ResponseEntity.ok(parkingTransactionService.checkOutByLicensePlate(licensePlate));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get all parking transactions")
    public ResponseEntity<List<ParkingTransactionDto>> getAllTransactions() {
        return ResponseEntity.ok(parkingTransactionService.getAllTransactions());
    }

    @GetMapping("/transactions/active")
    @Operation(summary = "Get all active parking transactions")
    public ResponseEntity<List<ParkingTransactionDto>> getActiveTransactions() {
        return ResponseEntity.ok(parkingTransactionService.getActiveTransactions());
    }

    @GetMapping("/transactions/completed")
    @Operation(summary = "Get all completed parking transactions")
    public ResponseEntity<List<ParkingTransactionDto>> getCompletedTransactions() {
        return ResponseEntity.ok(parkingTransactionService.getCompletedTransactions());
    }

    @GetMapping("/transactions/{id}")
    @Operation(summary = "Get parking transaction by ID")
    public ResponseEntity<ParkingTransactionDto> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(parkingTransactionService.getTransactionById(id));
    }

    @GetMapping("/transactions/car/{carId}")
    @Operation(summary = "Get parking transactions by car ID")
    public ResponseEntity<List<ParkingTransactionDto>> getTransactionsByCarId(@PathVariable Long carId) {
        return ResponseEntity.ok(parkingTransactionService.getTransactionsByCarId(carId));
    }
}
