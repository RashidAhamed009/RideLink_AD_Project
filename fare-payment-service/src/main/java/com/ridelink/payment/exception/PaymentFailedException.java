package com.ridelink.payment.exception;

public class PaymentFailedException extends RuntimeException {
    private final String failureReason;

    public PaymentFailedException(String message, String failureReason) {
        super(message);
        this.failureReason = failureReason;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
