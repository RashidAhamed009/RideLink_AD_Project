package com.ridelink.driver.dto;

import com.ridelink.driver.model.AvailabilityStatus;
import com.ridelink.driver.model.DriverProfile;
import com.ridelink.driver.model.GeoLocation;

import java.time.Instant;

public class DriverProfileResponse {

    private String id;
    private String driverId;
    private String licenseNumber;
    private String serviceArea;
    private AvailabilityStatus availabilityStatus;
    private GeoLocation currentLocation;
    private Double rating;
    private Instant createdAt;
    private Instant updatedAt;

    public DriverProfileResponse() {}

    public DriverProfileResponse(String id, String driverId, String licenseNumber, String serviceArea,
                                 AvailabilityStatus availabilityStatus, GeoLocation currentLocation,
                                 Double rating, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.driverId = driverId;
        this.licenseNumber = licenseNumber;
        this.serviceArea = serviceArea;
        this.availabilityStatus = availabilityStatus;
        this.currentLocation = currentLocation;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DriverProfileResponse fromEntity(DriverProfile profile) {
        if (profile == null) return null;
        return new DriverProfileResponse(
                profile.getId(),
                profile.getDriverId(),
                profile.getLicenseNumber(),
                profile.getServiceArea(),
                profile.getAvailabilityStatus(),
                profile.getSimulatedCurrentLocation(),
                profile.getRating(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
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

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
