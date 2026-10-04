package com.ridelink.driver.dto;

import java.util.List;

public class DriverDetailsResponse {

    private DriverProfileResponse profile;
    private List<VehicleResponse> vehicles;

    public DriverDetailsResponse() {}

    public DriverDetailsResponse(DriverProfileResponse profile, List<VehicleResponse> vehicles) {
        this.profile = profile;
        this.vehicles = vehicles;
    }

    public DriverProfileResponse getProfile() {
        return profile;
    }

    public void setProfile(DriverProfileResponse profile) {
        this.profile = profile;
    }

    public List<VehicleResponse> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<VehicleResponse> vehicles) {
        this.vehicles = vehicles;
    }
}
