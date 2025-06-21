package com.backend.wanderverse_server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    // JWT Expiration is in milliseconds
    private int jwtExpirationMs;

    private SecretKey key;

    // Initialize key only after class is instantiated and jwtSecret is injected from application.properties
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Generate JWT Token from username
//    public String generateToken(String username) {
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(new Date().getTime() + jwtExpriationMs))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }

    // Generate JWT Token from email
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Get username from JWT Token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Get email from JWT Token
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate JWT Token
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT Signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT Token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT Claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
