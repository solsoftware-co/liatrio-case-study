package com.liatrio.parkinggarage.mapper;

import com.liatrio.parkinggarage.dto.*;
import com.liatrio.parkinggarage.entity.*;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public FloorDto toFloorDto(Floor floor) {
        return FloorDto.builder()
                .id(floor.getId())
                .floorNumber(floor.getFloorNumber())
                .name(floor.getName())
                .active(floor.getActive())
                .build();
    }

    public BayDto toBayDto(Bay bay) {
        return BayDto.builder()
                .id(bay.getId())
                .bayIdentifier(bay.getBayIdentifier())
                .name(bay.getName())
                .floorId(bay.getFloor().getId())
                .floorNumber(bay.getFloor().getFloorNumber())
                .active(bay.getActive())
                .build();
    }

    public ParkingSpotDto toParkingSpotDto(ParkingSpot spot) {
        ParkingTransaction activeTransaction = spot.getActiveTransaction();
        
        return ParkingSpotDto.builder()
                .id(spot.getId())
                .spotIdentifier(spot.getSpotIdentifier())
                .spotNumber(spot.getSpotNumber())
                .spotTypeId(spot.getSpotType().getId())
                .spotTypeName(spot.getSpotType().getName())
                .bayId(spot.getBay().getId())
                .bayIdentifier(spot.getBay().getBayIdentifier())
                .floorNumber(spot.getBay().getFloor().getFloorNumber())
                .active(spot.getActive())
                .occupied(spot.isOccupied())
                .currentTransactionId(activeTransaction != null ? activeTransaction.getId() : null)
                .build();
    }

    public CarDto toCarDto(Car car) {
        ParkingTransaction currentTransaction = car.getCurrentTransaction();
        
        return CarDto.builder()
                .id(car.getId())
                .licensePlate(car.getLicensePlate())
                .make(car.getMake())
                .model(car.getModel())
                .color(car.getColor())
                .currentlyParked(car.isCurrentlyParked())
                .currentSpotId(currentTransaction != null ? currentTransaction.getParkingSpot().getId() : null)
                .currentSpotIdentifier(currentTransaction != null ? currentTransaction.getParkingSpot().getSpotIdentifier() : null)
                .build();
    }

    public ParkingTransactionDto toTransactionDto(ParkingTransaction transaction) {
        return ParkingTransactionDto.builder()
                .id(transaction.getId())
                .carId(transaction.getCar().getId())
                .licensePlate(transaction.getCar().getLicensePlate())
                .parkingSpotId(transaction.getParkingSpot().getId())
                .spotIdentifier(transaction.getParkingSpot().getSpotIdentifier())
                .floorNumber(transaction.getParkingSpot().getBay().getFloor().getFloorNumber())
                .bayIdentifier(transaction.getParkingSpot().getBay().getBayIdentifier())
                .spotNumber(transaction.getParkingSpot().getSpotNumber())
                .checkInTime(transaction.getCheckInTime())
                .checkOutTime(transaction.getCheckOutTime())
                .notes(transaction.getNotes())
                .active(transaction.isActive())
                .build();
    }
}
