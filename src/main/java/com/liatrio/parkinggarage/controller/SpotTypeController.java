package com.liatrio.parkinggarage.controller;

import com.liatrio.parkinggarage.dto.SpotTypeDto;
import com.liatrio.parkinggarage.service.SpotTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spot-types")
@RequiredArgsConstructor
@Tag(name = "Spot Types", description = "Spot type management APIs")
public class SpotTypeController {

    private final SpotTypeService spotTypeService;

    @GetMapping
    @Operation(summary = "Get all spot types")
    public ResponseEntity<List<SpotTypeDto>> getAllSpotTypes() {
        return ResponseEntity.ok(spotTypeService.getAllSpotTypes());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active spot types")
    public ResponseEntity<List<SpotTypeDto>> getActiveSpotTypes() {
        return ResponseEntity.ok(spotTypeService.getActiveSpotTypes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get spot type by ID")
    public ResponseEntity<SpotTypeDto> getSpotTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(spotTypeService.getSpotTypeById(id));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get spot type by name")
    public ResponseEntity<SpotTypeDto> getSpotTypeByName(@PathVariable String name) {
        return ResponseEntity.ok(spotTypeService.getSpotTypeByName(name));
    }

    @PostMapping
    @Operation(summary = "Create a new spot type")
    public ResponseEntity<SpotTypeDto> createSpotType(@Valid @RequestBody SpotTypeDto spotTypeDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spotTypeService.createSpotType(spotTypeDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing spot type")
    public ResponseEntity<SpotTypeDto> updateSpotType(@PathVariable Long id, @Valid @RequestBody SpotTypeDto spotTypeDto) {
        return ResponseEntity.ok(spotTypeService.updateSpotType(id, spotTypeDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a spot type (soft delete)")
    public ResponseEntity<Void> deleteSpotType(@PathVariable Long id) {
        spotTypeService.deleteSpotType(id);
        return ResponseEntity.noContent().build();
    }
}
