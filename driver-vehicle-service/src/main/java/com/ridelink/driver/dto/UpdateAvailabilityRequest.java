package com.ridelink.driver.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.ridelink.driver.model.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateAvailabilityRequest {

    @NotNull(message = "Availability status is required (AVAILABLE, UNAVAILABLE, ON_TRIP)")
    @JsonAlias({"status", "availabilityStatus"})
    private AvailabilityStatus availabilityStatus;

    public UpdateAvailabilityRequest() {}

    public UpdateAvailabilityRequest(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public AvailabilityStatus getStatus() {
        return availabilityStatus;
    }

    public void setStatus(AvailabilityStatus status) {
        this.availabilityStatus = status;
    }
}
