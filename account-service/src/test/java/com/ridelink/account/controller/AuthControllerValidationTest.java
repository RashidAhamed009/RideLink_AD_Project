package com.ridelink.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.account.dto.AuthResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.UserProfileResponse;
import com.ridelink.account.exception.DuplicateEmailException;
import com.ridelink.account.exception.GlobalExceptionHandler;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerValidationTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("MVC: Valid passenger registration returns 201 Created")
    void testRegisterPassenger_Success() throws Exception {
        RegisterRequest req = new RegisterRequest("Sarah Connor", "sarah@example.com", "SecurePass123!");
        UserProfileResponse res = new UserProfileResponse("u1", "Sarah Connor", "sarah@example.com", Role.PASSENGER, AccountStatus.ACTIVE, Instant.now(), Instant.now());

        when(accountService.registerPassenger(any(RegisterRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/register/passenger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("u1"))
                .andExpect(jsonPath("$.email").value("sarah@example.com"))
                .andExpect(jsonPath("$.role").value("PASSENGER"));
    }

    @Test
    @DisplayName("MVC: Register with invalid email returns 400 Bad Request with validationErrors")
    void testRegister_InvalidEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest req = new RegisterRequest("Sarah Connor", "invalid-email-format", "SecurePass123!");

        mockMvc.perform(post("/api/v1/auth/register/passenger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("email"));
    }

    @Test
    @DisplayName("MVC: Register with weak password (< 8 chars) returns 400 Bad Request")
    void testRegister_WeakPassword_ReturnsBadRequest() throws Exception {
        RegisterRequest req = new RegisterRequest("Sarah Connor", "sarah@example.com", "short");

        mockMvc.perform(post("/api/v1/auth/register/passenger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("password"));
    }

    @Test
    @DisplayName("MVC: Register with blank name returns 400 Bad Request")
    void testRegister_BlankName_ReturnsBadRequest() throws Exception {
        RegisterRequest req = new RegisterRequest("", "sarah@example.com", "SecurePass123!");

        mockMvc.perform(post("/api/v1/auth/register/passenger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("MVC: Register duplicate email returns 409 Conflict")
    void testRegister_DuplicateEmail_ReturnsConflict() throws Exception {
        RegisterRequest req = new RegisterRequest("Sarah Connor", "sarah@example.com", "SecurePass123!");

        when(accountService.registerPassenger(any(RegisterRequest.class)))
                .thenThrow(new DuplicateEmailException("Email address is already registered: sarah@example.com"));

        mockMvc.perform(post("/api/v1/auth/register/passenger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.message").value("Email address is already registered: sarah@example.com"));
    }

    @Test
    @DisplayName("MVC: Valid login returns 200 OK and JWT token")
    void testLogin_Success() throws Exception {
        LoginRequest req = new LoginRequest("sarah@example.com", "SecurePass123!");
        UserProfileResponse userRes = new UserProfileResponse("u1", "Sarah", "sarah@example.com", Role.PASSENGER, AccountStatus.ACTIVE, Instant.now(), Instant.now());
        AuthResponse authRes = new AuthResponse("mocked.jwt.token", 86400000L, userRes);

        when(accountService.login(any(LoginRequest.class))).thenReturn(authRes);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("sarah@example.com"));
    }

    @Test
    @DisplayName("MVC: Login with invalid credentials returns 401 Unauthorized")
    void testLogin_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        LoginRequest req = new LoginRequest("sarah@example.com", "WrongPassword!");

        when(accountService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Boundary: Register with password at minimum length boundary (exactly 8 chars) succeeds")
    void testRegister_BoundaryPassword_Exactly8Chars_Succeeds() throws Exception {
        RegisterRequest req = new RegisterRequest("Boundary User", "boundary8@example.com", "12345678");
        UserProfileResponse res = new UserProfileResponse("u-bound-8", "Boundary User", "boundary8@example.com", Role.PASSENGER, AccountStatus.ACTIVE, Instant.now(), Instant.now());

        when(accountService.registerPassenger(any(RegisterRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/register/passenger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("u-bound-8"));
    }

    @Test
    @DisplayName("Boundary: Register with password below minimum boundary (7 chars) returns 400 Bad Request")
    void testRegister_BoundaryPassword_7Chars_Fails() throws Exception {
        RegisterRequest req = new RegisterRequest("Boundary User", "boundary7@example.com", "1234567");

        mockMvc.perform(post("/api/v1/auth/register/passenger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("password"));
    }
}
