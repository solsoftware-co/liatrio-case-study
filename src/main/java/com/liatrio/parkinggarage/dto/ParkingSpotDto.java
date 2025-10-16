package com.liatrio.parkinggarage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpotDto {
    
    private Long id;
    
    @NotBlank(message = "Spot identifier is required")
    private String spotIdentifier;
    
    @NotBlank(message = "Spot number is required")
    private String spotNumber;
    
    @NotNull(message = "Spot type ID is required")
    private Long spotTypeId;
    
    private String spotTypeName;
    
    @NotNull(message = "Bay ID is required")
    private Long bayId;
    
    private String bayIdentifier;
    
    private Integer floorNumber;
    
    private Boolean active;
    
    private Boolean occupied;
    
    private Long currentTransactionId;
}
