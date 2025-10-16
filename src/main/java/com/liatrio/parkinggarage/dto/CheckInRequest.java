package com.liatrio.parkinggarage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInRequest {
    
    @NotBlank(message = "License plate is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "License plate must contain only uppercase letters, numbers, and hyphens")
    private String licensePlate;
    
    @NotBlank(message = "Spot identifier is required")
    private String spotIdentifier;
    
    private String make;
    
    private String model;
    
    private String color;
    
    private String notes;
}
