package com.ridelink.ride.service;

import com.ridelink.ride.dto.*;
import com.ridelink.ride.security.AuthenticatedUser;

import java.util.List;

public interface RideService {
    RideResponse createRide(String passengerId, CreateRideRequest request);
    RideResponse getRideById(String rideId, AuthenticatedUser caller);
    List<RideResponse> getPassengerRides(String passengerId, AuthenticatedUser caller);
    List<RideResponse> getDriverRides(String driverId, AuthenticatedUser caller);
    List<EligibleDriverDTO> findEligibleDriversForRide(String rideId, AuthenticatedUser caller);
    RideResponse assignDriver(String rideId, AssignDriverRequest request, AuthenticatedUser caller);
    RideResponse acceptRide(String rideId, AuthenticatedUser caller);
    RideResponse startRide(String rideId, AuthenticatedUser caller);
    RideResponse completeRide(String rideId, CompleteRideRequest request, AuthenticatedUser caller);
    RideResponse cancelRide(String rideId, CancelRideRequest request, AuthenticatedUser caller);
}
