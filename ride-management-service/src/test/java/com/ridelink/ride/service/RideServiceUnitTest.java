package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.FarePaymentServiceClient;
import com.ridelink.ride.dto.*;
import com.ridelink.ride.exception.*;
import com.ridelink.ride.model.LocationPoint;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.security.AuthenticatedUser;
import com.ridelink.ride.service.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceUnitTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverServiceClient driverServiceClient;

    @Mock
    private FarePaymentServiceClient farePaymentServiceClient;

    private RideServiceImpl rideService;

    private AuthenticatedUser passengerCaller;
    private AuthenticatedUser assignedDriverCaller;
    private AuthenticatedUser unauthorizedDriverCaller;
    private AuthenticatedUser adminCaller;

    private CreateRideRequest sampleCreateReq;
    private Ride sampleRide;

    @BeforeEach
    void setUp() {
        rideService = new RideServiceImpl(rideRepository, driverServiceClient, farePaymentServiceClient);

        passengerCaller = new AuthenticatedUser("usr-pass-1", "passenger@test.com", List.of("PASSENGER"));
        assignedDriverCaller = new AuthenticatedUser("usr-drv-1", "driver@test.com", List.of("DRIVER"));
        unauthorizedDriverCaller = new AuthenticatedUser("usr-drv-999", "intruder@test.com", List.of("DRIVER"));
        adminCaller = new AuthenticatedUser("usr-admin-1", "admin@test.com", List.of("ADMIN"));

        LocationPoint pickup = new LocationPoint("Colombo Fort", 6.9344, 79.8428);
        LocationPoint dest = new LocationPoint("Bambalapitiya", 6.8918, 79.8587);
        sampleCreateReq = new CreateRideRequest(pickup, dest, 6.2, 18.0);

        sampleRide = new Ride("usr-pass-1", pickup, dest, 21.75, 6.2, 18.0);
        sampleRide.setRideId("ride-test-101");
    }

    @Test
    @DisplayName("Unit: Successful ride creation when eligible drivers exist")
    void testCreateRide_Success() {
        EligibleDriverDTO driver = new EligibleDriverDTO("usr-drv-1", "prof-1", "LIC-1122", "COLOMBO", 4.9, 2.5);
        FareEstimateDTO fare = new FareEstimateDTO("est-1", "ride-test-101", 3.0, 9.3, 4.5, 16.8, "USD");

        when(driverServiceClient.findEligibleDrivers(6.9344, 79.8428, 10.0))
                .thenReturn(List.of(driver));
        when(farePaymentServiceClient.estimateFare(any(), any(), any(), anyDouble(), anyDouble()))
                .thenReturn(fare);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.createRide("usr-pass-1", sampleCreateReq);

        assertNotNull(response);
        assertEquals("usr-pass-1", response.getPassengerId());
        assertEquals(RideStatus.REQUESTED, response.getStatus());
        assertEquals(16.8, response.getEstimatedFare());
        assertNotNull(response.getRequestedAt());
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    @DisplayName("Unit: Ride creation fails with NoDriversAvailableException when no drivers nearby")
    void testCreateRide_NoDriversAvailable_ThrowsException() {
        when(driverServiceClient.findEligibleDrivers(6.9344, 79.8428, 10.0))
                .thenReturn(Collections.emptyList());

        assertThrows(NoDriversAvailableException.class,
                () -> rideService.createRide("usr-pass-1", sampleCreateReq));

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    @DisplayName("Unit: Successful driver assignment transitions REQUESTED to ASSIGNED")
    void testAssignDriver_Success() {
        sampleRide.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignDriverRequest assignReq = new AssignDriverRequest("usr-drv-1");
        RideResponse res = rideService.assignDriver("ride-test-101", assignReq, adminCaller);

        assertNotNull(res);
        assertEquals(RideStatus.ASSIGNED, res.getStatus());
        assertEquals("usr-drv-1", res.getDriverId());
        assertNotNull(res.getAssignedAt());
    }

    @Test
    @DisplayName("Unit: Invalid driver assignment rejects blank driver ID")
    void testAssignDriver_BlankDriverId_ThrowsException() {
        sampleRide.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));

        AssignDriverRequest assignReq = new AssignDriverRequest("");

        assertThrows(IllegalArgumentException.class,
                () -> rideService.assignDriver("ride-test-101", assignReq, adminCaller));
    }

    @Test
    @DisplayName("Unit: Invalid driver assignment on already COMPLETED ride throws IllegalRideStateTransitionException")
    void testAssignDriver_InvalidState_ThrowsException() {
        sampleRide.setStatus(RideStatus.COMPLETED);
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));

        AssignDriverRequest assignReq = new AssignDriverRequest("usr-drv-1");

        assertThrows(IllegalRideStateTransitionException.class,
                () -> rideService.assignDriver("ride-test-101", assignReq, adminCaller));
    }

    @Test
    @DisplayName("Unit: Assigned driver successfully accepts ride (ASSIGNED -> ACCEPTED)")
    void testAcceptRide_Success() {
        sampleRide.setStatus(RideStatus.ASSIGNED);
        sampleRide.setDriverId("usr-drv-1");
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse res = rideService.acceptRide("ride-test-101", assignedDriverCaller);

        assertNotNull(res);
        assertEquals(RideStatus.ACCEPTED, res.getStatus());
        assertNotNull(res.getAcceptedAt());
    }

    @Test
    @DisplayName("Unit: Unauthorized driver cannot accept someone else's ride")
    void testAcceptRide_UnauthorizedDriver_ThrowsException() {
        sampleRide.setStatus(RideStatus.ASSIGNED);
        sampleRide.setDriverId("usr-drv-1");
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));

        assertThrows(UnauthorizedOperationException.class,
                () -> rideService.acceptRide("ride-test-101", unauthorizedDriverCaller));
    }

    @Test
    @DisplayName("Unit: Start ride transitions ACCEPTED to IN_PROGRESS and notifies Driver Service")
    void testStartRide_Success() {
        sampleRide.setStatus(RideStatus.ACCEPTED);
        sampleRide.setDriverId("usr-drv-1");
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse res = rideService.startRide("ride-test-101", assignedDriverCaller);

        assertNotNull(res);
        assertEquals(RideStatus.IN_PROGRESS, res.getStatus());
        assertNotNull(res.getStartedAt());
        verify(driverServiceClient).updateDriverAvailability("usr-drv-1", "ON_TRIP");
    }

    @Test
    @DisplayName("Unit: Complete ride calculates final fare, processes payment, and marks driver AVAILABLE")
    void testCompleteRide_Success() {
        sampleRide.setStatus(RideStatus.IN_PROGRESS);
        sampleRide.setDriverId("usr-drv-1");
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinalFareDTO finalFare = new FinalFareDTO("ride-test-101", 3.0, 9.75, 5.0, 1.0, 24.50, "USD");
        PaymentResponseDTO payment = new PaymentResponseDTO("pmt-1", "ride-test-101", "usr-pass-1",
                24.50, "SIMULATED_WALLET", "SUCCESS", "TXN-SIM-12345", null, "rcpt-1");

        when(farePaymentServiceClient.calculateFinalFare("ride-test-101", 6.5, 20.0, null))
                .thenReturn(finalFare);
        when(farePaymentServiceClient.processPayment("ride-test-101", "usr-pass-1", "usr-drv-1", 24.50, "SIMULATED_WALLET"))
                .thenReturn(payment);

        CompleteRideRequest compReq = new CompleteRideRequest(6.5, 20.0, "SIMULATED_WALLET", null);
        RideResponse res = rideService.completeRide("ride-test-101", compReq, assignedDriverCaller);

        assertNotNull(res);
        assertEquals(RideStatus.COMPLETED, res.getStatus());
        assertEquals(24.50, res.getFinalFare());
        assertEquals("TXN-SIM-12345", res.getPaymentReference());
        assertEquals("rcpt-1", res.getReceiptId());
        assertNotNull(res.getCompletedAt());

        verify(driverServiceClient).updateDriverAvailability("usr-drv-1", "AVAILABLE");
    }

    @Test
    @DisplayName("Unit: Cancel ride transitions to CANCELLED and resets driver availability")
    void testCancelRide_Success() {
        sampleRide.setStatus(RideStatus.ASSIGNED);
        sampleRide.setDriverId("usr-drv-1");
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CancelRideRequest cancelReq = new CancelRideRequest("Driver took too long");
        RideResponse res = rideService.cancelRide("ride-test-101", cancelReq, passengerCaller);

        assertNotNull(res);
        assertEquals(RideStatus.CANCELLED, res.getStatus());
        assertEquals("Driver took too long", res.getCancellationReason());
        assertNotNull(res.getCancelledAt());
        verify(driverServiceClient).updateDriverAvailability("usr-drv-1", "AVAILABLE");
    }

    @Test
    @DisplayName("Unit: Invalid status transition - cannot complete a REQUESTED ride directly")
    void testInvalidStatusTransition_RequestedToCompleted_ThrowsException() {
        sampleRide.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));

        CompleteRideRequest compReq = new CompleteRideRequest(5.0, 15.0, "CASH", 1.0);

        assertThrows(IllegalRideStateTransitionException.class,
                () -> rideService.completeRide("ride-test-101", compReq, assignedDriverCaller));
    }

    @Test
    @DisplayName("Unit: Invalid status transition - cannot cancel already completed ride")
    void testInvalidStatusTransition_CompletedToCancelled_ThrowsException() {
        sampleRide.setStatus(RideStatus.COMPLETED);
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));

        CancelRideRequest cancelReq = new CancelRideRequest("Mistake");

        assertThrows(IllegalRideStateTransitionException.class,
                () -> rideService.cancelRide("ride-test-101", cancelReq, passengerCaller));
    }

    @Test
    @DisplayName("Unit: Downstream Driver Service failure propagates DownstreamServiceException")
    void testDownstreamFailure_DriverService_ThrowsException() {
        when(driverServiceClient.findEligibleDrivers(anyDouble(), anyDouble(), anyDouble()))
                .thenThrow(new DownstreamServiceException("driver-vehicle-service", "Connection timed out"));

        assertThrows(DownstreamServiceException.class,
                () -> rideService.createRide("usr-pass-1", sampleCreateReq));
    }

    @Test
    @DisplayName("Unit: Downstream Payment Service failure during completion propagates DownstreamServiceException")
    void testDownstreamFailure_PaymentService_ThrowsException() {
        sampleRide.setStatus(RideStatus.IN_PROGRESS);
        sampleRide.setDriverId("usr-drv-1");
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));

        FinalFareDTO finalFare = new FinalFareDTO("ride-test-101", 3.0, 9.75, 5.0, 1.0, 24.50, "USD");
        when(farePaymentServiceClient.calculateFinalFare(any(), anyDouble(), anyDouble(), any()))
                .thenReturn(finalFare);
        when(farePaymentServiceClient.processPayment(any(), any(), any(), anyDouble(), any()))
                .thenThrow(new DownstreamServiceException("fare-payment-service", "Payment gateway connection refused"));

        CompleteRideRequest compReq = new CompleteRideRequest(6.5, 20.0, "SIMULATED_WALLET", null);

        assertThrows(DownstreamServiceException.class,
                () -> rideService.completeRide("ride-test-101", compReq, assignedDriverCaller));
    }

    @Test
    @DisplayName("Unit: Unauthorized passenger cannot view or cancel someone else's ride")
    void testUnauthorizedOperation_UnrelatedUser_ThrowsException() {
        sampleRide.setStatus(RideStatus.REQUESTED);
        sampleRide.setPassengerId("usr-pass-1");
        when(rideRepository.findByRideId("ride-test-101")).thenReturn(Optional.of(sampleRide));

        AuthenticatedUser intruder = new AuthenticatedUser("usr-intruder", "intruder@test.com", List.of("PASSENGER"));

        assertThrows(UnauthorizedOperationException.class,
                () -> rideService.getRideById("ride-test-101", intruder));

        assertThrows(UnauthorizedOperationException.class,
                () -> rideService.cancelRide("ride-test-101", new CancelRideRequest("Hack"), intruder));
    }
}
