package com.ridelink.driver.service;

import com.ridelink.driver.dto.*;

import java.util.List;

public interface DriverService {
    DriverProfileResponse createOrUpdateProfile(String driverId, DriverProfileRequest request);
    VehicleResponse addVehicle(String driverId, VehicleRequest request);
    VehicleResponse updateVehicle(String driverId, String vehicleId, VehicleRequest request);
    DriverDetailsResponse getDriverDetails(String driverId);
    DriverProfileResponse updateAvailability(String driverId, UpdateAvailabilityRequest request);
    DriverProfileResponse updateLocation(String driverId, UpdateLocationRequest request);
    List<EligibleDriverResponse> findEligibleDrivers(String serviceArea, Double latitude, Double longitude, Double radiusKm);
}
