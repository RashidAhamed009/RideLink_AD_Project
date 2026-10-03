package com.ridelink.ride.dto;

import com.ridelink.ride.model.LocationPoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class CreateRideRequest {

    @NotNull(message = "Pickup location is required")
    @Valid
    private LocationPoint pickup;

    @NotNull(message = "Destination is required")
    @Valid
    private LocationPoint destination;

    @NotNull(message = "Estimated distance in kilometers is required")
    @DecimalMin(value = "0.1", message = "Distance must be greater than 0")
    private Double estimatedDistanceKm;

    @NotNull(message = "Estimated duration in minutes is required")
    @DecimalMin(value = "1.0", message = "Duration must be at least 1 minute")
    private Double estimatedDurationMinutes;

    public CreateRideRequest() {}

    public CreateRideRequest(LocationPoint pickup, LocationPoint destination,
                             Double estimatedDistanceKm, Double estimatedDurationMinutes) {
        this.pickup = pickup;
        this.destination = destination;
        this.estimatedDistanceKm = estimatedDistanceKm;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public LocationPoint getPickup() {
        return pickup;
    }

    public void setPickup(LocationPoint pickup) {
        this.pickup = pickup;
    }

    public LocationPoint getDestination() {
        return destination;
    }

    public void setDestination(LocationPoint destination) {
        this.destination = destination;
    }

    public Double getEstimatedDistanceKm() {
        return estimatedDistanceKm;
    }

    public void setEstimatedDistanceKm(Double estimatedDistanceKm) {
        this.estimatedDistanceKm = estimatedDistanceKm;
    }

    public Double getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(Double estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }
}
