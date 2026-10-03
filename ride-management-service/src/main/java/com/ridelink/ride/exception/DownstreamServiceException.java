package com.ridelink.ride.exception;

public class DownstreamServiceException extends RuntimeException {

    private final String serviceName;

    public DownstreamServiceException(String serviceName, String message) {
        super(String.format("Downstream service '%s' failed: %s", serviceName, message));
        this.serviceName = serviceName;
    }

    public DownstreamServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("Downstream service '%s' failed: %s", serviceName, message), cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
