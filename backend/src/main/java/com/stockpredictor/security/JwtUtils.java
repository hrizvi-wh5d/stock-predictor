package com.stockpredictor.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// JWT = JSON Web Token
// Think of it like a signed digital pass. When you log in, the server gives you a token.
// Every request after that, you show the token - server verifies it without hitting the database.
// Format: header.payload.signature  (three base64 parts separated by dots)
@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs; // 86400000ms = 24 hours

    // Create a signing key from our secret string
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Generate a JWT token for a logged-in user
    // The token contains: username, issued time, expiry time
    // It's signed with our secret so nobody can fake one
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)           // who this token is for
                .setIssuedAt(new Date())        // when it was created
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // expires in 24h
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // sign it
                .compact();
    }

    // Extract the username from a token
    // The server calls this on every request to know who's asking
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Check if a token is valid (not expired, not tampered with)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
