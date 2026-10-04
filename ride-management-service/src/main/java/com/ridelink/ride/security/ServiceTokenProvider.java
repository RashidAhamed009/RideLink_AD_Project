package com.ridelink.ride.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class ServiceTokenProvider {

    private final SecretKey key;

    public ServiceTokenProvider(@Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getAuthorizationHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null && attrs.getRequest() != null) {
            HttpServletRequest request = attrs.getRequest();
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                return header;
            }
        }

        // Fallback: Generate an internal service token with administrative privileges
        String token = Jwts.builder()
                .subject("system-ride-service")
                .claim("email", "system@ridelink.internal")
                .claim("roles", List.of("ADMIN", "DRIVER", "PASSENGER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 600_000)) // 10 minutes
                .signWith(key)
                .compact();

        return "Bearer " + token;
    }
}
