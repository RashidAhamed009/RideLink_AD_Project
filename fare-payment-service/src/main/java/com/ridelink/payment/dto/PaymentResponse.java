package com.ridelink.payment.dto;

import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;

import java.time.Instant;

public class PaymentResponse {

    private String paymentId;
    private String rideId;
    private String passengerId;
    private double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionReference;
    private String failureReason;
    private String receiptId;
    private Instant createdAt;

    public PaymentResponse() {}

    public PaymentResponse(String paymentId, String rideId, String passengerId, double amount,
                           PaymentMethod paymentMethod, PaymentStatus paymentStatus,
                           String transactionReference, String failureReason, String receiptId,
                           Instant createdAt) {
        this.paymentId = paymentId;
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionReference = transactionReference;
        this.failureReason = failureReason;
        this.receiptId = receiptId;
        this.createdAt = createdAt;
    }

    public static PaymentResponse fromEntity(Payment payment, String receiptId) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getRideId(),
                payment.getPassengerId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getTransactionReference(),
                payment.getFailureReason(),
                receiptId,
                payment.getCreatedAt()
        );
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
