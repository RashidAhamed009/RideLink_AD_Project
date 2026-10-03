package com.ridelink.driver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "driver_profiles")
public class DriverProfile {

    @Id
    private String id; // driverProfileId

    @Indexed(unique = true)
    private String driverId; // Foreign reference to Account Service userId

    @Indexed(unique = true)
    private String licenseNumber;

    @Indexed
    private String serviceArea;

    @Indexed
    private AvailabilityStatus availabilityStatus;

    private GeoLocation simulatedCurrentLocation;

    private Double rating;

    private Instant createdAt;

    private Instant updatedAt;

    public DriverProfile() {
        this.id = UUID.randomUUID().toString();
        this.availabilityStatus = AvailabilityStatus.UNAVAILABLE;
        this.rating = 5.0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public DriverProfile(String driverId, String licenseNumber, String serviceArea) {
        this();
        this.driverId = driverId;
        this.licenseNumber = licenseNumber;
        this.serviceArea = serviceArea;
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
        this.updatedAt = Instant.now();
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
        this.updatedAt = Instant.now();
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
        this.updatedAt = Instant.now();
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
        this.updatedAt = Instant.now();
    }

    public GeoLocation getSimulatedCurrentLocation() {
        return simulatedCurrentLocation;
    }

    public void setSimulatedCurrentLocation(GeoLocation simulatedCurrentLocation) {
        this.simulatedCurrentLocation = simulatedCurrentLocation;
        this.updatedAt = Instant.now();
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
        this.updatedAt = Instant.now();
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
