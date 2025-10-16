package com.liatrio.parkinggarage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingTransactionDto {
    
    private Long id;
    
    private Long carId;
    
    private String licensePlate;
    
    private Long parkingSpotId;
    
    private String spotIdentifier;
    
    private Integer floorNumber;
    
    private String bayIdentifier;
    
    private String spotNumber;
    
    private LocalDateTime checkInTime;
    
    private LocalDateTime checkOutTime;
    
    private Double parkingFee;
    
    private Double durationInHours;
    
    private String notes;
    
    private Boolean active;
}
