package com.ridelink.ride.repository;

import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends MongoRepository<Ride, String> {
    Optional<Ride> findByRideId(String rideId);
    List<Ride> findAllByPassengerId(String passengerId);
    List<Ride> findAllByDriverId(String driverId);
    List<Ride> findAllByStatus(RideStatus status);
}
