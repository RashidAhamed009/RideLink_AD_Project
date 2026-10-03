package com.ridelink.payment.repository;

import com.ridelink.payment.model.FareEstimate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FareEstimateRepository extends MongoRepository<FareEstimate, String> {
    List<FareEstimate> findByRideId(String rideId);
}
