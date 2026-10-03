package com.ridelink.driver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "vehicles")
public class Vehicle {

    @Id
    private String id; // vehicleId

    @Indexed
    private String driverId; // Foreign reference to Account Service userId

    @Indexed(unique = true)
    private String registrationNumber;

    private String make;

    private String model;

    private String vehicleType; // e.g. SEDAN, SUV, VAN, HATCHBACK

    private String color;

    private int year;

    private Instant createdAt;

    private Instant updatedAt;

    public Vehicle() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Vehicle(String driverId, String registrationNumber, String make, String model, String vehicleType, String color, int year) {
        this();
        this.driverId = driverId;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.vehicleType = vehicleType;
        this.color = color;
        this.year = year;
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

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        this.updatedAt = Instant.now();
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
        this.updatedAt = Instant.now();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
        this.updatedAt = Instant.now();
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
        this.updatedAt = Instant.now();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        this.updatedAt = Instant.now();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
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
