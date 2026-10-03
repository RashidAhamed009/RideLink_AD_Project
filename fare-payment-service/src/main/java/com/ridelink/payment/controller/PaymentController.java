package com.ridelink.payment.controller;

import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.dto.SimulatePaymentRequest;
import com.ridelink.payment.service.FarePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment & Receipts", description = "Endpoints for simulated wallet payments, status check, and receipts")
@SecurityRequirement(name = "BearerAuth")
public class PaymentController {

    private final FarePaymentService farePaymentService;

    public PaymentController(FarePaymentService farePaymentService) {
        this.farePaymentService = farePaymentService;
    }

    @PostMapping("/process")
    @Operation(summary = "Simulate payment transaction", description = "Records a simulated wallet/card transaction for a ride and generates a receipt. Supports deterministic failure simulation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed and receipt generated",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment input"),
            @ApiResponse(responseCode = "409", description = "Duplicate payment: ride already paid"),
            @ApiResponse(responseCode = "422", description = "Simulated payment declined (insufficient funds, etc.)")
    })
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody SimulatePaymentRequest request) {
        PaymentResponse response = farePaymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/rides/{rideId}")
    @Operation(summary = "Retrieve payment status by ride ID", description = "Checks the status of payment recorded for a specific ride.")
    public ResponseEntity<PaymentResponse> getPaymentByRideId(@PathVariable String rideId) {
        PaymentResponse response = farePaymentService.getPaymentByRideId(rideId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Retrieve payment details by payment ID", description = "Checks payment details using the unique payment UUID.")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable String paymentId) {
        PaymentResponse response = farePaymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/receipts/{receiptId}")
    @Operation(summary = "Retrieve receipt by receipt ID", description = "Returns itemized invoice/receipt details.")
    public ResponseEntity<ReceiptResponse> getReceiptById(@PathVariable String receiptId) {
        ReceiptResponse response = farePaymentService.getReceiptById(receiptId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/receipts/rides/{rideId}")
    @Operation(summary = "Retrieve receipt by ride ID", description = "Returns itemized invoice/receipt for a completed ride.")
    public ResponseEntity<ReceiptResponse> getReceiptByRideId(@PathVariable String rideId) {
        ReceiptResponse response = farePaymentService.getReceiptByRideId(rideId);
        return ResponseEntity.ok(response);
    }
}
