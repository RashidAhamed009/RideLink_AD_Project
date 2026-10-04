package com.ridelink.payment.dto;

import com.ridelink.payment.model.Receipt;

import java.time.Instant;

public class ReceiptResponse {

    private String receiptId;
    private String paymentId;
    private String rideId;
    private String passengerId;
    private String driverId;
    private double totalAmount;
    private String currency;
    private double baseFare;
    private double distanceCharge;
    private double timeCharge;
    private String receiptNumber;
    private Instant issuedAt;

    public ReceiptResponse() {}

    public ReceiptResponse(String receiptId, String paymentId, String rideId, String passengerId,
                           String driverId, double totalAmount, String currency, double baseFare,
                           double distanceCharge, double timeCharge, String receiptNumber, Instant issuedAt) {
        this.receiptId = receiptId;
        this.paymentId = paymentId;
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.timeCharge = timeCharge;
        this.receiptNumber = receiptNumber;
        this.issuedAt = issuedAt;
    }

    public static ReceiptResponse fromEntity(Receipt receipt) {
        if (receipt == null) return null;
        return new ReceiptResponse(
                receipt.getReceiptId(),
                receipt.getPaymentId(),
                receipt.getRideId(),
                receipt.getPassengerId(),
                receipt.getDriverId(),
                receipt.getTotalAmount(),
                receipt.getCurrency(),
                receipt.getBaseFare(),
                receipt.getDistanceCharge(),
                receipt.getTimeCharge(),
                receipt.getReceiptNumber(),
                receipt.getIssuedAt()
        );
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
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

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }

    public double getDistanceCharge() {
        return distanceCharge;
    }

    public void setDistanceCharge(double distanceCharge) {
        this.distanceCharge = distanceCharge;
    }

    public double getTimeCharge() {
        return timeCharge;
    }

    public void setTimeCharge(double timeCharge) {
        this.timeCharge = timeCharge;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }
}
