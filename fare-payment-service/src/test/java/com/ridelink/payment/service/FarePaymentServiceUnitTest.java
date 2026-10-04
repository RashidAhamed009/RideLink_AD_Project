package com.ridelink.payment.service;

import com.ridelink.payment.dto.*;
import com.ridelink.payment.exception.DuplicatePaymentException;
import com.ridelink.payment.exception.InvalidFareParameterException;
import com.ridelink.payment.exception.PaymentFailedException;
import com.ridelink.payment.exception.ResourceNotFoundException;
import com.ridelink.payment.model.*;
import com.ridelink.payment.repository.FareEstimateRepository;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.repository.ReceiptRepository;
import com.ridelink.payment.service.impl.FarePaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarePaymentServiceUnitTest {

    @Mock
    private FareEstimateRepository fareEstimateRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    private FarePaymentServiceImpl farePaymentService;

    @BeforeEach
    void setUp() {
        farePaymentService = new FarePaymentServiceImpl(
                fareEstimateRepository,
                paymentRepository,
                receiptRepository,
                3.00,   // baseFare
                1.50,   // pricePerKm
                0.25,   // pricePerMinute
                1.0,    // defaultSurgeMultiplier
                "USD"   // currency
        );
    }

    @Test
    @DisplayName("Unit: Calculate upfront fare estimate accurately with default rates")
    void testEstimateFare_Success() {
        FareEstimateRequest request = new FareEstimateRequest("ride-101", "Galle Face", "Colombo Fort", 10.0, 20.0);

        when(fareEstimateRepository.save(any(FareEstimate.class))).thenAnswer(invocation -> {
            FareEstimate arg = invocation.getArgument(0);
            return arg;
        });

        FareEstimateResponse response = farePaymentService.estimateFare(request);

        assertNotNull(response);
        assertEquals("ride-101", response.getRideId());
        assertEquals("Galle Face", response.getPickup());
        assertEquals("Colombo Fort", response.getDestination());
        assertEquals(3.00, response.getBaseFare());
        assertEquals(15.00, response.getDistanceCharge()); // 10 * 1.50
        assertEquals(5.00, response.getTimeCharge());      // 20 * 0.25
        assertEquals(23.00, response.getEstimatedTotal()); // 3 + 15 + 5
        assertEquals("USD", response.getCurrency());
        verify(fareEstimateRepository).save(any(FareEstimate.class));
    }

    @Test
    @DisplayName("Unit: Fare estimation rejects non-positive distance")
    void testEstimateFare_InvalidDistance_ThrowsException() {
        FareEstimateRequest request = new FareEstimateRequest("ride-101", "A", "B", 0.0, 15.0);

        assertThrows(InvalidFareParameterException.class, () -> farePaymentService.estimateFare(request));
        verify(fareEstimateRepository, never()).save(any(FareEstimate.class));
    }

    @Test
    @DisplayName("Unit: Fare estimation rejects non-positive duration")
    void testEstimateFare_InvalidDuration_ThrowsException() {
        FareEstimateRequest request = new FareEstimateRequest("ride-101", "A", "B", 5.0, -10.0);

        assertThrows(InvalidFareParameterException.class, () -> farePaymentService.estimateFare(request));
        verify(fareEstimateRepository, never()).save(any(FareEstimate.class));
    }

    @Test
    @DisplayName("Unit: Calculate final fare without surge")
    void testCalculateFinalFare_WithoutSurge() {
        FinalFareRequest request = new FinalFareRequest("ride-202", 8.0, 12.0, null);

        FinalFareResponse response = farePaymentService.calculateFinalFare(request);

        assertNotNull(response);
        assertEquals("ride-202", response.getRideId());
        assertEquals(3.00, response.getBaseFare());
        assertEquals(12.00, response.getDistanceCharge()); // 8.0 * 1.50
        assertEquals(3.00, response.getTimeCharge());       // 12.0 * 0.25
        assertEquals(1.0, response.getSurgeMultiplier());
        assertEquals(18.00, response.getTotalAmount());     // (3 + 12 + 3) * 1.0
    }

    @Test
    @DisplayName("Unit: Calculate final fare with dynamic surge multiplier")
    void testCalculateFinalFare_WithSurge() {
        FinalFareRequest request = new FinalFareRequest("ride-202", 10.0, 20.0, 1.5);

        FinalFareResponse response = farePaymentService.calculateFinalFare(request);

        assertNotNull(response);
        assertEquals(1.5, response.getSurgeMultiplier());
        // (3.00 + 15.00 + 5.00) * 1.5 = 23.00 * 1.5 = 34.50
        assertEquals(34.50, response.getTotalAmount());
    }

    @Test
    @DisplayName("Unit: Final fare rejects non-positive distance")
    void testCalculateFinalFare_InvalidDistance_ThrowsException() {
        FinalFareRequest request = new FinalFareRequest("ride-202", -5.0, 10.0, 1.0);

        assertThrows(InvalidFareParameterException.class, () -> farePaymentService.calculateFinalFare(request));
    }

    @Test
    @DisplayName("Unit: Process payment successfully and generate receipt")
    void testProcessPayment_Success() {
        SimulatePaymentRequest request = new SimulatePaymentRequest(
                "ride-303", "usr-pass-1", "usr-drv-1", 25.50,
                PaymentMethod.SIMULATED_WALLET, false, null
        );

        when(paymentRepository.existsByRideIdAndPaymentStatus("ride-303", PaymentStatus.SUCCESS))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            return p;
        });

        when(receiptRepository.save(any(Receipt.class))).thenAnswer(invocation -> {
            Receipt r = invocation.getArgument(0);
            return r;
        });

        PaymentResponse response = farePaymentService.processPayment(request);

        assertNotNull(response);
        assertEquals("ride-303", response.getRideId());
        assertEquals("usr-pass-1", response.getPassengerId());
        assertEquals(25.50, response.getAmount());
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertNotNull(response.getTransactionReference());
        assertTrue(response.getTransactionReference().startsWith("TXN-SIM-"));
        assertNotNull(response.getReceiptId());

        verify(paymentRepository).save(any(Payment.class));
        verify(receiptRepository).save(any(Receipt.class));
    }

    @Test
    @DisplayName("Unit: Duplicate payment for already-settled ride throws DuplicatePaymentException (409)")
    void testProcessPayment_DuplicateRide_ThrowsDuplicatePaymentException() {
        SimulatePaymentRequest request = new SimulatePaymentRequest(
                "ride-303", "usr-pass-1", "usr-drv-1", 25.50,
                PaymentMethod.SIMULATED_WALLET, false, null
        );

        when(paymentRepository.existsByRideIdAndPaymentStatus("ride-303", PaymentStatus.SUCCESS))
                .thenReturn(true);

        assertThrows(DuplicatePaymentException.class, () -> farePaymentService.processPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    @DisplayName("Unit: Simulated payment decline throws PaymentFailedException (422) and saves failed transaction")
    void testProcessPayment_SimulatedFailure_ThrowsPaymentFailedException() {
        SimulatePaymentRequest request = new SimulatePaymentRequest(
                "ride-404", "usr-pass-2", "usr-drv-2", 30.00,
                PaymentMethod.CREDIT_CARD, true, "INSUFFICIENT_FUNDS"
        );

        when(paymentRepository.existsByRideIdAndPaymentStatus("ride-404", PaymentStatus.SUCCESS))
                .thenReturn(false);

        PaymentFailedException ex = assertThrows(PaymentFailedException.class,
                () -> farePaymentService.processPayment(request));

        assertEquals("INSUFFICIENT_FUNDS", ex.getFailureReason());
        verify(paymentRepository).save(argThat(p ->
                p.getPaymentStatus() == PaymentStatus.FAILED &&
                "INSUFFICIENT_FUNDS".equals(p.getFailureReason())
        ));
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    @DisplayName("Unit: Process payment with zero or negative amount throws InvalidFareParameterException")
    void testProcessPayment_ZeroAmount_ThrowsException() {
        SimulatePaymentRequest request = new SimulatePaymentRequest(
                "ride-505", "usr-pass-3", "usr-drv-3", 0.0,
                PaymentMethod.CASH, false, null
        );

        when(paymentRepository.existsByRideIdAndPaymentStatus("ride-505", PaymentStatus.SUCCESS))
                .thenReturn(false);

        assertThrows(InvalidFareParameterException.class, () -> farePaymentService.processPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Unit: Retrieve payment by ride ID returns payment details")
    void testGetPaymentByRideId_Success() {
        Payment payment = new Payment("ride-606", "pass-1", "drv-1", 40.0,
                PaymentMethod.SIMULATED_WALLET, PaymentStatus.SUCCESS, "TXN-1234", null);
        Receipt receipt = new Receipt(payment.getPaymentId(), "ride-606", "pass-1", "drv-1",
                40.0, "USD", 3.0, 25.0, 12.0, "RCPT-9999");

        when(paymentRepository.findByRideId("ride-606")).thenReturn(Optional.of(payment));
        when(receiptRepository.findByRideId("ride-606")).thenReturn(Optional.of(receipt));

        PaymentResponse response = farePaymentService.getPaymentByRideId("ride-606");

        assertNotNull(response);
        assertEquals("ride-606", response.getRideId());
        assertEquals(receipt.getReceiptId(), response.getReceiptId());
    }

    @Test
    @DisplayName("Unit: Retrieve payment by non-existent ride ID throws ResourceNotFoundException (404)")
    void testGetPaymentByRideId_NotFound_ThrowsException() {
        when(paymentRepository.findByRideId("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> farePaymentService.getPaymentByRideId("non-existent"));
    }

    @Test
    @DisplayName("Unit: Retrieve receipt by receipt ID returns itemized breakdown")
    void testGetReceiptById_Success() {
        Receipt receipt = new Receipt("pmt-77", "ride-777", "pass-7", "drv-7",
                50.0, "USD", 3.0, 32.9, 14.1, "RCPT-ABC123");

        when(receiptRepository.findByReceiptId(receipt.getReceiptId())).thenReturn(Optional.of(receipt));

        ReceiptResponse response = farePaymentService.getReceiptById(receipt.getReceiptId());

        assertNotNull(response);
        assertEquals(receipt.getReceiptId(), response.getReceiptId());
        assertEquals("RCPT-ABC123", response.getReceiptNumber());
        assertEquals(50.0, response.getTotalAmount());
        assertEquals("USD", response.getCurrency());
    }

    @Test
    @DisplayName("Unit: Retrieve non-existent receipt throws ResourceNotFoundException (404)")
    void testGetReceiptById_NotFound_ThrowsException() {
        when(receiptRepository.findByReceiptId("invalid-rcpt")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> farePaymentService.getReceiptById("invalid-rcpt"));
    }

    @Test
    @DisplayName("Boundary: Calculate final fare at minimum positive distance boundary (0.01 km)")
    void testCalculateFinalFare_BoundaryDistance_MinimumPositive() {
        FinalFareRequest request = new FinalFareRequest("ride-boundary-min", 0.01, 1.0, 1.0);

        FinalFareResponse response = farePaymentService.calculateFinalFare(request);

        assertNotNull(response);
        assertEquals(3.00, response.getBaseFare());
        assertEquals(0.02, response.getDistanceCharge()); // 0.01 * 1.50 = 0.015 -> 0.02
        assertEquals(0.25, response.getTimeCharge());     // 1.0 * 0.25 = 0.25
        assertEquals(3.27, response.getTotalAmount());    // 3.00 + 0.02 + 0.25 = 3.27
    }

    @Test
    @DisplayName("Boundary: Calculate final fare at large distance boundary (500.0 km)")
    void testCalculateFinalFare_BoundaryDistance_Large() {
        FinalFareRequest request = new FinalFareRequest("ride-boundary-large", 500.0, 360.0, 1.0);

        FinalFareResponse response = farePaymentService.calculateFinalFare(request);

        assertNotNull(response);
        assertEquals(3.00, response.getBaseFare());
        assertEquals(750.00, response.getDistanceCharge()); // 500 * 1.50 = 750
        assertEquals(90.00, response.getTimeCharge());      // 360 * 0.25 = 90
        assertEquals(843.00, response.getTotalAmount());    // 3 + 750 + 90 = 843
    }

    @Test
    @DisplayName("Boundary: Exact zero distance boundary throws InvalidFareParameterException")
    void testCalculateFinalFare_BoundaryDistance_Zero_ThrowsException() {
        FinalFareRequest request = new FinalFareRequest("ride-zero-dist", 0.0, 15.0, 1.0);

        assertThrows(InvalidFareParameterException.class, () -> farePaymentService.calculateFinalFare(request));
    }

    @Test
    @DisplayName("Boundary: Exact zero duration boundary throws InvalidFareParameterException")
    void testCalculateFinalFare_BoundaryDuration_Zero_ThrowsException() {
        FinalFareRequest request = new FinalFareRequest("ride-zero-dur", 10.0, 0.0, 1.0);

        assertThrows(InvalidFareParameterException.class, () -> farePaymentService.calculateFinalFare(request));
    }
}
