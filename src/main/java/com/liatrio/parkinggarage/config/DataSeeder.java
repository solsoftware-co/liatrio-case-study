package com.liatrio.parkinggarage.config;

import com.liatrio.parkinggarage.entity.*;
import com.liatrio.parkinggarage.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    @Bean
    @Profile("dev")
    public CommandLineRunner seedData(
            FloorRepository floorRepository,
            BayRepository bayRepository,
            ParkingSpotRepository parkingSpotRepository,
            SpotTypeRepository spotTypeRepository,
            CarRepository carRepository,
            ParkingTransactionRepository transactionRepository) {
        
        return args -> {
            log.info("Starting data seeding...");
            
            // Check if data already exists
            if (floorRepository.count() > 0) {
                log.info("Data already exists, skipping seeding");
                return;
            }
            
            // Create Spot Types
            SpotType regularType = SpotType.builder()
                    .name("REGULAR")
                    .description("Regular sized parking spot")
                    .active(true)
                    .build();
            
            SpotType compactType = SpotType.builder()
                    .name("COMPACT")
                    .description("Compact sized parking spot")
                    .active(true)
                    .build();
            
            SpotType largeType = SpotType.builder()
                    .name("LARGE")
                    .description("Large sized parking spot for trucks/SUVs")
                    .active(true)
                    .build();
            
            SpotType handicapType = SpotType.builder()
                    .name("HANDICAP")
                    .description("Handicap accessible parking spot")
                    .active(true)
                    .build();
            
            spotTypeRepository.saveAll(List.of(regularType, compactType, largeType, handicapType));
            log.info("Created 4 spot types");
            
            // Create Floors
            Floor floor1 = Floor.builder()
                    .floorNumber(1)
                    .name("Ground Floor")
                    .active(true)
                    .bays(new ArrayList<>())
                    .build();
            
            Floor floor2 = Floor.builder()
                    .floorNumber(2)
                    .name("Second Floor")
                    .active(true)
                    .bays(new ArrayList<>())
                    .build();
            
            Floor floor3 = Floor.builder()
                    .floorNumber(3)
                    .name("Third Floor")
                    .active(true)
                    .bays(new ArrayList<>())
                    .build();
            
            floorRepository.saveAll(List.of(floor1, floor2, floor3));
            log.info("Created 3 floors");
            
            // Create Bays for Floor 1
            Bay bay1A = createBay("A", "Bay A", floor1);
            Bay bay1B = createBay("B", "Bay B", floor1);
            Bay bay1C = createBay("C", "Bay C", floor1);
            
            // Create Bays for Floor 2
            Bay bay2A = createBay("A", "Bay A", floor2);
            Bay bay2B = createBay("B", "Bay B", floor2);
            Bay bay2C = createBay("C", "Bay C", floor2);
            
            // Create Bays for Floor 3
            Bay bay3A = createBay("A", "Bay A", floor3);
            Bay bay3B = createBay("B", "Bay B", floor3);
            
            bayRepository.saveAll(List.of(bay1A, bay1B, bay1C, bay2A, bay2B, bay2C, bay3A, bay3B));
            log.info("Created 8 bays");
            
            // Create Parking Spots
            List<ParkingSpot> spots = new ArrayList<>();
            
            // Floor 1 - Bay A (10 spots)
            spots.addAll(createSpotsForBay(bay1A, 1, 10, regularType));
            
            // Floor 1 - Bay B (8 spots, 2 handicap)
            spots.addAll(createSpotsForBay(bay1B, 1, 6, regularType));
            spots.addAll(createSpotsForBay(bay1B, 7, 8, handicapType));
            
            // Floor 1 - Bay C (10 spots, compact)
            spots.addAll(createSpotsForBay(bay1C, 1, 10, compactType));
            
            // Floor 2 - Bay A (10 spots)
            spots.addAll(createSpotsForBay(bay2A, 1, 10, regularType));
            
            // Floor 2 - Bay B (10 spots)
            spots.addAll(createSpotsForBay(bay2B, 1, 10, regularType));
            
            // Floor 2 - Bay C (5 large spots)
            spots.addAll(createSpotsForBay(bay2C, 1, 5, largeType));
            
            // Floor 3 - Bay A (15 spots)
            spots.addAll(createSpotsForBay(bay3A, 1, 15, regularType));
            
            // Floor 3 - Bay B (15 spots)
            spots.addAll(createSpotsForBay(bay3B, 1, 15, regularType));
            
            parkingSpotRepository.saveAll(spots);
            log.info("Created {} parking spots", spots.size());
            
            // Create some sample cars
            Car car1 = Car.builder()
                    .licensePlate("ABC-123")
                    .make("Toyota")
                    .model("Camry")
                    .color("Blue")
                    .build();
            
            Car car2 = Car.builder()
                    .licensePlate("XYZ-789")
                    .make("Honda")
                    .model("Civic")
                    .color("Red")
                    .build();
            
            Car car3 = Car.builder()
                    .licensePlate("DEF-456")
                    .make("Ford")
                    .model("F-150")
                    .color("Black")
                    .build();
            
            carRepository.saveAll(List.of(car1, car2, car3));
            log.info("Created 3 sample cars");
            
            // Create some active parking transactions
            ParkingSpot spot1 = spots.get(0);
            ParkingSpot spot2 = spots.get(15);
            
            ParkingTransaction transaction1 = ParkingTransaction.builder()
                    .car(car1)
                    .parkingSpot(spot1)
                    .checkInTime(LocalDateTime.now().minusHours(2))
                    .notes("Regular parking")
                    .build();
            
            ParkingTransaction transaction2 = ParkingTransaction.builder()
                    .car(car2)
                    .parkingSpot(spot2)
                    .checkInTime(LocalDateTime.now().minusMinutes(30))
                    .notes("Short term parking")
                    .build();
            
            transactionRepository.saveAll(List.of(transaction1, transaction2));
            log.info("Created 2 active parking transactions");
            
            // Create a completed transaction
            ParkingSpot spot3 = spots.get(5);
            ParkingTransaction completedTransaction = ParkingTransaction.builder()
                    .car(car3)
                    .parkingSpot(spot3)
                    .checkInTime(LocalDateTime.now().minusDays(1))
                    .checkOutTime(LocalDateTime.now().minusDays(1).plusHours(3))
                    .notes("Completed parking session")
                    .build();
            
            transactionRepository.save(completedTransaction);
            log.info("Created 1 completed transaction");
            
            log.info("Data seeding completed successfully!");
        };
    }
    
    private Bay createBay(String identifier, String name, Floor floor) {
        return Bay.builder()
                .bayIdentifier(identifier)
                .name(name)
                .floor(floor)
                .active(true)
                .parkingSpots(new ArrayList<>())
                .build();
    }
    
    private List<ParkingSpot> createSpotsForBay(Bay bay, int startNum, int endNum, SpotType spotType) {
        List<ParkingSpot> spots = new ArrayList<>();
        for (int i = startNum; i <= endNum; i++) {
            String spotNumber = String.format("%02d", i);
            String spotIdentifier = String.format("F%d-%s-%s", 
                    bay.getFloor().getFloorNumber(), 
                    bay.getBayIdentifier(), 
                    spotNumber);
            
            ParkingSpot spot = ParkingSpot.builder()
                    .spotIdentifier(spotIdentifier)
                    .spotNumber(spotNumber)
                    .spotType(spotType)
                    .bay(bay)
                    .active(true)
                    .transactions(new ArrayList<>())
                    .build();
            
            spots.add(spot);
        }
        return spots;
    }
}
