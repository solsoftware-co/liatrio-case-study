package com.liatrio.parkinggarage.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liatrio.parkinggarage.dto.*;
import com.liatrio.parkinggarage.entity.SpotType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ParkingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void checkInAndCheckOut_ShouldWorkEndToEnd() throws Exception {
        // Create floor
        FloorDto floorDto = FloorDto.builder().floorNumber(1).name("Test Floor").build();
        MvcResult floorResult = mockMvc.perform(post("/api/floors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(floorDto)))
                .andExpect(status().isCreated()).andReturn();
        FloorDto createdFloor = objectMapper.readValue(floorResult.getResponse().getContentAsString(), FloorDto.class);

        // Create bay
        BayDto bayDto = BayDto.builder().bayIdentifier("A").name("Test Bay").floorId(createdFloor.getId()).build();
        MvcResult bayResult = mockMvc.perform(post("/api/bays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bayDto)))
                .andExpect(status().isCreated()).andReturn();
        BayDto createdBay = objectMapper.readValue(bayResult.getResponse().getContentAsString(), BayDto.class);

        // Create parking spot
        ParkingSpotDto spotDto = ParkingSpotDto.builder()
                .spotIdentifier("TEST-A-01").spotNumber("01").spotType(SpotType.REGULAR).bayId(createdBay.getId()).build();
        mockMvc.perform(post("/api/parking-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spotDto)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.occupied").value(false));

        // Check in car
        CheckInRequest checkInRequest = CheckInRequest.builder()
                .licensePlate("TEST-123").spotIdentifier("TEST-A-01").make("Toyota").build();
        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkInRequest)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.active").value(true));

        // Verify spot is occupied
        mockMvc.perform(get("/api/parking-spots/identifier/TEST-A-01"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.occupied").value(true));

        // Check out car
        CheckOutRequest checkOutRequest = CheckOutRequest.builder().spotIdentifier("TEST-A-01").build();
        mockMvc.perform(post("/api/parking/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkOutRequest)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.checkOutTime").exists());

        // Verify spot is available again
        mockMvc.perform(get("/api/parking-spots/identifier/TEST-A-01"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.occupied").value(false));
    }
}
