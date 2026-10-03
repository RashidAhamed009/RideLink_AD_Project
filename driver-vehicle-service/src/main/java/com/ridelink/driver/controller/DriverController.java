package com.ridelink.driver.controller;

import com.ridelink.driver.dto.*;
import com.ridelink.driver.security.AuthenticatedUser;
import com.ridelink.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@Tag(name = "Driver & Vehicle", description = "Endpoints for driver profiles, vehicles, location & availability")
@SecurityRequirement(name = "BearerAuth")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    private String resolveDriverId(AuthenticatedUser user, String fallbackDriverId) {
        if (user != null && user.getUserId() != null) {
            return user.getUserId();
        }
        return fallbackDriverId;
    }

    @PostMapping("/profile")
    @Operation(summary = "Create or update driver operational profile", description = "Creates or updates operational metadata for the authenticated driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver profile saved successfully",
                    content = @Content(schema = @Schema(implementation = DriverProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error on input fields"),
            @ApiResponse(responseCode = "409", description = "License number already registered")
    })
    public ResponseEntity<DriverProfileResponse> createOrUpdateProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody DriverProfileRequest request) {
        String driverId = resolveDriverId(user, "driver-default");
        DriverProfileResponse response = driverService.createOrUpdateProfile(driverId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vehicles")
    @Operation(summary = "Register vehicle for driver", description = "Registers a new vehicle belonging to the authenticated driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle registered successfully",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Vehicle registration number already taken")
    })
    public ResponseEntity<VehicleResponse> addVehicle(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody VehicleRequest request) {
        String driverId = resolveDriverId(user, "driver-default");
        VehicleResponse response = driverService.addVehicle(driverId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/vehicles/{vehicleId}")
    @Operation(summary = "Update vehicle details", description = "Updates details of a registered vehicle.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "409", description = "Registration number already taken")
    })
    public ResponseEntity<VehicleResponse> updateVehicle(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String vehicleId,
            @Valid @RequestBody VehicleRequest request) {
        String driverId = resolveDriverId(user, "driver-default");
        VehicleResponse response = driverService.updateVehicle(driverId, vehicleId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "View own driver details", description = "Returns operational profile and vehicles for authenticated driver.")
    public ResponseEntity<DriverDetailsResponse> getOwnDetails(@AuthenticationPrincipal AuthenticatedUser user) {
        String driverId = resolveDriverId(user, "driver-default");
        DriverDetailsResponse response = driverService.getDriverDetails(driverId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{driverId}")
    @Operation(summary = "View driver details by ID", description = "Returns operational profile and vehicle records for any driver ID.")
    public ResponseEntity<DriverDetailsResponse> getDriverDetails(@PathVariable String driverId) {
        DriverDetailsResponse response = driverService.getDriverDetails(driverId);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{driverId}/availability", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Update driver availability status", description = "Sets availability to AVAILABLE, UNAVAILABLE, or ON_TRIP.")
    public ResponseEntity<DriverProfileResponse> updateAvailability(
            @PathVariable String driverId,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        DriverProfileResponse response = driverService.updateAvailability(driverId, request);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{driverId}/location", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Update simulated driver location", description = "Updates latitude, longitude coordinates for the driver.")
    public ResponseEntity<DriverProfileResponse> updateLocation(
            @PathVariable String driverId,
            @Valid @RequestBody UpdateLocationRequest request) {
        DriverProfileResponse response = driverService.updateLocation(driverId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @Operation(summary = "Retrieve eligible available drivers", description = "Matches eligible drivers considering availability, location proximity, and registered vehicle.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eligible drivers retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EligibleDriverResponse.class))))
    })
    public ResponseEntity<List<EligibleDriverResponse>> getAvailableDrivers(
            @RequestParam(required = false) String serviceArea,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radiusKm) {
        List<EligibleDriverResponse> eligible = driverService.findEligibleDrivers(serviceArea, latitude, longitude, radiusKm);
        return ResponseEntity.ok(eligible);
    }
}
