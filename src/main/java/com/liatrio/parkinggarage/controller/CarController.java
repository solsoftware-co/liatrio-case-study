package com.liatrio.parkinggarage.controller;

import com.liatrio.parkinggarage.dto.CarDto;
import com.liatrio.parkinggarage.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
@Tag(name = "Cars", description = "Car management APIs")
public class CarController {

    private final CarService carService;

    @GetMapping
    @Operation(summary = "Get all cars")
    public ResponseEntity<List<CarDto>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    @GetMapping("/parked")
    @Operation(summary = "Get all currently parked cars")
    public ResponseEntity<List<CarDto>> getCurrentlyParkedCars() {
        return ResponseEntity.ok(carService.getCurrentlyParkedCars());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car by ID")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getCarById(id));
    }

    @GetMapping("/license-plate/{licensePlate}")
    @Operation(summary = "Get car by license plate")
    public ResponseEntity<CarDto> getCarByLicensePlate(@PathVariable String licensePlate) {
        return ResponseEntity.ok(carService.getCarByLicensePlate(licensePlate));
    }

    @PostMapping
    @Operation(summary = "Create a new car")
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarDto carDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.createCar(carDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing car")
    public ResponseEntity<CarDto> updateCar(@PathVariable Long id, @Valid @RequestBody CarDto carDto) {
        return ResponseEntity.ok(carService.updateCar(id, carDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a car")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }
}
