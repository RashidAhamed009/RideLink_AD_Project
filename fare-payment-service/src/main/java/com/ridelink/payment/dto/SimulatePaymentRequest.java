package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SimulatePaymentRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotBlank(message = "Passenger ID is required")
    private String passengerId;

    @NotBlank(message = "Driver ID is required")
    private String driverId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    private Double amount;

    @NotNull(message = "Payment method is required (SIMULATED_WALLET, CREDIT_CARD, CASH)")
    private PaymentMethod paymentMethod;

    private Boolean simulateFailure;

    private String failureReason;

    public SimulatePaymentRequest() {}

    public SimulatePaymentRequest(String rideId, String passengerId, String driverId, Double amount,
                                  PaymentMethod paymentMethod, Boolean simulateFailure, String failureReason) {
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.simulateFailure = simulateFailure;
        this.failureReason = failureReason;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Boolean getSimulateFailure() {
        return simulateFailure;
    }

    public void setSimulateFailure(Boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
