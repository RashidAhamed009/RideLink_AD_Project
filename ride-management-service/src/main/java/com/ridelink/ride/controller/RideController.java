package com.ridelink.ride.controller;

import com.ridelink.ride.dto.*;
import com.ridelink.ride.security.AuthenticatedUser;
import com.ridelink.ride.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
@Tag(name = "Ride Lifecycle & Dispatch", description = "Endpoints for booking, assigning, driving, and settling rides")
@SecurityRequirement(name = "BearerAuth")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    private AuthenticatedUser resolveCaller(AuthenticatedUser principal) {
        if (principal != null) return principal;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser u) {
            return u;
        }
        return null;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    @Operation(summary = "Create a new ride request", description = "Validates passenger identity, checks Driver Service for nearby available drivers, estimates fare with Fare Service, and saves the ride in REQUESTED state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ride successfully created in REQUESTED state",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed on pickup/destination coordinates"),
            @ApiResponse(responseCode = "404", description = "No eligible drivers available within proximity")
    })
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser caller) {
        AuthenticatedUser user = resolveCaller(caller);
        String passengerId = user != null ? user.getUserId() : "usr-pass-sample";
        RideResponse response = rideService.createRide(passengerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{rideId}")
    @Operation(summary = "Retrieve ride by ride ID", description = "Fetches complete ride tracking and lifecycle details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Ride not found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access to ride details")
    })
    public ResponseEntity<RideResponse> getRideById(@PathVariable String rideId,
                                                    @AuthenticationPrincipal AuthenticatedUser caller) {
        RideResponse response = rideService.getRideById(rideId, resolveCaller(caller));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/passenger/{passengerId}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    @Operation(summary = "Get rides requested by passenger", description = "Returns historical and active rides for a passenger.")
    public ResponseEntity<List<RideResponse>> getPassengerRides(@PathVariable String passengerId,
                                                                @AuthenticationPrincipal AuthenticatedUser caller) {
        List<RideResponse> response = rideService.getPassengerRides(passengerId, resolveCaller(caller));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary = "Get rides assigned to driver", description = "Returns historical and active rides for a driver.")
    public ResponseEntity<List<RideResponse>> getDriverRides(@PathVariable String driverId,
                                                             @AuthenticationPrincipal AuthenticatedUser caller) {
        List<RideResponse> response = rideService.getDriverRides(driverId, resolveCaller(caller));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{rideId}/eligible-drivers")
    @Operation(summary = "Find nearby eligible drivers for a ride", description = "Queries Driver & Vehicle Service using the ride's pickup coordinates.")
    public ResponseEntity<List<EligibleDriverDTO>> getEligibleDrivers(@PathVariable String rideId,
                                                                      @AuthenticationPrincipal AuthenticatedUser caller) {
        List<EligibleDriverDTO> drivers = rideService.findEligibleDriversForRide(rideId, resolveCaller(caller));
        return ResponseEntity.ok(drivers);
    }

    @PatchMapping("/{rideId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER', 'DRIVER')")
    @Operation(summary = "Assign driver to ride", description = "Transitions ride from REQUESTED to ASSIGNED state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver successfully assigned"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition (ride not in REQUESTED state)")
    })
    public ResponseEntity<RideResponse> assignDriver(@PathVariable String rideId,
                                                     @Valid @RequestBody AssignDriverRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser caller) {
        RideResponse response = rideService.assignDriver(rideId, request, resolveCaller(caller));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rideId}/accept")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary = "Driver accepts assigned ride", description = "Transitions ride from ASSIGNED to ACCEPTED state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride accepted by driver"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition"),
            @ApiResponse(responseCode = "403", description = "Only the assigned driver may accept")
    })
    public ResponseEntity<RideResponse> acceptRide(@PathVariable String rideId,
                                                   @AuthenticationPrincipal AuthenticatedUser caller) {
        RideResponse response = rideService.acceptRide(rideId, resolveCaller(caller));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rideId}/start")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary = "Driver starts ride", description = "Transitions ride from ACCEPTED to IN_PROGRESS and updates driver status to ON_TRIP.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride is now in progress"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition")
    })
    public ResponseEntity<RideResponse> startRide(@PathVariable String rideId,
                                                  @AuthenticationPrincipal AuthenticatedUser caller) {
        RideResponse response = rideService.startRide(rideId, resolveCaller(caller));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rideId}/complete")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary = "Driver completes ride and processes payment", description = "Transitions ride from IN_PROGRESS to COMPLETED, requests final fare & payment from Fare Service, generates receipt, and marks driver AVAILABLE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride completed and settled successfully"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition"),
            @ApiResponse(responseCode = "502", description = "Downstream Fare & Payment Service error")
    })
    public ResponseEntity<RideResponse> completeRide(@PathVariable String rideId,
                                                     @Valid @RequestBody CompleteRideRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser caller) {
        RideResponse response = rideService.completeRide(rideId, request, resolveCaller(caller));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rideId}/cancel")
    @Operation(summary = "Cancel ride", description = "Transitions ride from non-terminal states to CANCELLED and frees up driver availability.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride cancelled successfully"),
            @ApiResponse(responseCode = "409", description = "Cannot cancel already completed or cancelled ride"),
            @ApiResponse(responseCode = "403", description = "Unauthorized cancellation attempt")
    })
    public ResponseEntity<RideResponse> cancelRide(@PathVariable String rideId,
                                                   @RequestBody(required = false) CancelRideRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser caller) {
        RideResponse response = rideService.cancelRide(rideId, request, resolveCaller(caller));
        return ResponseEntity.ok(response);
    }
}
