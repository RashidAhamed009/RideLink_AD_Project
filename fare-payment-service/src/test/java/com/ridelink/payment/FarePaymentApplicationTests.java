package com.ridelink.payment;

import com.ridelink.payment.dto.ErrorResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FarePaymentApplicationTests {

    @Test
    @DisplayName("Unit test: ErrorResponseDTO initializes with default timestamp and serviceName")
    void testErrorResponseDTOInitialization() {
        ErrorResponseDTO dto = new ErrorResponseDTO(422, "Unprocessable Entity", "PAYMENT_DECLINED", "Card declined", "/api/v1/payments");

        assertNotNull(dto.getTimestamp());
        assertEquals(422, dto.getStatus());
        assertEquals("Unprocessable Entity", dto.getError());
        assertEquals("PAYMENT_DECLINED", dto.getErrorCode());
        assertEquals("Card declined", dto.getMessage());
        assertEquals("/api/v1/payments", dto.getPath());
        assertEquals("fare-payment-service", dto.getServiceName());
    }
}
