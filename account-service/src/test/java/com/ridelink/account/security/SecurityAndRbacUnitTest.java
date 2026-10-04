package com.ridelink.account.security;

import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAndRbacUnitTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("Security: Generates valid signed JWT and parses claims accurately")
    void testGenerateAndValidateToken_Success() {
        User user = new User("Sarah Connor", "sarah@example.com", "hash", Role.PASSENGER);
        user.setId("user-12345");
        UserPrincipal principal = UserPrincipal.create(user);

        String token = jwtTokenProvider.generateToken(principal);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("user-12345", jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Security: Tampered or invalid JWT fails validation")
    void testValidateToken_InvalidSignature_ReturnsFalse() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalidpayload.invalidSignature";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Security: Expired JWT fails validation")
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Token provider with 0 ms expiration
        JwtTokenProvider expiredProvider = new JwtTokenProvider(secret, -1000);
        User user = new User("Sarah Connor", "sarah@example.com", "hash", Role.PASSENGER);
        user.setId("user-12345");
        String expiredToken = expiredProvider.generateToken(UserPrincipal.create(user));

        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    @DisplayName("Security: UserPrincipal verifies role authorities mapping correctly")
    void testUserPrincipalAuthorities() {
        User user = new User("Admin", "admin@ridelink.com", "hash", Role.ADMIN);
        UserPrincipal principal = UserPrincipal.create(user);

        assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(principal.isEnabled());
        assertTrue(principal.isAccountNonLocked());
    }

    @Test
    @DisplayName("Security: Suspended user is locked in UserPrincipal")
    void testUserPrincipalSuspended() {
        User user = new User("Suspended", "bad@ridelink.com", "hash", Role.DRIVER);
        user.setAccountStatus(AccountStatus.SUSPENDED);
        UserPrincipal principal = UserPrincipal.create(user);

        assertFalse(principal.isAccountNonLocked());
        assertFalse(principal.isEnabled());
    }
}
