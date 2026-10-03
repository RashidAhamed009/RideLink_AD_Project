package com.ridelink.payment.dto;

import com.ridelink.payment.model.FareEstimate;

import java.time.Instant;

public class FareEstimateResponse {

    private String estimateId;
    private String rideId;
    private String pickup;
    private String destination;
    private double distanceKm;
    private double estimatedDurationMinutes;
    private double baseFare;
    private double distanceCharge;
    private double timeCharge;
    private double estimatedTotal;
    private String currency;
    private Instant createdAt;

    public FareEstimateResponse() {}

    public FareEstimateResponse(String estimateId, String rideId, String pickup, String destination,
                                double distanceKm, double estimatedDurationMinutes, double baseFare,
                                double distanceCharge, double timeCharge, double estimatedTotal,
                                String currency, Instant createdAt) {
        this.estimateId = estimateId;
        this.rideId = rideId;
        this.pickup = pickup;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.timeCharge = timeCharge;
        this.estimatedTotal = estimatedTotal;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public static FareEstimateResponse fromEntity(FareEstimate entity, String currency) {
        return new FareEstimateResponse(
                entity.getEstimateId(),
                entity.getRideId(),
                entity.getPickup(),
                entity.getDestination(),
                entity.getDistanceKm(),
                entity.getEstimatedDurationMinutes(),
                entity.getBaseFare(),
                entity.getDistanceCharge(),
                entity.getTimeCharge(),
                entity.getEstimatedTotal(),
                currency,
                entity.getCreatedAt()
        );
    }

    public String getEstimateId() {
        return estimateId;
    }

    public void setEstimateId(String estimateId) {
        this.estimateId = estimateId;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(double estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }

    public double getDistanceCharge() {
        return distanceCharge;
    }

    public void setDistanceCharge(double distanceCharge) {
        this.distanceCharge = distanceCharge;
    }

    public double getTimeCharge() {
        return timeCharge;
    }

    public void setTimeCharge(double timeCharge) {
        this.timeCharge = timeCharge;
    }

    public double getEstimatedTotal() {
        return estimatedTotal;
    }

    public void setEstimatedTotal(double estimatedTotal) {
        this.estimatedTotal = estimatedTotal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
