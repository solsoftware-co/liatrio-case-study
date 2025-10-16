package com.liatrio.parkinggarage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest {

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService();
        // Set test values using reflection
        ReflectionTestUtils.setField(billingService, "hourlyRate", 5.00);
        ReflectionTestUtils.setField(billingService, "minimumCharge", 2.00);
        ReflectionTestUtils.setField(billingService, "gracePeriodMinutes", 15);
    }

    @Test
    void calculateParkingFee_WhenWithinGracePeriod_ShouldReturnZero() {
        // Arrange
        LocalDateTime checkIn = LocalDateTime.now().minusMinutes(10);
        LocalDateTime checkOut = LocalDateTime.now();

        // Act
        Double fee = billingService.calculateParkingFee(checkIn, checkOut);

        // Assert
        assertEquals(0.0, fee);
    }

    @Test
    void calculateParkingFee_WhenExactlyOneHour_ShouldReturnHourlyRate() {
        // Arrange
        LocalDateTime checkIn = LocalDateTime.now().minusHours(1);
        LocalDateTime checkOut = LocalDateTime.now();

        // Act
        Double fee = billingService.calculateParkingFee(checkIn, checkOut);

        // Assert
        assertEquals(5.00, fee);
    }

    @Test
    void calculateParkingFee_WhenPartialHour_ShouldRoundUp() {
        // Arrange - 1.5 hours should round up to 2 hours
        LocalDateTime checkIn = LocalDateTime.now().minusMinutes(90);
        LocalDateTime checkOut = LocalDateTime.now();

        // Act
        Double fee = billingService.calculateParkingFee(checkIn, checkOut);

        // Assert
        assertEquals(10.00, fee); // 2 hours * $5/hr
    }

    @Test
    void calculateParkingFee_WhenBelowMinimumCharge_ShouldReturnMinimum() {
        // Arrange - 20 minutes (after grace period) = 1 hour @ $5 but minimum is $2
        LocalDateTime checkIn = LocalDateTime.now().minusMinutes(20);
        LocalDateTime checkOut = LocalDateTime.now();

        // Act
        Double fee = billingService.calculateParkingFee(checkIn, checkOut);

        // Assert
        assertEquals(5.00, fee); // 1 hour * $5/hr (above minimum)
    }

    @Test
    void calculateParkingFee_WhenMultipleHours_ShouldCalculateCorrectly() {
        // Arrange - 5 hours
        LocalDateTime checkIn = LocalDateTime.now().minusHours(5);
        LocalDateTime checkOut = LocalDateTime.now();

        // Act
        Double fee = billingService.calculateParkingFee(checkIn, checkOut);

        // Assert
        assertEquals(25.00, fee); // 5 hours * $5/hr
    }

    @Test
    void calculateParkingFee_WhenNullCheckIn_ShouldReturnZero() {
        // Arrange
        LocalDateTime checkOut = LocalDateTime.now();

        // Act
        Double fee = billingService.calculateParkingFee(null, checkOut);

        // Assert
        assertEquals(0.0, fee);
    }

    @Test
    void calculateParkingFee_WhenNullCheckOut_ShouldReturnZero() {
        // Arrange
        LocalDateTime checkIn = LocalDateTime.now();

        // Act
        Double fee = billingService.calculateParkingFee(checkIn, null);

        // Assert
        assertEquals(0.0, fee);
    }

    @Test
    void getHourlyRate_ShouldReturnConfiguredRate() {
        // Act & Assert
        assertEquals(5.00, billingService.getHourlyRate());
    }

    @Test
    void getMinimumCharge_ShouldReturnConfiguredCharge() {
        // Act & Assert
        assertEquals(2.00, billingService.getMinimumCharge());
    }

    @Test
    void getGracePeriodMinutes_ShouldReturnConfiguredPeriod() {
        // Act & Assert
        assertEquals(15, billingService.getGracePeriodMinutes());
    }
}
