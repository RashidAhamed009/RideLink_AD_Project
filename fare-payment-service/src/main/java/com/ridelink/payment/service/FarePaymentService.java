package com.ridelink.payment.service;

import com.ridelink.payment.dto.*;

public interface FarePaymentService {
    FareEstimateResponse estimateFare(FareEstimateRequest request);
    FinalFareResponse calculateFinalFare(FinalFareRequest request);
    PaymentResponse processPayment(SimulatePaymentRequest request);
    PaymentResponse getPaymentByRideId(String rideId);
    PaymentResponse getPaymentById(String paymentId);
    ReceiptResponse getReceiptById(String receiptId);
    ReceiptResponse getReceiptByRideId(String rideId);
}
