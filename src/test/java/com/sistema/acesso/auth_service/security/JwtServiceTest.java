package com.sistema.acesso.auth_service.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.sistema.acesso.auth_service.permission.Permission;
import com.sistema.acesso.auth_service.role.Role;
import com.sistema.acesso.auth_service.user.User;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    // 40-byte raw secret -> Base64, comfortably above the 256-bit HS256 minimum.
    private static final String SECRET = Base64.getEncoder()
        .encodeToString("0123456789012345678901234567890123456789".getBytes(StandardCharsets.UTF_8));
    private static final long ACCESS_TTL = 900_000L;
    private static final long REFRESH_TTL = 604_800_000L;

    private JwtService jwtService;
    private CustomUserDetails user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(SECRET, ACCESS_TTL, REFRESH_TTL));
        user = CustomUserDetails.from(buildUser(42L, "admin", "admin@sistema.local"));
    }

    private User buildUser(Long id, String username, String email) {
        Permission read = new Permission("USER_READ", "Visualizar usuários");
        Role admin = new Role("ROLE_ADMIN", "Administrador");
        admin.setPermissions(Set.of(read));
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("hash");
        u.setEnabled(true);
        u.setRoles(Set.of(admin));
        return u;
    }

    @Test
    void generatesTokenWithAdrClaims() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@sistema.local");
        // Only ROLE_* authorities go into the roles claim; permissions are excluded.
        assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_ADMIN");
        assertThat(jwtService.extractExpiration(token)).isAfter(Instant.now());
    }

    @Test
    void validTokenIsValidForSameUser() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
        assertThat(jwtService.isExpired(token)).isFalse();
    }

    @Test
    void tokenIsInvalidForADifferentUser() {
        String token = jwtService.generateAccessToken(user);
        CustomUserDetails other = CustomUserDetails.from(buildUser(99L, "other", "other@x"));

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void malformedTokenIsNotValid() {
        assertThat(jwtService.isTokenValid("not-a-real-token", user)).isFalse();
    }

    @Test
    void tamperedTokenIsNotValid() {
        String token = jwtService.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isTokenValid(tampered, user)).isFalse();
    }

    @Test
    void expiredTokenIsNotValid() {
        JwtService shortLived =
            new JwtService(new JwtProperties(SECRET, -1_000L, REFRESH_TTL));
        String expired = shortLived.generateAccessToken(user);

        assertThat(shortLived.isTokenValid(expired, user)).isFalse();
    }
}
