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
public class BayDto {
    
    private Long id;
    
    @NotBlank(message = "Bay identifier is required")
    private String bayIdentifier;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Floor ID is required")
    private Long floorId;
    
    private Integer floorNumber;
    
    private Boolean active;
}
