package com.ridelink.ride.exception;

import com.ridelink.ride.model.RideStatus;

public class IllegalRideStateTransitionException extends RuntimeException {

    private final RideStatus currentStatus;
    private final RideStatus attemptedStatus;

    public IllegalRideStateTransitionException(RideStatus currentStatus, RideStatus attemptedStatus) {
        super(String.format("Invalid ride status transition from '%s' to '%s'.", currentStatus, attemptedStatus));
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    public IllegalRideStateTransitionException(String message) {
        super(message);
        this.currentStatus = null;
        this.attemptedStatus = null;
    }

    public RideStatus getCurrentStatus() {
        return currentStatus;
    }

    public RideStatus getAttemptedStatus() {
        return attemptedStatus;
    }
}
