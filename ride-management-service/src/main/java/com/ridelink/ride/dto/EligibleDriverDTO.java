package com.ridelink.ride.dto;

public class EligibleDriverDTO {

    private String driverId;
    private String driverProfileId;
    private String licenseNumber;
    private String serviceArea;
    private Double rating;
    private Double distanceKm;

    public EligibleDriverDTO() {}

    public EligibleDriverDTO(String driverId, String driverProfileId, String licenseNumber,
                             String serviceArea, Double rating, Double distanceKm) {
        this.driverId = driverId;
        this.driverProfileId = driverProfileId;
        this.licenseNumber = licenseNumber;
        this.serviceArea = serviceArea;
        this.rating = rating;
        this.distanceKm = distanceKm;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverProfileId() {
        return driverProfileId;
    }

    public void setDriverProfileId(String driverProfileId) {
        this.driverProfileId = driverProfileId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
