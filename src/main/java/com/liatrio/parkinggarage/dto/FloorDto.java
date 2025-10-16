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
public class FloorDto {
    
    private Long id;
    
    @NotNull(message = "Floor number is required")
    private Integer floorNumber;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private Boolean active;
}
