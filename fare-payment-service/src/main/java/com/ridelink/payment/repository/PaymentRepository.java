package com.ridelink.payment.repository;

import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByPaymentId(String paymentId);
    Optional<Payment> findByRideId(String rideId);
    List<Payment> findAllByRideId(String rideId);
    boolean existsByRideIdAndPaymentStatus(String rideId, PaymentStatus status);
}
