package com.ridelink.ride;

import com.ridelink.ride.dto.ErrorResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RideManagementApplicationTests {

    @Test
    @DisplayName("Unit test: ErrorResponseDTO initializes with default timestamp and serviceName")
    void testErrorResponseDTOInitialization() {
        ErrorResponseDTO dto = new ErrorResponseDTO(404, "Not Found", "RESOURCE_NOT_FOUND", "Ride not found", "/api/v1/rides/123");

        assertNotNull(dto.getTimestamp());
        assertEquals(404, dto.getStatus());
        assertEquals("Not Found", dto.getError());
        assertEquals("RESOURCE_NOT_FOUND", dto.getErrorCode());
        assertEquals("Ride not found", dto.getMessage());
        assertEquals("/api/v1/rides/123", dto.getPath());
        assertEquals("ride-management-service", dto.getServiceName());
    }
}
