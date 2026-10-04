package com.ridelink.account.service;

import com.ridelink.account.dto.*;
import com.ridelink.account.exception.AccountSuspendedException;
import com.ridelink.account.exception.DuplicateEmailException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.exception.ResourceNotFoundException;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.model.User;
import com.ridelink.account.repository.UserRepository;
import com.ridelink.account.security.JwtTokenProvider;
import com.ridelink.account.security.TokenProvider;
import com.ridelink.account.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private TokenProvider tokenProvider;
    private AccountServiceImpl accountService;

    private User samplePassenger;
    private User sampleSuspendedUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        tokenProvider = new JwtTokenProvider(
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
                86400000L
        );
        accountService = new AccountServiceImpl(userRepository, passwordEncoder, tokenProvider);

        samplePassenger = new User("Sarah Connor", "sarah@example.com", passwordEncoder.encode("Password123!"), Role.PASSENGER);
        samplePassenger.setId("user-uuid-1");
        samplePassenger.setAccountStatus(AccountStatus.ACTIVE);

        sampleSuspendedUser = new User("Suspended Driver", "suspended@example.com", passwordEncoder.encode("Password123!"), Role.DRIVER);
        sampleSuspendedUser.setId("user-uuid-2");
        sampleSuspendedUser.setAccountStatus(AccountStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Unit: Passenger registration succeeds and sets ROLE_PASSENGER")
    void testRegisterPassenger_Success() {
        RegisterRequest req = new RegisterRequest("Sarah Connor", "sarah@example.com", "Password123!");
        when(userRepository.existsByEmail("sarah@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(samplePassenger);

        UserProfileResponse res = accountService.registerPassenger(req);

        assertNotNull(res);
        assertEquals("user-uuid-1", res.getId());
        assertEquals("sarah@example.com", res.getEmail());
        assertEquals(Role.PASSENGER, res.getRole());
        assertEquals(AccountStatus.ACTIVE, res.getAccountStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Unit: Driver registration succeeds and sets ROLE_DRIVER")
    void testRegisterDriver_Success() {
        RegisterRequest req = new RegisterRequest("Alex Driver", "alex@example.com", "DriverPass123!");
        User driverUser = new User("Alex Driver", "alex@example.com", passwordEncoder.encode("DriverPass123!"), Role.DRIVER);
        driverUser.setId("driver-uuid-1");

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(driverUser);

        UserProfileResponse res = accountService.registerDriver(req);

        assertNotNull(res);
        assertEquals("driver-uuid-1", res.getId());
        assertEquals(Role.DRIVER, res.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Unit: Duplicate email registration throws DuplicateEmailException (409)")
    void testRegister_DuplicateEmail_ThrowsException() {
        RegisterRequest req = new RegisterRequest("Duplicate User", "sarah@example.com", "Password123!");
        when(userRepository.existsByEmail("sarah@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> accountService.registerPassenger(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Unit: Successful login returns token and user profile without passwordHash")
    void testLogin_Success() {
        LoginRequest req = new LoginRequest("sarah@example.com", "Password123!");
        when(userRepository.findByEmail("sarah@example.com")).thenReturn(Optional.of(samplePassenger));

        AuthResponse res = accountService.login(req);

        assertNotNull(res);
        assertNotNull(res.getToken());
        assertTrue(tokenProvider.validateToken(res.getToken()));
        assertEquals("Bearer", res.getTokenType());
        assertEquals("user-uuid-1", res.getUser().getId());
        assertEquals("sarah@example.com", res.getUser().getEmail());
    }

    @Test
    @DisplayName("Unit: Login with incorrect password throws InvalidCredentialsException (401)")
    void testLogin_IncorrectPassword_ThrowsException() {
        LoginRequest req = new LoginRequest("sarah@example.com", "WrongPassword!");
        when(userRepository.findByEmail("sarah@example.com")).thenReturn(Optional.of(samplePassenger));

        assertThrows(InvalidCredentialsException.class, () -> accountService.login(req));
    }

    @Test
    @DisplayName("Unit: Login for non-existent email throws InvalidCredentialsException (401)")
    void testLogin_UserNotFound_ThrowsException() {
        LoginRequest req = new LoginRequest("unknown@example.com", "Password123!");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> accountService.login(req));
    }

    @Test
    @DisplayName("Unit: Login on suspended account throws AccountSuspendedException (403)")
    void testLogin_SuspendedAccount_ThrowsException() {
        LoginRequest req = new LoginRequest("suspended@example.com", "Password123!");
        when(userRepository.findByEmail("suspended@example.com")).thenReturn(Optional.of(sampleSuspendedUser));

        assertThrows(AccountSuspendedException.class, () -> accountService.login(req));
    }

    @Test
    @DisplayName("Unit: Retrieve own profile returns user profile DTO")
    void testGetProfile_Success() {
        when(userRepository.findById("user-uuid-1")).thenReturn(Optional.of(samplePassenger));

        UserProfileResponse res = accountService.getProfile("user-uuid-1");

        assertNotNull(res);
        assertEquals("Sarah Connor", res.getName());
        assertEquals("sarah@example.com", res.getEmail());
    }

    @Test
    @DisplayName("Unit: Retrieve profile for unknown ID throws ResourceNotFoundException (404)")
    void testGetProfile_NotFound_ThrowsException() {
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getProfile("non-existent"));
    }

    @Test
    @DisplayName("Unit: Update own profile modifies name and updates timestamp")
    void testUpdateProfile_Success() {
        UpdateProfileRequest req = new UpdateProfileRequest("Sarah Connor Connor");
        when(userRepository.findById("user-uuid-1")).thenReturn(Optional.of(samplePassenger));
        when(userRepository.save(any(User.class))).thenReturn(samplePassenger);

        UserProfileResponse res = accountService.updateProfile("user-uuid-1", req);

        assertNotNull(res);
        assertEquals("Sarah Connor Connor", samplePassenger.getName());
        verify(userRepository).save(samplePassenger);
    }

    @Test
    @DisplayName("Unit: Admin updates account status to SUSPENDED")
    void testUpdateAccountStatus_Success() {
        UpdateStatusRequest req = new UpdateStatusRequest(AccountStatus.SUSPENDED);
        when(userRepository.findById("user-uuid-1")).thenReturn(Optional.of(samplePassenger));
        when(userRepository.save(any(User.class))).thenReturn(samplePassenger);

        UserProfileResponse res = accountService.updateAccountStatus("user-uuid-1", req);

        assertNotNull(res);
        assertEquals(AccountStatus.SUSPENDED, samplePassenger.getAccountStatus());
        verify(userRepository).save(samplePassenger);
    }
}
