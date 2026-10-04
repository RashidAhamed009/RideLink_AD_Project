package com.ridelink.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.payment.dto.*;
import com.ridelink.payment.exception.GlobalExceptionHandler;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.service.FarePaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FarePaymentControllerValidationTest {

    private MockMvc fareMockMvc;
    private MockMvc paymentMockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private FarePaymentService farePaymentService;

    @InjectMocks
    private FareController fareController;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        fareMockMvc = MockMvcBuilders.standaloneSetup(fareController)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        paymentMockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("MVC: Fare estimate returns 200 OK with valid parameters")
    void testEstimateFare_ValidInput_ReturnsOk() throws Exception {
        FareEstimateRequest request = new FareEstimateRequest("ride-1", "Point A", "Point B", 10.0, 15.0);
        FareEstimateResponse mockResponse = new FareEstimateResponse("est-1", "ride-1", "Point A", "Point B",
                10.0, 15.0, 3.0, 15.0, 3.75, 21.75, "USD", Instant.now());

        when(farePaymentService.estimateFare(any(FareEstimateRequest.class))).thenReturn(mockResponse);

        fareMockMvc.perform(post("/api/v1/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedTotal").value(21.75))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    @DisplayName("MVC: Fare estimate with blank pickup returns 400 Bad Request")
    void testEstimateFare_BlankPickup_ReturnsBadRequest() throws Exception {
        FareEstimateRequest request = new FareEstimateRequest("ride-1", "", "Point B", 10.0, 15.0);

        fareMockMvc.perform(post("/api/v1/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("pickup"));
    }

    @Test
    @DisplayName("MVC: Fare estimate with non-positive distance returns 400 Bad Request")
    void testEstimateFare_ZeroDistance_ReturnsBadRequest() throws Exception {
        FareEstimateRequest request = new FareEstimateRequest("ride-1", "A", "B", 0.0, 15.0);

        fareMockMvc.perform(post("/api/v1/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("distanceKm"));
    }

    @Test
    @DisplayName("MVC: Final fare calculation returns 200 OK with valid parameters")
    void testCalculateFinalFare_ValidInput_ReturnsOk() throws Exception {
        FinalFareRequest request = new FinalFareRequest("ride-1", 12.0, 25.0, 1.2);
        FinalFareResponse mockResponse = new FinalFareResponse("ride-1", 3.0, 12.0, 18.0, 25.0, 6.25, 1.2, 32.70, "USD");

        when(farePaymentService.calculateFinalFare(any(FinalFareRequest.class))).thenReturn(mockResponse);

        fareMockMvc.perform(post("/api/v1/fares/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(32.70))
                .andExpect(jsonPath("$.surgeMultiplier").value(1.2));
    }

    @Test
    @DisplayName("MVC: Final fare with blank rideId returns 400 Bad Request")
    void testCalculateFinalFare_BlankRideId_ReturnsBadRequest() throws Exception {
        FinalFareRequest request = new FinalFareRequest("", 12.0, 25.0, 1.0);

        fareMockMvc.perform(post("/api/v1/fares/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("rideId"));
    }

    @Test
    @DisplayName("MVC: Process payment returns 201 Created with valid payload")
    void testProcessPayment_ValidInput_ReturnsCreated() throws Exception {
        SimulatePaymentRequest request = new SimulatePaymentRequest(
                "ride-99", "pass-1", "drv-1", 28.50, PaymentMethod.SIMULATED_WALLET, false, null
        );

        PaymentResponse mockResponse = new PaymentResponse(
                "pmt-99", "ride-99", "pass-1", 28.50, PaymentMethod.SIMULATED_WALLET,
                PaymentStatus.SUCCESS, "TXN-SIM-9999", null, "rcpt-99", Instant.now()
        );

        when(farePaymentService.processPayment(any(SimulatePaymentRequest.class))).thenReturn(mockResponse);

        paymentMockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("pmt-99"))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.receiptId").value("rcpt-99"));
    }

    @Test
    @DisplayName("MVC: Process payment with missing passengerId returns 400 Bad Request")
    void testProcessPayment_MissingPassenger_ReturnsBadRequest() throws Exception {
        SimulatePaymentRequest request = new SimulatePaymentRequest(
                "ride-99", "", "drv-1", 28.50, PaymentMethod.SIMULATED_WALLET, false, null
        );

        paymentMockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("passengerId"));
    }

    @Test
    @DisplayName("MVC: Process payment with zero amount returns 400 Bad Request")
    void testProcessPayment_ZeroAmount_ReturnsBadRequest() throws Exception {
        SimulatePaymentRequest request = new SimulatePaymentRequest(
                "ride-99", "pass-1", "drv-1", 0.0, PaymentMethod.CREDIT_CARD, false, null
        );

        paymentMockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("amount"));
    }

    @Test
    @DisplayName("MVC: Retrieve payment by ride ID returns 200 OK")
    void testGetPaymentByRideId_ReturnsOk() throws Exception {
        PaymentResponse mockResponse = new PaymentResponse(
                "pmt-88", "ride-88", "pass-2", 35.00, PaymentMethod.SIMULATED_WALLET,
                PaymentStatus.SUCCESS, "TXN-8888", null, "rcpt-88", Instant.now()
        );

        when(farePaymentService.getPaymentByRideId("ride-88")).thenReturn(mockResponse);

        paymentMockMvc.perform(get("/api/v1/payments/rides/ride-88"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("pmt-88"))
                .andExpect(jsonPath("$.rideId").value("ride-88"));
    }

    @Test
    @DisplayName("MVC: Retrieve receipt by receipt ID returns 200 OK")
    void testGetReceiptById_ReturnsOk() throws Exception {
        ReceiptResponse mockResponse = new ReceiptResponse(
                "rcpt-77", "pmt-77", "ride-77", "pass-3", "drv-3", 42.0, "USD",
                3.0, 27.3, 11.7, "RCPT-7777", Instant.now()
        );

        when(farePaymentService.getReceiptById("rcpt-77")).thenReturn(mockResponse);

        paymentMockMvc.perform(get("/api/v1/payments/receipts/rcpt-77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptId").value("rcpt-77"))
                .andExpect(jsonPath("$.receiptNumber").value("RCPT-7777"))
                .andExpect(jsonPath("$.totalAmount").value(42.0));
    }
}
