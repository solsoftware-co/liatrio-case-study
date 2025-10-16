package com.liatrio.parkinggarage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
public class BillingService {

    @Value("${parking.billing.hourly-rate:5.00}")
    private Double hourlyRate;

    @Value("${parking.billing.minimum-charge:2.00}")
    private Double minimumCharge;

    @Value("${parking.billing.grace-period-minutes:15}")
    private Integer gracePeriodMinutes;

    /**
     * Calculate parking fee based on duration
     * 
     * @param checkInTime When the car was checked in
     * @param checkOutTime When the car was checked out
     * @return Calculated parking fee
     */
    public Double calculateParkingFee(LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        if (checkInTime == null || checkOutTime == null) {
            log.warn("Cannot calculate fee with null check-in or check-out time");
            return 0.0;
        }

        // Calculate duration
        Duration duration = Duration.between(checkInTime, checkOutTime);
        long totalMinutes = duration.toMinutes();

        // Apply grace period - free parking if under grace period
        if (totalMinutes <= gracePeriodMinutes) {
            log.debug("Parking duration {} minutes is within grace period of {} minutes - no charge", 
                    totalMinutes, gracePeriodMinutes);
            return 0.0;
        }

        // Calculate hours (round up partial hours)
        double hours = Math.ceil(totalMinutes / 60.0);
        
        // Calculate base fee
        double calculatedFee = hours * hourlyRate;

        // Apply minimum charge
        double finalFee = Math.max(calculatedFee, minimumCharge);

        log.debug("Calculated parking fee: {} hours at ${}/hr = ${} (min charge: ${})", 
                hours, hourlyRate, calculatedFee, minimumCharge);

        return Math.round(finalFee * 100.0) / 100.0; // Round to 2 decimal places
    }

    /**
     * Get current hourly rate
     */
    public Double getHourlyRate() {
        return hourlyRate;
    }

    /**
     * Get minimum charge
     */
    public Double getMinimumCharge() {
        return minimumCharge;
    }

    /**
     * Get grace period in minutes
     */
    public Integer getGracePeriodMinutes() {
        return gracePeriodMinutes;
    }
}
