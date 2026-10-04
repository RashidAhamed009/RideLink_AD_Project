package com.ridelink.driver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VehicleRequest {

    @NotBlank(message = "Registration number is required")
    @Size(min = 3, max = 20, message = "Registration number must be between 3 and 20 characters")
    private String registrationNumber;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Vehicle type is required (e.g. SEDAN, SUV, VAN)")
    private String vehicleType;

    @NotBlank(message = "Color is required")
    private String color;

    @Min(value = 1990, message = "Year must be 1990 or newer")
    @Max(value = 2030, message = "Year cannot exceed 2030")
    private int year;

    public VehicleRequest() {}

    public VehicleRequest(String registrationNumber, String make, String model, String vehicleType, String color, int year) {
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.vehicleType = vehicleType;
        this.color = color;
        this.year = year;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
