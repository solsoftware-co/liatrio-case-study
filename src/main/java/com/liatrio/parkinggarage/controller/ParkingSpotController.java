package com.liatrio.parkinggarage.controller;

import com.liatrio.parkinggarage.dto.ParkingSpotDto;
import com.liatrio.parkinggarage.service.ParkingSpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-spots")
@RequiredArgsConstructor
@Tag(name = "Parking Spots", description = "Parking spot management APIs")
public class ParkingSpotController {

    private final ParkingSpotService parkingSpotService;

    @GetMapping
    @Operation(summary = "Get all parking spots")
    public ResponseEntity<List<ParkingSpotDto>> getAllParkingSpots() {
        return ResponseEntity.ok(parkingSpotService.getAllParkingSpots());
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available parking spots")
    public ResponseEntity<List<ParkingSpotDto>> getAvailableParkingSpots() {
        return ResponseEntity.ok(parkingSpotService.getAvailableParkingSpots());
    }

    @GetMapping("/occupied")
    @Operation(summary = "Get all occupied parking spots")
    public ResponseEntity<List<ParkingSpotDto>> getOccupiedParkingSpots() {
        return ResponseEntity.ok(parkingSpotService.getOccupiedParkingSpots());
    }

    @GetMapping("/available/floor/{floorId}")
    @Operation(summary = "Get available parking spots by floor")
    public ResponseEntity<List<ParkingSpotDto>> getAvailableSpotsByFloor(@PathVariable Long floorId) {
        return ResponseEntity.ok(parkingSpotService.getAvailableSpotsByFloor(floorId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get parking spot by ID")
    public ResponseEntity<ParkingSpotDto> getParkingSpotById(@PathVariable Long id) {
        return ResponseEntity.ok(parkingSpotService.getParkingSpotById(id));
    }

    @GetMapping("/identifier/{spotIdentifier}")
    @Operation(summary = "Get parking spot by identifier")
    public ResponseEntity<ParkingSpotDto> getParkingSpotByIdentifier(@PathVariable String spotIdentifier) {
        return ResponseEntity.ok(parkingSpotService.getParkingSpotByIdentifier(spotIdentifier));
    }

    @PostMapping
    @Operation(summary = "Create a new parking spot")
    public ResponseEntity<ParkingSpotDto> createParkingSpot(@Valid @RequestBody ParkingSpotDto spotDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.createParkingSpot(spotDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing parking spot")
    public ResponseEntity<ParkingSpotDto> updateParkingSpot(@PathVariable Long id, @Valid @RequestBody ParkingSpotDto spotDto) {
        return ResponseEntity.ok(parkingSpotService.updateParkingSpot(id, spotDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a parking spot (soft delete)")
    public ResponseEntity<Void> deleteParkingSpot(@PathVariable Long id) {
        parkingSpotService.deleteParkingSpot(id);
        return ResponseEntity.noContent().build();
    }
}
