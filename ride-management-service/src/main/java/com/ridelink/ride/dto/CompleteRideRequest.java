package com.ridelink.ride.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class CompleteRideRequest {

    @NotNull(message = "Actual distance in kilometers is required")
    @DecimalMin(value = "0.01", message = "Actual distance must be greater than 0")
    private Double actualDistanceKm;

    @NotNull(message = "Actual duration in minutes is required")
    @DecimalMin(value = "0.1", message = "Actual duration must be greater than 0")
    private Double actualDurationMinutes;

    private String paymentMethod; // Default to SIMULATED_WALLET if null

    private Double surgeMultiplier; // Default to 1.0 if null

    public CompleteRideRequest() {}

    public CompleteRideRequest(Double actualDistanceKm, Double actualDurationMinutes,
                               String paymentMethod, Double surgeMultiplier) {
        this.actualDistanceKm = actualDistanceKm;
        this.actualDurationMinutes = actualDurationMinutes;
        this.paymentMethod = paymentMethod;
        this.surgeMultiplier = surgeMultiplier;
    }

    public Double getActualDistanceKm() {
        return actualDistanceKm;
    }

    public void setActualDistanceKm(Double actualDistanceKm) {
        this.actualDistanceKm = actualDistanceKm;
    }

    public Double getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(Double actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Double getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public void setSurgeMultiplier(Double surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }
}
