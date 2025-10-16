package com.liatrio.parkinggarage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liatrio.parkinggarage.dto.CheckInRequest;
import com.liatrio.parkinggarage.dto.CheckOutRequest;
import com.liatrio.parkinggarage.dto.ParkingTransactionDto;
import com.liatrio.parkinggarage.service.ParkingTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingController.class)
class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingTransactionService parkingTransactionService;

    private CheckInRequest checkInRequest;
    private CheckOutRequest checkOutRequest;
    private ParkingTransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        checkInRequest = CheckInRequest.builder()
                .licensePlate("ABC-123")
                .spotIdentifier("F1-A-01")
                .make("Toyota")
                .model("Camry")
                .color("Blue")
                .build();

        checkOutRequest = CheckOutRequest.builder()
                .spotIdentifier("F1-A-01")
                .build();

        transactionDto = ParkingTransactionDto.builder()
                .id(1L)
                .carId(1L)
                .licensePlate("ABC-123")
                .parkingSpotId(1L)
                .spotIdentifier("F1-A-01")
                .floorNumber(1)
                .bayIdentifier("A")
                .spotNumber("01")
                .checkInTime(LocalDateTime.now())
                .active(true)
                .build();
    }

    @Test
    void checkIn_WhenValidRequest_ShouldReturn201() throws Exception {
        // Arrange
        when(parkingTransactionService.checkIn(any(CheckInRequest.class)))
                .thenReturn(transactionDto);

        // Act & Assert
        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkInRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$.spotIdentifier").value("F1-A-01"));
    }

    @Test
    void checkIn_WhenInvalidRequest_ShouldReturn400() throws Exception {
        // Arrange
        CheckInRequest invalidRequest = CheckInRequest.builder()
                .licensePlate("") // Empty license plate
                .spotIdentifier("F1-A-01")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkOut_WhenValidRequest_ShouldReturn200() throws Exception {
        // Arrange
        transactionDto.setCheckOutTime(LocalDateTime.now());
        transactionDto.setActive(false);
        when(parkingTransactionService.checkOut(any(CheckOutRequest.class)))
                .thenReturn(transactionDto);

        // Act & Assert
        mockMvc.perform(post("/api/parking/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkOutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.spotIdentifier").value("F1-A-01"));
    }

    @Test
    void getActiveTransactions_ShouldReturn200() throws Exception {
        // Arrange
        when(parkingTransactionService.getActiveTransactions())
                .thenReturn(List.of(transactionDto));

        // Act & Assert
        mockMvc.perform(get("/api/parking/transactions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getTransactionById_WhenExists_ShouldReturn200() throws Exception {
        // Arrange
        when(parkingTransactionService.getTransactionById(1L))
                .thenReturn(transactionDto);

        // Act & Assert
        mockMvc.perform(get("/api/parking/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.licensePlate").value("ABC-123"));
    }
}
