package com.ridelink.driver.service;

import com.ridelink.driver.dto.*;
import com.ridelink.driver.exception.DuplicateResourceException;
import com.ridelink.driver.exception.ResourceNotFoundException;
import com.ridelink.driver.model.AvailabilityStatus;
import com.ridelink.driver.model.DriverProfile;
import com.ridelink.driver.model.GeoLocation;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.repository.DriverProfileRepository;
import com.ridelink.driver.repository.VehicleRepository;
import com.ridelink.driver.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceUnitTest {

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private DriverServiceImpl driverService;

    private DriverProfile sampleProfile;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleProfile = new DriverProfile("drv-123", "LIC-998877", "COLOMBO_CENTRAL");
        sampleProfile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        sampleProfile.setSimulatedCurrentLocation(new GeoLocation(6.9271, 79.8612, "Fort, Colombo"));
        sampleProfile.setRating(4.9);

        sampleVehicle = new Vehicle("drv-123", "CAB-1122", "Toyota", "Prius", "SEDAN", "White", 2021);
    }

    @Test
    @DisplayName("Unit: Create driver profile successfully")
    void testCreateProfile_Success() {
        DriverProfileRequest req = new DriverProfileRequest("LIC-998877", "COLOMBO_CENTRAL", 6.9271, 79.8612, "Fort");
        when(driverProfileRepository.findByDriverId("drv-123")).thenReturn(Optional.empty());
        when(driverProfileRepository.existsByLicenseNumber("LIC-998877")).thenReturn(false);
        when(driverProfileRepository.save(any(DriverProfile.class))).thenReturn(sampleProfile);

        DriverProfileResponse res = driverService.createOrUpdateProfile("drv-123", req);

        assertNotNull(res);
        assertEquals("drv-123", res.getDriverId());
        assertEquals("LIC-998877", res.getLicenseNumber());
        verify(driverProfileRepository).save(any(DriverProfile.class));
    }

    @Test
    @DisplayName("Unit: Duplicate license number throws DuplicateResourceException (409)")
    void testCreateProfile_DuplicateLicense_ThrowsException() {
        DriverProfileRequest req = new DriverProfileRequest("LIC-998877", "COLOMBO_CENTRAL", null, null, null);
        when(driverProfileRepository.findByDriverId("drv-123")).thenReturn(Optional.empty());
        when(driverProfileRepository.existsByLicenseNumber("LIC-998877")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> driverService.createOrUpdateProfile("drv-123", req));
        verify(driverProfileRepository, never()).save(any(DriverProfile.class));
    }

    @Test
    @DisplayName("Unit: Add vehicle successfully")
    void testAddVehicle_Success() {
        VehicleRequest req = new VehicleRequest("CAB-1122", "Toyota", "Prius", "SEDAN", "White", 2021);
        when(vehicleRepository.existsByRegistrationNumber("CAB-1122")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        VehicleResponse res = driverService.addVehicle("drv-123", req);

        assertNotNull(res);
        assertEquals("CAB-1122", res.getRegistrationNumber());
        assertEquals("Toyota", res.getMake());
        assertEquals("drv-123", res.getDriverId());
    }

    @Test
    @DisplayName("Unit: Add vehicle with duplicate registration number throws DuplicateResourceException")
    void testAddVehicle_DuplicateRegistration_ThrowsException() {
        VehicleRequest req = new VehicleRequest("CAB-1122", "Toyota", "Prius", "SEDAN", "White", 2021);
        when(vehicleRepository.existsByRegistrationNumber("CAB-1122")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> driverService.addVehicle("drv-123", req));
    }

    @Test
    @DisplayName("Unit: Update availability successfully")
    void testUpdateAvailability_Success() {
        UpdateAvailabilityRequest req = new UpdateAvailabilityRequest(AvailabilityStatus.ON_TRIP);
        when(driverProfileRepository.findByDriverId("drv-123")).thenReturn(Optional.of(sampleProfile));
        when(driverProfileRepository.save(any(DriverProfile.class))).thenReturn(sampleProfile);

        DriverProfileResponse res = driverService.updateAvailability("drv-123", req);

        assertNotNull(res);
        assertEquals(AvailabilityStatus.ON_TRIP, sampleProfile.getAvailabilityStatus());
    }

    @Test
    @DisplayName("Unit: Update simulated location successfully")
    void testUpdateLocation_Success() {
        UpdateLocationRequest req = new UpdateLocationRequest(6.9056, 79.8510, "Bambalapitiya");
        when(driverProfileRepository.findByDriverId("drv-123")).thenReturn(Optional.of(sampleProfile));
        when(driverProfileRepository.save(any(DriverProfile.class))).thenReturn(sampleProfile);

        DriverProfileResponse res = driverService.updateLocation("drv-123", req);

        assertNotNull(res);
        assertEquals(6.9056, sampleProfile.getSimulatedCurrentLocation().getLatitude());
        assertEquals(79.8510, sampleProfile.getSimulatedCurrentLocation().getLongitude());
    }

    @Test
    @DisplayName("Unit: Eligibility algorithm matches nearby available driver with vehicle")
    void testFindEligibleDrivers_MatchesDriver() {
        when(driverProfileRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(sampleProfile));
        when(vehicleRepository.findAllByDriverId("drv-123"))
                .thenReturn(List.of(sampleVehicle));

        // Request near driver (Fort, Colombo: 6.9271, 79.8612) -> search within 5 km
        List<EligibleDriverResponse> eligible = driverService.findEligibleDrivers(null, 6.9300, 79.8600, 5.0);

        assertEquals(1, eligible.size());
        assertEquals("drv-123", eligible.get(0).getDriverId());
        assertEquals("CAB-1122", eligible.get(0).getVehicle().getRegistrationNumber());
        assertNotNull(eligible.get(0).getDistanceKm());
    }

    @Test
    @DisplayName("Unit: Eligibility algorithm filters out drivers without a vehicle")
    void testFindEligibleDrivers_FiltersOutDriversWithoutVehicles() {
        when(driverProfileRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(sampleProfile));
        when(vehicleRepository.findAllByDriverId("drv-123"))
                .thenReturn(List.of()); // No vehicle

        List<EligibleDriverResponse> eligible = driverService.findEligibleDrivers(null, 6.9300, 79.8600, 5.0);

        assertTrue(eligible.isEmpty());
    }

    @Test
    @DisplayName("Unit: Eligibility algorithm filters out drivers beyond search radius")
    void testFindEligibleDrivers_FiltersOutDriversBeyondRadius() {
        // Driver is in Colombo (lat: 6.9271, lon: 79.8612)
        when(driverProfileRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(sampleProfile));
        when(vehicleRepository.findAllByDriverId("drv-123"))
                .thenReturn(List.of(sampleVehicle));

        // Passenger is in Kandy (lat: 7.2906, lon: 80.6337) -> ~115 km away, radius 10 km
        List<EligibleDriverResponse> eligible = driverService.findEligibleDrivers(null, 7.2906, 80.6337, 10.0);
        assertTrue(eligible.isEmpty());
    }

    @Test
    @DisplayName("Unit: Eligibility algorithm excludes drivers with UNAVAILABLE status")
    void testFindEligibleDrivers_ExcludesUnavailableDrivers() {
        when(driverProfileRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of()); // No AVAILABLE drivers found

        List<EligibleDriverResponse> eligible = driverService.findEligibleDrivers(null, 6.9300, 79.8600, 5.0);

        assertTrue(eligible.isEmpty());
        verify(driverProfileRepository).findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        verify(vehicleRepository, never()).findAllByDriverId(anyString());
    }

    @Test
    @DisplayName("Unit: Get driver details for invalid driver ID throws ResourceNotFoundException (404)")
    void testGetDriverDetails_InvalidDriver_ThrowsResourceNotFoundException() {
        when(driverProfileRepository.findByDriverId("invalid-driver-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.getDriverDetails("invalid-driver-id"));
        verify(vehicleRepository, never()).findAllByDriverId(anyString());
    }

    @Test
    @DisplayName("Unit: Update availability for non-existent driver throws ResourceNotFoundException (404)")
    void testUpdateAvailability_InvalidDriver_ThrowsResourceNotFoundException() {
        when(driverProfileRepository.findByDriverId("invalid-driver-id")).thenReturn(Optional.empty());

        UpdateAvailabilityRequest req = new UpdateAvailabilityRequest(AvailabilityStatus.AVAILABLE);
        assertThrows(ResourceNotFoundException.class, () -> driverService.updateAvailability("invalid-driver-id", req));
        verify(driverProfileRepository, never()).save(any(DriverProfile.class));
    }

    @Test
    @DisplayName("Unit: Update simulated location for non-existent driver throws ResourceNotFoundException (404)")
    void testUpdateLocation_InvalidDriver_ThrowsResourceNotFoundException() {
        when(driverProfileRepository.findByDriverId("invalid-driver-id")).thenReturn(Optional.empty());

        UpdateLocationRequest req = new UpdateLocationRequest(6.9271, 79.8612, "Fort");
        assertThrows(ResourceNotFoundException.class, () -> driverService.updateLocation("invalid-driver-id", req));
        verify(driverProfileRepository, never()).save(any(DriverProfile.class));
    }
}
