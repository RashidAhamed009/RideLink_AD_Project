package com.ridelink.ride.client.impl;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.dto.EligibleDriverDTO;
import com.ridelink.ride.exception.DownstreamServiceException;
import com.ridelink.ride.security.ServiceTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class DriverServiceClientImpl implements DriverServiceClient {

    private static final Logger log = LoggerFactory.getLogger(DriverServiceClientImpl.class);

    private final RestClient restClient;
    private final ServiceTokenProvider tokenProvider;
    private final String baseUrl;

    public DriverServiceClientImpl(RestClient.Builder restClientBuilder,
                                   ServiceTokenProvider tokenProvider,
                                   @Value("${driver.service.url:http://localhost:8082}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.tokenProvider = tokenProvider;
        this.baseUrl = baseUrl;
    }

    @Override
    public List<EligibleDriverDTO> findEligibleDrivers(double latitude, double longitude, double radiusKm) {
        try {
            log.info("Querying Driver Service for eligible drivers at ({}, {}) within {} km", latitude, longitude, radiusKm);
            List<EligibleDriverDTO> drivers = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/drivers/available")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("radiusKm", radiusKm)
                            .build())
                    .header("Authorization", tokenProvider.getAuthorizationHeader())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<EligibleDriverDTO>>() {});

            return drivers != null ? drivers : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to communicate with Driver & Vehicle Service at {}: {}", baseUrl, e.getMessage());
            throw new DownstreamServiceException("driver-vehicle-service", "Unable to query available drivers: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateDriverAvailability(String driverId, String status) {
        try {
            log.info("Updating availability for driver {} to {}", driverId, status);
            restClient.patch()
                    .uri("/api/v1/drivers/{driverId}/availability", driverId)
                    .header("Authorization", tokenProvider.getAuthorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("availabilityStatus", status, "status", status))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Failed to update availability for driver {} in Driver & Vehicle Service: {}", driverId, e.getMessage());
            // Gracefully log warning so primary ride workflow isn't completely corrupted if status sync has a glitch
        }
    }
}
