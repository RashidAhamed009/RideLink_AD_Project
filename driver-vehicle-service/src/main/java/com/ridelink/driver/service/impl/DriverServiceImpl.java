package com.ridelink.driver.service.impl;

import com.ridelink.driver.dto.*;
import com.ridelink.driver.exception.DuplicateResourceException;
import com.ridelink.driver.exception.ResourceNotFoundException;
import com.ridelink.driver.model.AvailabilityStatus;
import com.ridelink.driver.model.DriverProfile;
import com.ridelink.driver.model.GeoLocation;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.repository.DriverProfileRepository;
import com.ridelink.driver.repository.VehicleRepository;
import com.ridelink.driver.service.DriverService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class DriverServiceImpl implements DriverService {

    private final DriverProfileRepository driverProfileRepository;
    private final VehicleRepository vehicleRepository;

    public DriverServiceImpl(DriverProfileRepository driverProfileRepository,
                             VehicleRepository vehicleRepository) {
        this.driverProfileRepository = driverProfileRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public DriverProfileResponse createOrUpdateProfile(String driverId, DriverProfileRequest request) {
        Optional<DriverProfile> existingOpt = driverProfileRepository.findByDriverId(driverId);
        DriverProfile profile;

        if (existingOpt.isPresent()) {
            profile = existingOpt.get();
            if (!profile.getLicenseNumber().equalsIgnoreCase(request.getLicenseNumber())
                    && driverProfileRepository.existsByLicenseNumber(request.getLicenseNumber())) {
                throw new DuplicateResourceException("License number is already registered: " + request.getLicenseNumber());
            }
        } else {
            if (driverProfileRepository.existsByLicenseNumber(request.getLicenseNumber())) {
                throw new DuplicateResourceException("License number is already registered: " + request.getLicenseNumber());
            }
            profile = new DriverProfile(driverId, request.getLicenseNumber(), request.getServiceArea());
        }

        profile.setLicenseNumber(request.getLicenseNumber().trim());
        profile.setServiceArea(request.getServiceArea().trim());

        if (request.getLatitude() != null && request.getLongitude() != null) {
            profile.setSimulatedCurrentLocation(new GeoLocation(
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getAddress() != null ? request.getAddress() : "Simulated Location"
            ));
        }

        DriverProfile saved = driverProfileRepository.save(profile);
        return DriverProfileResponse.fromEntity(saved);
    }

    @Override
    public VehicleResponse addVehicle(String driverId, VehicleRequest request) {
        String regNumber = request.getRegistrationNumber().trim().toUpperCase();
        if (vehicleRepository.existsByRegistrationNumber(regNumber)) {
            throw new DuplicateResourceException("Vehicle with registration number is already registered: " + regNumber);
        }

        Vehicle vehicle = new Vehicle(
                driverId,
                regNumber,
                request.getMake().trim(),
                request.getModel().trim(),
                request.getVehicleType().trim().toUpperCase(),
                request.getColor().trim(),
                request.getYear()
        );

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(saved);
    }

    @Override
    public VehicleResponse updateVehicle(String driverId, String vehicleId, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        if (!vehicle.getDriverId().equals(driverId)) {
            throw new ResourceNotFoundException("Vehicle not found or does not belong to driver: " + driverId);
        }

        String newReg = request.getRegistrationNumber().trim().toUpperCase();
        if (!vehicle.getRegistrationNumber().equalsIgnoreCase(newReg)
                && vehicleRepository.existsByRegistrationNumber(newReg)) {
            throw new DuplicateResourceException("Vehicle registration number is already taken: " + newReg);
        }

        vehicle.setRegistrationNumber(newReg);
        vehicle.setMake(request.getMake().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setVehicleType(request.getVehicleType().trim().toUpperCase());
        vehicle.setColor(request.getColor().trim());
        vehicle.setYear(request.getYear());
        vehicle.setUpdatedAt(Instant.now());

        Vehicle updated = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(updated);
    }

    @Override
    public DriverDetailsResponse getDriverDetails(String driverId) {
        DriverProfile profile = driverProfileRepository.findByDriverId(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver operational profile not found for id: " + driverId));

        List<VehicleResponse> vehicles = vehicleRepository.findAllByDriverId(driverId).stream()
                .map(VehicleResponse::fromEntity)
                .toList();

        return new DriverDetailsResponse(DriverProfileResponse.fromEntity(profile), vehicles);
    }

    @Override
    public DriverProfileResponse updateAvailability(String driverId, UpdateAvailabilityRequest request) {
        DriverProfile profile = driverProfileRepository.findByDriverId(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver operational profile not found for id: " + driverId));

        profile.setAvailabilityStatus(request.getAvailabilityStatus());
        DriverProfile updated = driverProfileRepository.save(profile);
        return DriverProfileResponse.fromEntity(updated);
    }

    @Override
    public DriverProfileResponse updateLocation(String driverId, UpdateLocationRequest request) {
        DriverProfile profile = driverProfileRepository.findByDriverId(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver operational profile not found for id: " + driverId));

        profile.setSimulatedCurrentLocation(new GeoLocation(
                request.getLatitude(),
                request.getLongitude(),
                request.getAddress() != null ? request.getAddress() : "Updated Coordinates"
        ));

        DriverProfile updated = driverProfileRepository.save(profile);
        return DriverProfileResponse.fromEntity(updated);
    }

    @Override
    public List<EligibleDriverResponse> findEligibleDrivers(String serviceArea, Double latitude, Double longitude, Double radiusKm) {
        double maxRadius = (radiusKm != null && radiusKm > 0) ? radiusKm : 10.0; // Default 10.0 km

        // 1. Fetch all drivers who are currently AVAILABLE
        List<DriverProfile> availableDrivers = driverProfileRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        List<EligibleDriverResponse> eligibleList = new ArrayList<>();

        for (DriverProfile profile : availableDrivers) {
            // 2. Validate driver has a registered vehicle
            List<Vehicle> vehicles = vehicleRepository.findAllByDriverId(profile.getDriverId());
            if (vehicles.isEmpty()) {
                continue; // Cannot fulfill ride requests without a vehicle
            }
            Vehicle primaryVehicle = vehicles.get(0);

            // 3. Proximity / Service Area Filtering
            Double distanceKm = null;
            if (latitude != null && longitude != null && profile.getSimulatedCurrentLocation() != null) {
                GeoLocation loc = profile.getSimulatedCurrentLocation();
                distanceKm = calculateHaversineDistanceKm(latitude, longitude, loc.getLatitude(), loc.getLongitude());

                if (distanceKm > maxRadius) {
                    continue; // Exceeds requested search radius
                }
            } else if (serviceArea != null && !serviceArea.isBlank()) {
                if (!profile.getServiceArea().equalsIgnoreCase(serviceArea.trim())) {
                    continue; // Service area mismatch
                }
            }

            eligibleList.add(new EligibleDriverResponse(
                    profile.getDriverId(),
                    profile.getId(),
                    profile.getLicenseNumber(),
                    profile.getServiceArea(),
                    profile.getSimulatedCurrentLocation(),
                    profile.getRating(),
                    distanceKm != null ? Math.round(distanceKm * 100.0) / 100.0 : null,
                    VehicleResponse.fromEntity(primaryVehicle)
            ));
        }

        // 4. Sort eligible drivers: nearest first, then highest rating
        eligibleList.sort(Comparator
                .comparing(EligibleDriverResponse::getDistanceKm, Comparator.nullsLast(Double::compareTo))
                .thenComparing(EligibleDriverResponse::getRating, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        return eligibleList;
    }

    /**
     * Haversine formula to compute great-circle distance between two geo-coordinates in kilometers.
     */
    private double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
