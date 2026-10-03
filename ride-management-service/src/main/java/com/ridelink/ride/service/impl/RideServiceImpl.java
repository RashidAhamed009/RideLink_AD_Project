package com.ridelink.ride.service.impl;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.FarePaymentServiceClient;
import com.ridelink.ride.dto.*;
import com.ridelink.ride.exception.*;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.security.AuthenticatedUser;
import com.ridelink.ride.service.RideService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RideServiceImpl implements RideService {

    private static final Logger log = LoggerFactory.getLogger(RideServiceImpl.class);

    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;
    private final FarePaymentServiceClient farePaymentServiceClient;

    public RideServiceImpl(RideRepository rideRepository,
                           DriverServiceClient driverServiceClient,
                           FarePaymentServiceClient farePaymentServiceClient) {
        this.rideRepository = rideRepository;
        this.driverServiceClient = driverServiceClient;
        this.farePaymentServiceClient = farePaymentServiceClient;
    }

    private boolean isAdmin(AuthenticatedUser caller) {
        if (caller == null) return false;
        return caller.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override
    public RideResponse createRide(String passengerId, CreateRideRequest request) {
        if (passengerId == null || passengerId.isBlank()) {
            throw new IllegalArgumentException("Passenger identity must be provided");
        }

        // 1. Inter-Service Communication: Query Driver & Vehicle Service for nearby eligible drivers
        List<EligibleDriverDTO> eligibleDrivers = driverServiceClient.findEligibleDrivers(
                request.getPickup().getLatitude(),
                request.getPickup().getLongitude(),
                10.0 // Default 10km radius
        );

        if (eligibleDrivers.isEmpty()) {
            log.warn("No eligible drivers available within radius for passenger {}", passengerId);
            throw new NoDriversAvailableException("No eligible drivers are currently available near your pickup location.");
        }

        // 2. Inter-Service Communication: Request upfront deterministic fare estimate from Fare & Payment Service
        String rideId = java.util.UUID.randomUUID().toString();
        FareEstimateDTO fareEstimate = farePaymentServiceClient.estimateFare(
                rideId,
                request.getPickup().getAddress(),
                request.getDestination().getAddress(),
                request.getEstimatedDistanceKm(),
                request.getEstimatedDurationMinutes()
        );

        // 3. Persist Ride entity in initial REQUESTED state
        Ride ride = new Ride(
                passengerId,
                request.getPickup(),
                request.getDestination(),
                fareEstimate != null ? fareEstimate.getEstimatedTotal() : 0.0,
                request.getEstimatedDistanceKm(),
                request.getEstimatedDurationMinutes()
        );
        ride.setRideId(rideId);
        ride.setStatus(RideStatus.REQUESTED);

        Ride savedRide = rideRepository.save(ride);
        log.info("Ride created successfully with ID: {} for passenger: {}", savedRide.getRideId(), passengerId);
        return RideResponse.fromEntity(savedRide);
    }

    @Override
    public RideResponse getRideById(String rideId, AuthenticatedUser caller) {
        Ride ride = rideRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideId));

        if (caller != null && !isAdmin(caller)
                && !caller.getUserId().equals(ride.getPassengerId())
                && !caller.getUserId().equals(ride.getDriverId())) {
            throw new UnauthorizedOperationException("You are not authorized to view details for this ride.");
        }

        return RideResponse.fromEntity(ride);
    }

    @Override
    public List<RideResponse> getPassengerRides(String passengerId, AuthenticatedUser caller) {
        if (caller != null && !isAdmin(caller) && !caller.getUserId().equals(passengerId)) {
            throw new UnauthorizedOperationException("You are not authorized to view rides for this passenger.");
        }
        return rideRepository.findAllByPassengerId(passengerId).stream()
                .map(RideResponse::fromEntity)
                .toList();
    }

    @Override
    public List<RideResponse> getDriverRides(String driverId, AuthenticatedUser caller) {
        if (caller != null && !isAdmin(caller) && !caller.getUserId().equals(driverId)) {
            throw new UnauthorizedOperationException("You are not authorized to view rides for this driver.");
        }
        return rideRepository.findAllByDriverId(driverId).stream()
                .map(RideResponse::fromEntity)
                .toList();
    }

    @Override
    public List<EligibleDriverDTO> findEligibleDriversForRide(String rideId, AuthenticatedUser caller) {
        Ride ride = rideRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideId));

        if (caller != null && !isAdmin(caller) && !caller.getUserId().equals(ride.getPassengerId())) {
            throw new UnauthorizedOperationException("Only the ride requester or admin can query eligible drivers.");
        }

        return driverServiceClient.findEligibleDrivers(
                ride.getPickup().getLatitude(),
                ride.getPickup().getLongitude(),
                10.0
        );
    }

    @Override
    public RideResponse assignDriver(String rideId, AssignDriverRequest request, AuthenticatedUser caller) {
        Ride ride = rideRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideId));

        // Strict State Transition Check: REQUESTED -> ASSIGNED
        if (!ride.getStatus().canTransitionTo(RideStatus.ASSIGNED)) {
            throw new IllegalRideStateTransitionException(ride.getStatus(), RideStatus.ASSIGNED);
        }

        if (request.getDriverId() == null || request.getDriverId().isBlank()) {
            throw new IllegalArgumentException("Driver ID is required for assignment.");
        }

        ride.setDriverId(request.getDriverId().trim());
        ride.setStatus(RideStatus.ASSIGNED);
        Instant now = Instant.now();
        ride.setAssignedAt(now);
        ride.setUpdatedAt(now);

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} successfully assigned to driver {}", rideId, request.getDriverId());
        return RideResponse.fromEntity(saved);
    }

    @Override
    public RideResponse acceptRide(String rideId, AuthenticatedUser caller) {
        Ride ride = rideRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideId));

        // Strict State Transition Check: ASSIGNED -> ACCEPTED
        if (!ride.getStatus().canTransitionTo(RideStatus.ACCEPTED)) {
            throw new IllegalRideStateTransitionException(ride.getStatus(), RideStatus.ACCEPTED);
        }

        // Only the assigned driver (or Admin) can accept
        if (caller != null && !isAdmin(caller) && !caller.getUserId().equals(ride.getDriverId())) {
            throw new UnauthorizedOperationException("Only the assigned driver can accept this ride.");
        }

        ride.setStatus(RideStatus.ACCEPTED);
        Instant now = Instant.now();
        ride.setAcceptedAt(now);
        ride.setUpdatedAt(now);

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} accepted by driver {}", rideId, ride.getDriverId());
        return RideResponse.fromEntity(saved);
    }

    @Override
    public RideResponse startRide(String rideId, AuthenticatedUser caller) {
        Ride ride = rideRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideId));

        // Strict State Transition Check: ACCEPTED -> IN_PROGRESS
        if (!ride.getStatus().canTransitionTo(RideStatus.IN_PROGRESS)) {
            throw new IllegalRideStateTransitionException(ride.getStatus(), RideStatus.IN_PROGRESS);
        }

        if (caller != null && !isAdmin(caller) && !caller.getUserId().equals(ride.getDriverId())) {
            throw new UnauthorizedOperationException("Only the assigned driver can start this ride.");
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        Instant now = Instant.now();
        ride.setStartedAt(now);
        ride.setUpdatedAt(now);

        // Update driver availability in Driver Service to ON_TRIP
        driverServiceClient.updateDriverAvailability(ride.getDriverId(), "ON_TRIP");

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} is now IN_PROGRESS with driver {}", rideId, ride.getDriverId());
        return RideResponse.fromEntity(saved);
    }

    @Override
    public RideResponse completeRide(String rideId, CompleteRideRequest request, AuthenticatedUser caller) {
        Ride ride = rideRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideId));

        // Strict State Transition Check: IN_PROGRESS -> COMPLETED
        if (!ride.getStatus().canTransitionTo(RideStatus.COMPLETED)) {
            throw new IllegalRideStateTransitionException(ride.getStatus(), RideStatus.COMPLETED);
        }

        if (caller != null && !isAdmin(caller) && !caller.getUserId().equals(ride.getDriverId())) {
            throw new UnauthorizedOperationException("Only the assigned driver can complete this ride.");
        }

        ride.setActualDistanceKm(request.getActualDistanceKm());
        ride.setActualDurationMinutes(request.getActualDurationMinutes());

        // 1. Calculate final fare with Fare & Payment Service
        FinalFareDTO finalFare = farePaymentServiceClient.calculateFinalFare(
                rideId,
                request.getActualDistanceKm(),
                request.getActualDurationMinutes(),
                request.getSurgeMultiplier()
        );
        double totalAmount = finalFare != null ? finalFare.getTotalAmount() : ride.getEstimatedFare();
        ride.setFinalFare(totalAmount);

        // 2. Process simulated payment settlement with Fare & Payment Service
        PaymentResponseDTO paymentResponse = farePaymentServiceClient.processPayment(
                rideId,
                ride.getPassengerId(),
                ride.getDriverId(),
                totalAmount,
                request.getPaymentMethod()
        );
        if (paymentResponse != null) {
            ride.setPaymentReference(paymentResponse.getTransactionReference());
            ride.setReceiptId(paymentResponse.getReceiptId());
        }

        // 3. Mark ride COMPLETED
        ride.setStatus(RideStatus.COMPLETED);
        Instant now = Instant.now();
        ride.setCompletedAt(now);
        ride.setUpdatedAt(now);

        // 4. Reset driver availability to AVAILABLE
        driverServiceClient.updateDriverAvailability(ride.getDriverId(), "AVAILABLE");

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} completed. Final fare: ${}. Payment ref: {}", rideId, totalAmount, ride.getPaymentReference());
        return RideResponse.fromEntity(saved);
    }

    @Override
    public RideResponse cancelRide(String rideId, CancelRideRequest request, AuthenticatedUser caller) {
        Ride ride = rideRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideId));

        // Strict State Transition Check: Terminal states cannot be cancelled
        if (!ride.getStatus().canTransitionTo(RideStatus.CANCELLED)) {
            throw new IllegalRideStateTransitionException(ride.getStatus(), RideStatus.CANCELLED);
        }

        // Authorization Check: Only passenger, assigned driver, or admin can cancel
        if (caller != null && !isAdmin(caller)
                && !caller.getUserId().equals(ride.getPassengerId())
                && !caller.getUserId().equals(ride.getDriverId())) {
            throw new UnauthorizedOperationException("You are not authorized to cancel this ride.");
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancellationReason(request != null && request.getReason() != null ? request.getReason() : "Cancelled by user");
        ride.setCancelledBy(caller != null ? caller.getUsername() : "SYSTEM");

        Instant now = Instant.now();
        ride.setCancelledAt(now);
        ride.setUpdatedAt(now);

        // If a driver was already assigned or on trip, free them up
        if (ride.getDriverId() != null) {
            driverServiceClient.updateDriverAvailability(ride.getDriverId(), "AVAILABLE");
        }

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} cancelled by {}", rideId, ride.getCancelledBy());
        return RideResponse.fromEntity(saved);
    }
}
