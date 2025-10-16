package com.liatrio.parkinggarage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingConfigDto {
    
    private Double hourlyRate;
    
    private Double minimumCharge;
    
    private Integer gracePeriodMinutes;
}
