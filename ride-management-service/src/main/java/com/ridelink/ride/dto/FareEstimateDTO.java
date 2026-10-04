package com.ridelink.ride.dto;

public class FareEstimateDTO {

    private String estimateId;
    private String rideId;
    private double baseFare;
    private double distanceCharge;
    private double timeCharge;
    private double estimatedTotal;
    private String currency;

    public FareEstimateDTO() {}

    public FareEstimateDTO(String estimateId, String rideId, double baseFare,
                           double distanceCharge, double timeCharge, double estimatedTotal, String currency) {
        this.estimateId = estimateId;
        this.rideId = rideId;
        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.timeCharge = timeCharge;
        this.estimatedTotal = estimatedTotal;
        this.currency = currency;
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
}
