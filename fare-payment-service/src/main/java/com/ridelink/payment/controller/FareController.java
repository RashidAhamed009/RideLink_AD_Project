package com.ridelink.payment.controller;

import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.service.FarePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fares")
@Tag(name = "Fare Engine", description = "Endpoints for deterministic fare estimation and final calculation")
@SecurityRequirement(name = "BearerAuth")
public class FareController {

    private final FarePaymentService farePaymentService;

    public FareController(FarePaymentService farePaymentService) {
        this.farePaymentService = farePaymentService;
    }

    @PostMapping("/estimate")
    @Operation(summary = "Calculate upfront fare estimate", description = "Calculates estimated fare based on distance and duration rules: baseFare + (dist * pricePerKm) + (time * pricePerMin).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fare estimated successfully",
                    content = @Content(schema = @Schema(implementation = FareEstimateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid distance or duration parameters")
    })
    public ResponseEntity<FareEstimateResponse> estimateFare(@Valid @RequestBody FareEstimateRequest request) {
        FareEstimateResponse response = farePaymentService.estimateFare(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/calculate", "/calculate-final"})
    @Operation(summary = "Calculate final ride fare", description = "Calculates final itemized fare from actual completed trip distance, duration, and surge multiplier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Final fare calculated successfully",
                    content = @Content(schema = @Schema(implementation = FinalFareResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid distance or duration")
    })
    public ResponseEntity<FinalFareResponse> calculateFinalFare(@Valid @RequestBody FinalFareRequest request) {
        FinalFareResponse response = farePaymentService.calculateFinalFare(request);
        return ResponseEntity.ok(response);
    }
}
