package com.example.groceries_jwt_project.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

/**
 * JwtUtil:
 *  - Handles creation and validation of JWT tokens.
 *  - Uses HS256 (HMAC with SHA-256) for signing — same secret used for verify.
 *
 * Teaching points:
 *  - JWTs are SIGNED, not ENCRYPTED (never store passwords in them).
 *  - The secret key must be ≥ 32 bytes for HS256.
 *  - The same key is reused for signing and verifying.
 */
@Component
public class JwtUtil {

    // Secret used to sign and verify tokens (from application.properties)
    @Value("${app.jwt.secret}")
    private String secret;

    // Token lifetime (milliseconds)
    @Value("${app.jwt.expiration-ms}")
    private long expiration;

    // Cached SecretKey so we don’t rebuild it each time
    private SecretKey signingKey;

    /**
     * Initialize the signing key once at startup.
     *  - If the secret looks like Base64 → decode it.
     *  - Otherwise → use it as plain text.
     */
    @PostConstruct
    void initKey() {
        byte[] keyBytes;
        try {
            // Try to decode as Base64 (some people store secrets that way)
            keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
        } catch (DecodingException e) {
            // Not Base64? Treat it as normal UTF-8 text
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        // JJWT enforces ≥ 32 bytes or it throws IllegalArgumentException
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a signed JWT for a logged-in user.
     *
     * Flow:
     *  1) Take username from Authentication (the subject).
     *  2) Set issuedAt and expiration.
     *  3) Sign with our cached key.
     *  4) Return compact string (HEADER.PAYLOAD.SIGNATURE).
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();     // "sub" claim
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)                   // uses HS256 automatically
                .compact();
    }

    /**
     * Verify and extract username ("sub") from the token.
     *  - verifyWith(signingKey) ensures integrity and authenticity.
     *  - Throws if token is expired or tampered.
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** Convenience helper — when new tokens issued now will expire (epoch ms). */
    public long getExpirationInstantMs() {
        return System.currentTimeMillis() + expiration;
    }
}

//Base64 is a way to represent binary data (like bytes) in a 
//text format — made up only of letters, numbers, + and / — 
//so it can be safely stored in files, databases, or 
//configuration properties.
//
//It’s called Base64 because:
//It uses 64 characters to represent any data.
//Each Base64 character represents 6 bits (since 2⁶ = 64).
//
//For example:
//Raw binary key (bytes): 01101100 01101001 01101110 01110101 01111000
//
//Base64-encoded version: "bGludXg="