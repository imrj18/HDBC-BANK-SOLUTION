package com.ritik.customer_microservice.serviceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtServiceImpl {

    private final SecretKey userKey;

    public JwtServiceImpl(@Value("${jwt.secret}") String userSecret) {
        this.userKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(userSecret));
    }

    public String generateUserToken(String email) {
        return Jwts.builder()
                .subject(email.toLowerCase())
                .claim("type", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000)) // 10 min
                .signWith(userKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(userKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean validateUserToken(String token, UserDetails userDetails) {
        Claims claims = extractClaims(token);
        return userDetails.getUsername().equals(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    public Date extractExpiryTime(String token) {
        return extractClaims(token).getExpiration();
    }
}
