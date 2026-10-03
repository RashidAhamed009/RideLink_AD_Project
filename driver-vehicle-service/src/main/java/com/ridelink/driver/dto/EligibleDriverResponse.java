package com.ridelink.driver.dto;

import com.ridelink.driver.model.GeoLocation;

public class EligibleDriverResponse {

    private String driverId;
    private String driverProfileId;
    private String licenseNumber;
    private String serviceArea;
    private GeoLocation currentLocation;
    private Double rating;
    private Double distanceKm;
    private VehicleResponse vehicle;

    public EligibleDriverResponse() {}

    public EligibleDriverResponse(String driverId, String driverProfileId, String licenseNumber,
                                  String serviceArea, GeoLocation currentLocation, Double rating,
                                  Double distanceKm, VehicleResponse vehicle) {
        this.driverId = driverId;
        this.driverProfileId = driverProfileId;
        this.licenseNumber = licenseNumber;
        this.serviceArea = serviceArea;
        this.currentLocation = currentLocation;
        this.rating = rating;
        this.distanceKm = distanceKm;
        this.vehicle = vehicle;
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

    public GeoLocation getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(GeoLocation currentLocation) {
        this.currentLocation = currentLocation;
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

    public VehicleResponse getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleResponse vehicle) {
        this.vehicle = vehicle;
    }
}
