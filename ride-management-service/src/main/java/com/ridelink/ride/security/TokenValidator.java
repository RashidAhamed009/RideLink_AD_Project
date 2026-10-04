package com.ridelink.ride.security;

import java.util.List;

public interface TokenValidator {
    boolean validateToken(String token);
    String extractUserId(String token);
    String extractEmail(String token);
    List<String> extractRoles(String token);
}
