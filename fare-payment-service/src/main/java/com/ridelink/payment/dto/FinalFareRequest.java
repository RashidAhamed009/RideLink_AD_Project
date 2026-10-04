package com.ridelink.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FinalFareRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotNull(message = "Actual distance in kilometers is required")
    @DecimalMin(value = "0.01", message = "Distance must be greater than 0")
    private Double distanceKm;

    @NotNull(message = "Actual duration in minutes is required")
    @DecimalMin(value = "0.1", message = "Duration must be greater than 0")
    private Double durationMinutes;

    private Double surgeMultiplier;

    public FinalFareRequest() {}

    public FinalFareRequest(String rideId, Double distanceKm, Double durationMinutes, Double surgeMultiplier) {
        this.rideId = rideId;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.surgeMultiplier = surgeMultiplier;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Double durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Double getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public void setSurgeMultiplier(Double surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }
}
