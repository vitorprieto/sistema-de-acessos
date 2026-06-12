package com.sistema.acesso.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Issues and validates stateless JWT access tokens (ADR-002).
 *
 * <p>Token payload mirrors the ADR: {@code sub} = user id, plus {@code email}
 * and {@code roles} claims, signed with HMAC-SHA using the configured secret.
 * Refresh-token persistence/revocation is out of scope here (FEATURE 2.3).
 */
@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;

    public JwtService(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.accessTokenExpirationMs = properties.accessTokenExpiration();
    }

    /** Generates a signed access token for the authenticated user. */
    public String generateAccessToken(CustomUserDetails user) {
        Instant now = Instant.now();
        List<String> roles = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .toList();

        return Jwts.builder()
            .subject(String.valueOf(user.getId()))
            .claim(CLAIM_EMAIL, user.getEmail())
            .claim(CLAIM_ROLES, roles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
            .signWith(signingKey)
            .compact();
    }

    public Long extractUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        return parseClaims(token).get(CLAIM_EMAIL, String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return parseClaims(token).get(CLAIM_ROLES, List.class);
    }

    public Instant extractExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    /** True when the token is well-formed, correctly signed, unexpired and belongs to this user. */
    public boolean isTokenValid(String token, CustomUserDetails user) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject().equals(String.valueOf(user.getId()))
                && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    /**
     * Verifies the signature and returns the claims.
     *
     * @throws JwtException if the token is malformed, tampered with or expired.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
