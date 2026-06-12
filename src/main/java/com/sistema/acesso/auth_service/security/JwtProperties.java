package com.sistema.acesso.auth_service.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed binding for the {@code app.jwt.*} settings (see application.properties).
 *
 * @param secret                 Base64-encoded signing key (>= 256 bits for HS256).
 * @param accessTokenExpiration  access-token lifetime in milliseconds (ADR-002: 15 min).
 * @param refreshTokenExpiration refresh-token lifetime in milliseconds (ADR-002: 7 days).
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secret,
    long accessTokenExpiration,
    long refreshTokenExpiration) {
}
