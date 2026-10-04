package com.ridelink.payment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtTokenValidator implements TokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidator.class);

    private final String jwtSecret;

    public JwtTokenValidator(
            @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String getUserIdFromToken(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    public String getEmailFromToken(String token) {
        return getClaims(token).get("email", String.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            return (List<String>) rolesObj;
        }
        String singleRole = claims.get("role", String.class);
        if (singleRole != null) {
            return List.of(singleRole);
        }
        return new ArrayList<>();
    }
}
