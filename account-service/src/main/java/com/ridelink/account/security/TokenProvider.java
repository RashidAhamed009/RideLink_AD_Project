package com.ridelink.account.security;

public interface TokenProvider {
    String generateToken(UserPrincipal userPrincipal);
    String getUserIdFromToken(String token);
    String getEmailFromToken(String token);
    boolean validateToken(String authToken);
    long getExpirationMs();
}
