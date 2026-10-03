package com.ridelink.ride.client;

import com.ridelink.ride.dto.FareEstimateDTO;
import com.ridelink.ride.dto.FinalFareDTO;
import com.ridelink.ride.dto.PaymentResponseDTO;

public interface FarePaymentServiceClient {
    FareEstimateDTO estimateFare(String rideId, String pickup, String destination, double distanceKm, double durationMinutes);
    FinalFareDTO calculateFinalFare(String rideId, double distanceKm, double durationMinutes, Double surgeMultiplier);
    PaymentResponseDTO processPayment(String rideId, String passengerId, String driverId, double amount, String paymentMethod);
}
