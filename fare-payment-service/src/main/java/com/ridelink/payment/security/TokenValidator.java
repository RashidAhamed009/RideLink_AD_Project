package com.ridelink.payment.security;

import java.util.List;

public interface TokenValidator {
    boolean validateToken(String token);
    String getUserIdFromToken(String token);
    String getEmailFromToken(String token);
    List<String> getRolesFromToken(String token);
}
