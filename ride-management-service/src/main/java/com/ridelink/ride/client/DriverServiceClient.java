package com.ridelink.ride.client;

import com.ridelink.ride.dto.EligibleDriverDTO;

import java.util.List;

public interface DriverServiceClient {
    List<EligibleDriverDTO> findEligibleDrivers(double latitude, double longitude, double radiusKm);
    void updateDriverAvailability(String driverId, String status);
}
