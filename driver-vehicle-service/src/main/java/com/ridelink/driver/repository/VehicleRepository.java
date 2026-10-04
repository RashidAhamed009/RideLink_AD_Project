package com.ridelink.driver.repository;

import com.ridelink.driver.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    Optional<Vehicle> findByDriverId(String driverId);
    List<Vehicle> findAllByDriverId(String driverId);
    boolean existsByRegistrationNumber(String registrationNumber);
}
