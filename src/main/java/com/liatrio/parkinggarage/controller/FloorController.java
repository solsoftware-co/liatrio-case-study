package com.liatrio.parkinggarage.controller;

import com.liatrio.parkinggarage.dto.FloorDto;
import com.liatrio.parkinggarage.service.FloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/floors")
@RequiredArgsConstructor
@Tag(name = "Floors", description = "Floor management APIs")
public class FloorController {

    private final FloorService floorService;

    @GetMapping
    @Operation(summary = "Get all floors")
    public ResponseEntity<List<FloorDto>> getAllFloors() {
        return ResponseEntity.ok(floorService.getAllFloors());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active floors")
    public ResponseEntity<List<FloorDto>> getActiveFloors() {
        return ResponseEntity.ok(floorService.getActiveFloors());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get floor by ID")
    public ResponseEntity<FloorDto> getFloorById(@PathVariable Long id) {
        return ResponseEntity.ok(floorService.getFloorById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new floor")
    public ResponseEntity<FloorDto> createFloor(@Valid @RequestBody FloorDto floorDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(floorService.createFloor(floorDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing floor")
    public ResponseEntity<FloorDto> updateFloor(@PathVariable Long id, @Valid @RequestBody FloorDto floorDto) {
        return ResponseEntity.ok(floorService.updateFloor(id, floorDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a floor (soft delete)")
    public ResponseEntity<Void> deleteFloor(@PathVariable Long id) {
        floorService.deleteFloor(id);
        return ResponseEntity.noContent().build();
    }
}
