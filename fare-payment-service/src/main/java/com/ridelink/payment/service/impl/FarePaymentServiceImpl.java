package com.ridelink.payment.service.impl;

import com.ridelink.payment.dto.*;
import com.ridelink.payment.exception.DuplicatePaymentException;
import com.ridelink.payment.exception.InvalidFareParameterException;
import com.ridelink.payment.exception.PaymentFailedException;
import com.ridelink.payment.exception.ResourceNotFoundException;
import com.ridelink.payment.model.*;
import com.ridelink.payment.repository.FareEstimateRepository;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.repository.ReceiptRepository;
import com.ridelink.payment.service.FarePaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Service
public class FarePaymentServiceImpl implements FarePaymentService {

    private final FareEstimateRepository fareEstimateRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;

    private final double baseFare;
    private final double pricePerKm;
    private final double pricePerMinute;
    private final double defaultSurgeMultiplier;
    private final String currency;

    public FarePaymentServiceImpl(
            FareEstimateRepository fareEstimateRepository,
            PaymentRepository paymentRepository,
            ReceiptRepository receiptRepository,
            @Value("${fare.base-fare:3.00}") double baseFare,
            @Value("${fare.price-per-km:1.50}") double pricePerKm,
            @Value("${fare.price-per-minute:0.25}") double pricePerMinute,
            @Value("${fare.surge-multiplier:1.0}") double defaultSurgeMultiplier,
            @Value("${fare.currency:USD}") String currency) {
        this.fareEstimateRepository = fareEstimateRepository;
        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
        this.baseFare = baseFare;
        this.pricePerKm = pricePerKm;
        this.pricePerMinute = pricePerMinute;
        this.defaultSurgeMultiplier = defaultSurgeMultiplier;
        this.currency = currency;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public FareEstimateResponse estimateFare(FareEstimateRequest request) {
        if (request.getDistanceKm() <= 0) {
            throw new InvalidFareParameterException("Distance in kilometers must be greater than 0");
        }
        if (request.getDurationMinutes() <= 0) {
            throw new InvalidFareParameterException("Duration in minutes must be greater than 0");
        }

        double distanceCharge = round2(request.getDistanceKm() * pricePerKm);
        double timeCharge = round2(request.getDurationMinutes() * pricePerMinute);
        double total = round2((baseFare + distanceCharge + timeCharge) * defaultSurgeMultiplier);

        FareEstimate estimate = new FareEstimate(
                request.getRideId(),
                request.getPickup().trim(),
                request.getDestination().trim(),
                request.getDistanceKm(),
                request.getDurationMinutes(),
                baseFare,
                distanceCharge,
                timeCharge,
                total
        );

        FareEstimate saved = fareEstimateRepository.save(estimate);
        return FareEstimateResponse.fromEntity(saved, currency);
    }

    @Override
    public FinalFareResponse calculateFinalFare(FinalFareRequest request) {
        if (request.getDistanceKm() <= 0) {
            throw new InvalidFareParameterException("Final trip distance must be greater than 0");
        }
        if (request.getDurationMinutes() <= 0) {
            throw new InvalidFareParameterException("Final trip duration must be greater than 0");
        }

        double surge = (request.getSurgeMultiplier() != null && request.getSurgeMultiplier() > 0)
                ? request.getSurgeMultiplier()
                : defaultSurgeMultiplier;

        double distanceCharge = round2(request.getDistanceKm() * pricePerKm);
        double timeCharge = round2(request.getDurationMinutes() * pricePerMinute);
        double total = round2((baseFare + distanceCharge + timeCharge) * surge);

        return new FinalFareResponse(
                request.getRideId(),
                baseFare,
                request.getDistanceKm(),
                distanceCharge,
                request.getDurationMinutes(),
                timeCharge,
                surge,
                total,
                currency
        );
    }

    @Override
    public PaymentResponse processPayment(SimulatePaymentRequest request) {
        String rideId = request.getRideId().trim();

        // 1. Guard against duplicate successful payments (Idempotency)
        if (paymentRepository.existsByRideIdAndPaymentStatus(rideId, PaymentStatus.SUCCESS)) {
            throw new DuplicatePaymentException("Payment has already been successfully processed for ride ID: " + rideId);
        }

        if (request.getAmount() <= 0) {
            throw new InvalidFareParameterException("Payment amount must be greater than 0");
        }

        // 2. Deterministic Simulation of Failure Scenarios
        if (Boolean.TRUE.equals(request.getSimulateFailure())) {
            String failureReason = (request.getFailureReason() != null && !request.getFailureReason().isBlank())
                    ? request.getFailureReason().trim()
                    : "INSUFFICIENT_FUNDS";

            Payment failedPayment = new Payment(
                    rideId,
                    request.getPassengerId(),
                    request.getDriverId(),
                    request.getAmount(),
                    request.getPaymentMethod(),
                    PaymentStatus.FAILED,
                    "TXN-FAIL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    failureReason
            );
            paymentRepository.save(failedPayment);
            throw new PaymentFailedException("Simulated payment transaction failed.", failureReason);
        }

        // 3. Successful Payment Simulation
        String txnRef = "TXN-SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = new Payment(
                rideId,
                request.getPassengerId(),
                request.getDriverId(),
                request.getAmount(),
                request.getPaymentMethod(),
                PaymentStatus.SUCCESS,
                txnRef,
                null
        );
        Payment savedPayment = paymentRepository.save(payment);

        // 4. Generate Receipt automatically upon payment success
        String receiptNumber = "RCPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double distanceEstimate = Math.max(0, request.getAmount() - baseFare);

        Receipt receipt = new Receipt(
                savedPayment.getPaymentId(),
                rideId,
                request.getPassengerId(),
                request.getDriverId(),
                request.getAmount(),
                currency,
                baseFare,
                round2(distanceEstimate * 0.7),
                round2(distanceEstimate * 0.3),
                receiptNumber
        );
        Receipt savedReceipt = receiptRepository.save(receipt);

        return PaymentResponse.fromEntity(savedPayment, savedReceipt.getReceiptId());
    }

    @Override
    public PaymentResponse getPaymentByRideId(String rideId) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ride ID: " + rideId));

        String receiptId = receiptRepository.findByRideId(rideId)
                .map(Receipt::getReceiptId)
                .orElse(null);

        return PaymentResponse.fromEntity(payment, receiptId);
    }

    @Override
    public PaymentResponse getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ID: " + paymentId));

        String receiptId = receiptRepository.findByPaymentId(paymentId)
                .map(Receipt::getReceiptId)
                .orElse(null);

        return PaymentResponse.fromEntity(payment, receiptId);
    }

    @Override
    public ReceiptResponse getReceiptById(String receiptId) {
        Receipt receipt = receiptRepository.findByReceiptId(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with ID: " + receiptId));
        return ReceiptResponse.fromEntity(receipt);
    }

    @Override
    public ReceiptResponse getReceiptByRideId(String rideId) {
        Receipt receipt = receiptRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found for ride ID: " + rideId));
        return ReceiptResponse.fromEntity(receipt);
    }
}
