package com.ridelink.account.controller;

import com.ridelink.account.dto.AuthResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.UserProfileResponse;
import com.ridelink.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for registration and authentication")
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register/passenger")
    @Operation(summary = "Register a new passenger account", description = "Creates a new passenger profile with role PASSENGER and returns the profile details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Passenger registered successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed on input fields"),
            @ApiResponse(responseCode = "409", description = "Email is already registered")
    })
    public ResponseEntity<UserProfileResponse> registerPassenger(@Valid @RequestBody RegisterRequest request) {
        UserProfileResponse response = accountService.registerPassenger(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/driver")
    @Operation(summary = "Register a new driver account", description = "Creates a new driver profile with role DRIVER and returns the profile details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Driver registered successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed on input fields"),
            @ApiResponse(responseCode = "409", description = "Email is already registered")
    })
    public ResponseEntity<UserProfileResponse> registerDriver(@Valid @RequestBody RegisterRequest request) {
        UserProfileResponse response = accountService.registerDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT", description = "Verifies email and password, returning a signed JWT token if credentials are valid.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request payload"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "403", description = "Account is suspended or inactive")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = accountService.login(request);
        return ResponseEntity.ok(response);
    }
}
