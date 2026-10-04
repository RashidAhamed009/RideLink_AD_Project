package com.ridelink.ride.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.ride.dto.*;
import com.ridelink.ride.exception.*;
import com.ridelink.ride.model.LocationPoint;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RideControllerValidationTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private RideService rideService;

    @InjectMocks
    private RideController rideController;

    private CreateRideRequest validCreateReq;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(rideController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        LocationPoint pickup = new LocationPoint("Colombo Fort", 6.9344, 79.8428);
        LocationPoint dest = new LocationPoint("Bambalapitiya", 6.8918, 79.8587);
        validCreateReq = new CreateRideRequest(pickup, dest, 6.2, 18.0);
    }

    @Test
    @DisplayName("MVC: Create ride returns 201 Created with valid request")
    void testCreateRide_ValidInput_ReturnsCreated() throws Exception {
        Ride sample = new Ride("usr-pass-1", validCreateReq.getPickup(), validCreateReq.getDestination(), 20.0, 6.2, 18.0);
        sample.setRideId("ride-mvc-1");
        when(rideService.createRide(any(), any(CreateRideRequest.class)))
                .thenReturn(RideResponse.fromEntity(sample));

        mockMvc.perform(post("/api/v1/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rideId").value("ride-mvc-1"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @DisplayName("MVC: Create ride with blank pickup address returns 400 Bad Request")
    void testCreateRide_BlankPickupAddress_ReturnsBadRequest() throws Exception {
        CreateRideRequest req = new CreateRideRequest(
                new LocationPoint("", 6.9344, 79.8428),
                new LocationPoint("Bambalapitiya", 6.8918, 79.8587),
                5.0, 15.0
        );

        mockMvc.perform(post("/api/v1/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("pickup.address"));
    }

    @Test
    @DisplayName("MVC: Create ride with out-of-range latitude returns 400 Bad Request")
    void testCreateRide_InvalidLatitude_ReturnsBadRequest() throws Exception {
        CreateRideRequest req = new CreateRideRequest(
                new LocationPoint("Colombo Fort", 195.0, 79.8428),
                new LocationPoint("Bambalapitiya", 6.8918, 79.8587),
                5.0, 15.0
        );

        mockMvc.perform(post("/api/v1/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("pickup.latitude"));
    }

    @Test
    @DisplayName("MVC: Create ride returns 404 when no drivers are available")
    void testCreateRide_NoDriversAvailable_ReturnsNotFound() throws Exception {
        when(rideService.createRide(any(), any(CreateRideRequest.class)))
                .thenThrow(new NoDriversAvailableException("No drivers available"));

        mockMvc.perform(post("/api/v1/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NO_DRIVERS_AVAILABLE"));
    }

    @Test
    @DisplayName("MVC: Assign driver on invalid ride state returns 409 Conflict")
    void testAssignDriver_InvalidState_ReturnsConflict() throws Exception {
        AssignDriverRequest req = new AssignDriverRequest("drv-123");
        when(rideService.assignDriver(any(), any(), any()))
                .thenThrow(new IllegalRideStateTransitionException(RideStatus.COMPLETED, RideStatus.ASSIGNED));

        mockMvc.perform(patch("/api/v1/rides/ride-1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    @DisplayName("MVC: Get ride by non-existent ID returns 404 Not Found")
    void testGetRideById_NotFound_ReturnsNotFound() throws Exception {
        when(rideService.getRideById(any(), any()))
                .thenThrow(new ResourceNotFoundException("Ride not found"));

        mockMvc.perform(get("/api/v1/rides/non-existent-ride"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("MVC: Unauthorized driver accept returns 403 Forbidden")
    void testAcceptRide_Unauthorized_ReturnsForbidden() throws Exception {
        when(rideService.acceptRide(any(), any()))
                .thenThrow(new UnauthorizedOperationException("Only the assigned driver can accept this ride."));

        mockMvc.perform(patch("/api/v1/rides/ride-1/accept"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED_ACCESS"));
    }

    @Test
    @DisplayName("MVC: Complete ride returns 200 OK")
    void testCompleteRide_ValidInput_ReturnsOk() throws Exception {
        CompleteRideRequest req = new CompleteRideRequest(6.5, 20.0, "SIMULATED_WALLET", 1.0);
        Ride sample = new Ride("pass-1", validCreateReq.getPickup(), validCreateReq.getDestination(), 20.0, 6.2, 18.0);
        sample.setStatus(RideStatus.COMPLETED);
        sample.setFinalFare(25.50);

        when(rideService.completeRide(any(), any(), any()))
                .thenReturn(RideResponse.fromEntity(sample));

        mockMvc.perform(patch("/api/v1/rides/ride-1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.finalFare").value(25.50));
    }

    @Test
    @DisplayName("MVC: Cancel ride returns 200 OK")
    void testCancelRide_ValidInput_ReturnsOk() throws Exception {
        CancelRideRequest req = new CancelRideRequest("Change of plans");
        Ride sample = new Ride("pass-1", validCreateReq.getPickup(), validCreateReq.getDestination(), 20.0, 6.2, 18.0);
        sample.setStatus(RideStatus.CANCELLED);
        sample.setCancellationReason("Change of plans");

        when(rideService.cancelRide(any(), any(), any()))
                .thenReturn(RideResponse.fromEntity(sample));

        mockMvc.perform(patch("/api/v1/rides/ride-1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Change of plans"));
    }

    @Test
    @DisplayName("Boundary: Create ride at minimum valid distance boundary (0.1 km) succeeds")
    void testCreateRide_BoundaryDistance_ExactlyMin_ReturnsCreated() throws Exception {
        CreateRideRequest req = new CreateRideRequest(validCreateReq.getPickup(), validCreateReq.getDestination(), 0.1, 1.0);
        Ride sample = new Ride("usr-pass-1", validCreateReq.getPickup(), validCreateReq.getDestination(), 3.5, 0.1, 1.0);
        sample.setRideId("ride-bound-min");

        when(rideService.createRide(any(), any(CreateRideRequest.class)))
                .thenReturn(RideResponse.fromEntity(sample));

        mockMvc.perform(post("/api/v1/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rideId").value("ride-bound-min"));
    }

    @Test
    @DisplayName("Boundary: Create ride with distance below boundary (0.05 km) returns 400 Bad Request")
    void testCreateRide_BoundaryDistance_BelowMin_ReturnsBadRequest() throws Exception {
        CreateRideRequest req = new CreateRideRequest(validCreateReq.getPickup(), validCreateReq.getDestination(), 0.05, 5.0);

        mockMvc.perform(post("/api/v1/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("estimatedDistanceKm"));
    }

    @Test
    @DisplayName("Boundary: Create ride with duration below boundary (0.5 min) returns 400 Bad Request")
    void testCreateRide_BoundaryDuration_BelowMin_ReturnsBadRequest() throws Exception {
        CreateRideRequest req = new CreateRideRequest(validCreateReq.getPickup(), validCreateReq.getDestination(), 5.0, 0.5);

        mockMvc.perform(post("/api/v1/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("estimatedDurationMinutes"));
    }
}
