package com.quickbee.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // IMPORTANT: Make this a long, random, secret string and keep it safe
    // You can generate one at a site like: https://www.allkeysgenerator.com/
    // This is just an example, USE YOUR OWN!
    @Value("${jwt.secret}")
    private final String SECRET_KEY = "your-very-long-and-super-secret-key-for-jwt-that-is-at-least-256-bits";

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Token validity (e.g., 10 hours)
    private static final long JWT_TOKEN_VALIDITY = 10 * 60 * 60 * 1000; // 10 hours in ms

    // 1. Generate token from UserDetails
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // You can add more claims here if needed (e.g., roles)
        // claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername()); // username is email
    }

    // 2. Create the token
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject) // The user's email
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(key)
                .compact();
    }

    // 3. Validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // 4. Extract username (email) from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 5. Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 6. Check if the token has expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 7. Helper function to extract any claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 8. Helper function to extract all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}