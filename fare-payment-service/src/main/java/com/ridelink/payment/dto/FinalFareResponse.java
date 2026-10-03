package com.ridelink.payment.dto;

public class FinalFareResponse {

    private String rideId;
    private double baseFare;
    private double distanceKm;
    private double distanceCharge;
    private double durationMinutes;
    private double timeCharge;
    private double surgeMultiplier;
    private double totalAmount;
    private String currency;

    public FinalFareResponse() {}

    public FinalFareResponse(String rideId, double baseFare, double distanceKm, double distanceCharge,
                             double durationMinutes, double timeCharge, double surgeMultiplier,
                             double totalAmount, String currency) {
        this.rideId = rideId;
        this.baseFare = baseFare;
        this.distanceKm = distanceKm;
        this.distanceCharge = distanceCharge;
        this.durationMinutes = durationMinutes;
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

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getDistanceCharge() {
        return distanceCharge;
    }

    public void setDistanceCharge(double distanceCharge) {
        this.distanceCharge = distanceCharge;
    }

    public double getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(double durationMinutes) {
        this.durationMinutes = durationMinutes;
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
