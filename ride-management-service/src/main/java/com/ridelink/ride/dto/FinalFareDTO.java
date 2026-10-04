package com.ridelink.ride.dto;

public class FinalFareDTO {

    private String rideId;
    private double baseFare;
    private double distanceCharge;
    private double timeCharge;
    private double surgeMultiplier;
    private double totalAmount;
    private String currency;

    public FinalFareDTO() {}

    public FinalFareDTO(String rideId, double baseFare, double distanceCharge, double timeCharge,
                        double surgeMultiplier, double totalAmount, String currency) {
        this.rideId = rideId;
        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.timeCharge = timeCharge;
        this.surgeMultiplier = surgeMultiplier;
        this.totalAmount = totalAmount;
        this.currency = currency;
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

    public double getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public void setSurgeMultiplier(double surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
