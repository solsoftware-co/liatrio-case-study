package com.liatrio.parkinggarage.controller;

import com.liatrio.parkinggarage.dto.BayDto;
import com.liatrio.parkinggarage.service.BayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bays")
@RequiredArgsConstructor
@Tag(name = "Bays", description = "Bay management APIs")
public class BayController {

    private final BayService bayService;

    @GetMapping
    @Operation(summary = "Get all bays")
    public ResponseEntity<List<BayDto>> getAllBays() {
        return ResponseEntity.ok(bayService.getAllBays());
    }

    @GetMapping("/floor/{floorId}")
    @Operation(summary = "Get bays by floor ID")
    public ResponseEntity<List<BayDto>> getBaysByFloorId(@PathVariable Long floorId) {
        return ResponseEntity.ok(bayService.getBaysByFloorId(floorId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bay by ID")
    public ResponseEntity<BayDto> getBayById(@PathVariable Long id) {
        return ResponseEntity.ok(bayService.getBayById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new bay")
    public ResponseEntity<BayDto> createBay(@Valid @RequestBody BayDto bayDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bayService.createBay(bayDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing bay")
    public ResponseEntity<BayDto> updateBay(@PathVariable Long id, @Valid @RequestBody BayDto bayDto) {
        return ResponseEntity.ok(bayService.updateBay(id, bayDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bay (soft delete)")
    public ResponseEntity<Void> deleteBay(@PathVariable Long id) {
        bayService.deleteBay(id);
        return ResponseEntity.noContent().build();
    }
}
