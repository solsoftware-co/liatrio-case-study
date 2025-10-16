package com.liatrio.parkinggarage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckOutRequest {
    
    @NotBlank(message = "Spot identifier is required")
    private String spotIdentifier;
    
    private String notes;
}
