package com.ridelink.ride.client.impl;

import com.ridelink.ride.client.FarePaymentServiceClient;
import com.ridelink.ride.dto.FareEstimateDTO;
import com.ridelink.ride.dto.FinalFareDTO;
import com.ridelink.ride.dto.PaymentRequestDTO;
import com.ridelink.ride.dto.PaymentResponseDTO;
import com.ridelink.ride.exception.DownstreamServiceException;
import com.ridelink.ride.security.ServiceTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
public class FarePaymentServiceClientImpl implements FarePaymentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(FarePaymentServiceClientImpl.class);

    private final RestClient restClient;
    private final ServiceTokenProvider tokenProvider;
    private final String baseUrl;

    public FarePaymentServiceClientImpl(RestClient.Builder restClientBuilder,
                                        ServiceTokenProvider tokenProvider,
                                        @Value("${payment.service.url:http://localhost:8084}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.tokenProvider = tokenProvider;
        this.baseUrl = baseUrl;
    }

    private double round2(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public FareEstimateDTO estimateFare(String rideId, String pickup, String destination, double distanceKm, double durationMinutes) {
        try {
            log.info("Requesting fare estimate from Fare & Payment Service for ride {}", rideId);
            Map<String, Object> req = new HashMap<>();
            req.put("rideId", rideId);
            req.put("pickup", pickup);
            req.put("destination", destination);
            req.put("distanceKm", distanceKm);
            req.put("durationMinutes", durationMinutes);

            return restClient.post()
                    .uri("/api/v1/fares/estimate")
                    .header("Authorization", tokenProvider.getAuthorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(FareEstimateDTO.class);
        } catch (RestClientException e) {
            log.warn("Downstream Fare Service unavailable at {}. Applying deterministic fallback pricing: {}", baseUrl, e.getMessage());
            // Graceful fallback: 3.00 base + (dist * 1.50) + (time * 0.25)
            double distCharge = round2(distanceKm * 1.50);
            double timeCharge = round2(durationMinutes * 0.25);
            double total = round2(3.00 + distCharge + timeCharge);
            return new FareEstimateDTO("fallback-est", rideId, 3.00, distCharge, timeCharge, total, "USD");
        }
    }

    @Override
    public FinalFareDTO calculateFinalFare(String rideId, double distanceKm, double durationMinutes, Double surgeMultiplier) {
        double surge = surgeMultiplier != null && surgeMultiplier > 0 ? surgeMultiplier : 1.0;
        try {
            log.info("Calculating final fare with Fare & Payment Service for ride {}", rideId);
            Map<String, Object> req = new HashMap<>();
            req.put("rideId", rideId);
            req.put("distanceKm", distanceKm);
            req.put("durationMinutes", durationMinutes);
            req.put("surgeMultiplier", surge);

            return restClient.post()
                    .uri("/api/v1/fares/calculate")
                    .header("Authorization", tokenProvider.getAuthorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(FinalFareDTO.class);
        } catch (RestClientException e) {
            log.warn("Downstream Fare Service calculate endpoint unavailable. Applying deterministic fallback: {}", e.getMessage());
            double distCharge = round2(distanceKm * 1.50);
            double timeCharge = round2(durationMinutes * 0.25);
            double total = round2((3.00 + distCharge + timeCharge) * surge);
            return new FinalFareDTO(rideId, 3.00, distCharge, timeCharge, surge, total, "USD");
        }
    }

    @Override
    public PaymentResponseDTO processPayment(String rideId, String passengerId, String driverId, double amount, String paymentMethod) {
        try {
            log.info("Processing settlement payment of ${} for ride {} via Fare & Payment Service", amount, rideId);
            PaymentRequestDTO req = new PaymentRequestDTO(
                    rideId, passengerId, driverId, amount,
                    paymentMethod != null ? paymentMethod : "SIMULATED_WALLET",
                    false, null
            );

            return restClient.post()
                    .uri("/api/v1/payments/process")
                    .header("Authorization", tokenProvider.getAuthorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(PaymentResponseDTO.class);
        } catch (RestClientException e) {
            log.error("Failed to process payment settlement with Fare & Payment Service at {}: {}", baseUrl, e.getMessage());
            throw new DownstreamServiceException("fare-payment-service", "Payment settlement processing failed: " + e.getMessage(), e);
        }
    }
}
