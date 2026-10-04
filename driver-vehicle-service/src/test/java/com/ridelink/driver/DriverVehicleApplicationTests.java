package com.ridelink.driver;

import com.ridelink.driver.dto.ErrorResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DriverVehicleApplicationTests {

    @Test
    @DisplayName("Unit test: ErrorResponseDTO initializes with default timestamp and serviceName")
    void testErrorResponseDTOInitialization() {
        ErrorResponseDTO dto = new ErrorResponseDTO(400, "Bad Request", "VALIDATION_FAILED", "Invalid field", "/api/v1/drivers");

        assertNotNull(dto.getTimestamp());
        assertEquals(400, dto.getStatus());
        assertEquals("Bad Request", dto.getError());
        assertEquals("VALIDATION_FAILED", dto.getErrorCode());
        assertEquals("Invalid field", dto.getMessage());
        assertEquals("/api/v1/drivers", dto.getPath());
        assertEquals("driver-vehicle-service", dto.getServiceName());
    }
}
