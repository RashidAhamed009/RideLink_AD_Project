package com.ridelink.ride.dto;

import com.ridelink.ride.model.LocationPoint;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;

import java.time.Instant;

public class RideResponse {

    private String rideId;
    private String passengerId;
    private String driverId;
    private LocationPoint pickup;
    private LocationPoint destination;
    private double estimatedFare;
    private Double finalFare;
    private RideStatus status;
    private Double estimatedDistanceKm;
    private Double estimatedDurationMinutes;
    private Double actualDistanceKm;
    private Double actualDurationMinutes;
    private String paymentReference;
    private String receiptId;
    private String cancellationReason;
    private String cancelledBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant requestedAt;
    private Instant assignedAt;
    private Instant acceptedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    public RideResponse() {}

    public static RideResponse fromEntity(Ride ride) {
        if (ride == null) return null;
        RideResponse res = new RideResponse();
        res.rideId = ride.getRideId();
        res.passengerId = ride.getPassengerId();
        res.driverId = ride.getDriverId();
        res.pickup = ride.getPickup();
        res.destination = ride.getDestination();
        res.estimatedFare = ride.getEstimatedFare();
        res.finalFare = ride.getFinalFare();
        res.status = ride.getStatus();
        res.estimatedDistanceKm = ride.getEstimatedDistanceKm();
        res.estimatedDurationMinutes = ride.getEstimatedDurationMinutes();
        res.actualDistanceKm = ride.getActualDistanceKm();
        res.actualDurationMinutes = ride.getActualDurationMinutes();
        res.paymentReference = ride.getPaymentReference();
        res.receiptId = ride.getReceiptId();
        res.cancellationReason = ride.getCancellationReason();
        res.cancelledBy = ride.getCancelledBy();
        res.createdAt = ride.getCreatedAt();
        res.updatedAt = ride.getUpdatedAt();
        res.requestedAt = ride.getRequestedAt();
        res.assignedAt = ride.getAssignedAt();
        res.acceptedAt = ride.getAcceptedAt();
        res.startedAt = ride.getStartedAt();
        res.completedAt = ride.getCompletedAt();
        res.cancelledAt = ride.getCancelledAt();
        return res;
    }

    public String getRideId() {
        return rideId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public LocationPoint getPickup() {
        return pickup;
    }

    public LocationPoint getDestination() {
        return destination;
    }

    public double getEstimatedFare() {
        return estimatedFare;
    }

    public Double getFinalFare() {
        return finalFare;
    }

    public RideStatus getStatus() {
        return status;
    }

    public Double getEstimatedDistanceKm() {
        return estimatedDistanceKm;
    }

    public Double getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public Double getActualDistanceKm() {
        return actualDistanceKm;
    }

    public Double getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }
}
