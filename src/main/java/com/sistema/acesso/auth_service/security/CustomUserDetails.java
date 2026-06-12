package com.sistema.acesso.auth_service.security;

import com.sistema.acesso.auth_service.user.User;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adapter between the {@link User} entity and Spring Security.
 *
 * <p>
 * This implementation bridges the application's User entity with Spring Security
 * by converting User roles and permissions into {@link GrantedAuthority} objects.
 * </p>
 *
 * <p>
 * <strong>Authority Model (per ADR-001 RBAC):</strong>
 * </p>
 *
 * <ul>
 * <li>Each role contributes its own name as an authority (e.g., {@code ROLE_ADMIN},
 * {@code ROLE_MANAGER}, {@code ROLE_USER}). These names are pre-fixed with
 * {@code ROLE_} in the database, so Spring Security's {@code hasRole('ADMIN')}
 * directly works.</li>
 * <li>Each permission associated with a role becomes an authority (e.g.,
 * {@code USER_CREATE}, {@code USER_READ}). This allows {@code hasAuthority('USER_CREATE')}
 * checks.</li>
 * <li>Permissions are only granted through roles; direct user-permission
 * associations are not supported.</li>
 * </ul>
 *
 * <p>
 * <strong>Thread Safety & Transaction Handling:</strong>
 * </p>
 *
 * <p>
 * The authority set is materialized at construction time via {@link #from(User)},
 * which must be called inside an active transaction. The UserRepository's
 * {@code @EntityGraph} eagerly fetches roles and their permissions, ensuring
 * lazy loading does not occur after the transaction closes (important when
 * {@code spring.jpa.open-in-view} is {@code false}).
 * </p>
 *
 * @see User
 * @see CustomUserDetailsService
 * @see com.sistema.acesso.auth_service.role.Role
 * @see com.sistema.acesso.auth_service.permission.Permission
 * @since 1.0
 */
@Slf4j
@Getter
public class CustomUserDetails implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean locked;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Private constructor. Use factory method {@link #from(User)} instead.
     *
     * @param id the user ID
     * @param username the username for authentication
     * @param email the user's email address
     * @param password the user's password hash (BCrypt)
     * @param enabled whether the user account is enabled
     * @param locked whether the user account is locked (blocked)
     * @param authorities the collection of granted authorities (roles + permissions)
     */
    private CustomUserDetails(
        Long id,
        String username,
        String email,
        String password,
        boolean enabled,
        boolean locked,
        Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.locked = locked;
        this.authorities = authorities;
    }

    /**
     * Factory method to create a CustomUserDetails from a User entity.
     *
     * <p>
     * Converts the user's roles and permissions into a set of Spring Security
     * authorities. This method must be called within an active transaction context
     * to safely access the lazy-loaded {@code roles} and {@code permissions}
     * collections.
     * </p>
     *
     * <p>
     * Authority extraction:
     * </p>
     *
     * <ol>
     * <li>For each role, add the role name as an authority (e.g., {@code ROLE_ADMIN})</li>
     * <li>For each permission in that role, add the permission name as an authority
     * (e.g., {@code USER_CREATE})</li>
     * </ol>
     *
     * @param user the User entity (must have roles and permissions already loaded)
     * @return a CustomUserDetails instance with all authorities populated
     * @throws NullPointerException if user is null or missing required fields
     */
    public static CustomUserDetails from(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role names as authorities (e.g., ROLE_ADMIN)
        user.getRoles().forEach(role ->
            authorities.add(new SimpleGrantedAuthority(role.getName())));

        // Add all permissions from all roles as authorities (e.g., USER_CREATE)
        user.getRoles().forEach(role ->
            role.getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getName()))));

        return new CustomUserDetails(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPassword(),
            user.isEnabled(),
            user.isLocked(),
            authorities);
    }

    /**
     * Returns the authorities (roles + permissions) granted to the user.
     *
     * @return the collection of GrantedAuthority objects
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the password hash for authentication.
     *
     * @return the user's password hash (BCrypt)
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username for authentication.
     *
     * @return the username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates if the user account is not expired.
     *
     * <p>
     * Currently always returns {@code true}. If account expiration is
     * required in the future, add an {@code expiresAt} field to User.
     * </p>
     *
     * @return true (account is never expired in current implementation)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates if the user account is not locked (not blocked).
     *
     * <p>
     * A locked account cannot authenticate, even if enabled. This respects
     * the RN-003 rule: "Usuários bloqueados não podem realizar login."
     * </p>
     *
     * @return true if the account is not locked, false if locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    /**
     * Indicates if the user credentials (password) are not expired.
     *
     * <p>
     * Currently always returns {@code true}. If password expiration is
     * required in the future, add a {@code passwordExpiresAt} field to User.
     * </p>
     *
     * @return true (credentials are never expired in current implementation)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates if the user account is enabled.
     *
     * <p>
     * A disabled account cannot authenticate. This is distinct from being
     * locked; a disabled account is administratively disabled.
     * </p>
     *
     * @return true if the account is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks if the user has a specific role.
     *
     * <p>
     * Example: {@code hasRole("ADMIN")} checks if the user has the
     * {@code ROLE_ADMIN} authority.
     * </p>
     *
     * @param role the role name (without the {@code ROLE_} prefix)
     * @return true if the user has this role, false otherwise
     */
    public boolean hasRole(String role) {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(auth -> auth.equals(ROLE_PREFIX + role));
    }

    /**
     * Checks if the user has a specific permission.
     *
     * <p>
     * Example: {@code hasPermission("USER_CREATE")} checks if the user has
     * this permission through any of their roles.
     * </p>
     *
     * @param permission the permission name (e.g., {@code USER_CREATE})
     * @return true if the user has this permission, false otherwise
     */
    public boolean hasPermission(String permission) {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(auth -> auth.equals(permission));
    }

    /**
     * Returns only the permission authorities (excludes role authorities).
     *
     * @return a set of permission authorities
     */
    public Set<String> getPermissions() {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> !auth.startsWith(ROLE_PREFIX))
            .collect(Collectors.toSet());
    }

    /**
     * Returns only the role authorities (excludes permission authorities).
     *
     * @return a set of role authorities
     */
    public Set<String> getRoles() {
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith(ROLE_PREFIX))
            .collect(Collectors.toSet());
    }
}
