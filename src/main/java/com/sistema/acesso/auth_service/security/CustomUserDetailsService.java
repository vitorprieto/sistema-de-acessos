package com.sistema.acesso.auth_service.security;

import com.sistema.acesso.auth_service.user.User;
import com.sistema.acesso.auth_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security's UserDetailsService implementation.
 *
 * <p>
 * Loads user authentication details from the database by username, email, or ID.
 * Converts the {@link User} entity into a {@link CustomUserDetails} object suitable
 * for Spring Security authentication and authorization.
 * </p>
 *
 * <p>
 * <strong>Transaction & Lazy Loading:</strong>
 * </p>
 *
 * <p>
 * All load methods are wrapped in read-only transactions. The {@link UserRepository}
 * uses {@code @EntityGraph} to eagerly fetch roles and their permissions, ensuring
 * lazy loading is safe even when {@code spring.jpa.open-in-view} is false. This
 * prevents "lazy loading outside of transaction" errors and improves performance by
 * reducing database round-trips.
 * </p>
 *
 * <p>
 * <strong>Authority Conversion:</strong>
 * </p>
 *
 * <p>
 * The {@link #loadUserByUsername(String)} method is the entry point used by Spring
 * Security during authentication. It retrieves the user and converts them to
 * {@link CustomUserDetails}, which materializes all roles and permissions as
 * Spring Security authorities within the transaction context.
 * </p>
 *
 * <p>
 * <strong>Error Handling:</strong>
 * </p>
 *
 * <p>
 * If a user is not found, a {@link UsernameNotFoundException} is thrown. This is
 * the standard Spring Security exception for authentication failures. It does not
 * reveal whether a user exists (for security reasons), helping prevent user enumeration.
 * </p>
 *
 * @see CustomUserDetails
 * @see User
 * @see UserRepository
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by username for authentication.
     *
     * <p>
     * This is the primary entry point for Spring Security authentication.
     * It retrieves the user by username and converts them to {@link CustomUserDetails}.
     * </p>
     *
     * <p>
     * The repository query uses {@code @EntityGraph} to eagerly load:
     * </p>
     *
     * <ul>
     * <li>User roles (e.g., ROLE_ADMIN, ROLE_USER)</li>
     * <li>Permissions associated with each role (e.g., USER_CREATE, USER_READ)</li>
     * </ul>
     *
     * <p>
     * All data fetching occurs within the transaction, so {@link CustomUserDetails#from(User)}
     * can safely access lazy collections.
     * </p>
     *
     * @param username the username to authenticate
     * @return a UserDetails object containing authorities (roles + permissions)
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);

        return userRepository
            .findByUsername(username)
            .map(user -> {
                log.debug("User found: {}, converting to CustomUserDetails", username);
                return CustomUserDetails.from(user);
            })
            .orElseThrow(() -> {
                log.warn("User not found for username: {}", username);
                return new UsernameNotFoundException(
                    "Usuário não encontrado: " + username);
            });
    }

    /**
     * Loads user details by email for authentication or user lookup.
     *
     * <p>
     * Some authentication mechanisms (e.g., OAuth2, social login) may use
     * email instead of username. This method provides that flexibility.
     * </p>
     *
     * @param email the email address to look up
     * @return a UserDetails object if found
     * @throws UsernameNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        log.debug("Loading user details for email: {}", email);

        return userRepository
            .findByEmail(email)
            .map(user -> {
                log.debug("User found for email: {}, converting to CustomUserDetails", email);
                return CustomUserDetails.from(user);
            })
            .orElseThrow(() -> {
                log.warn("User not found for email: {}", email);
                return new UsernameNotFoundException(
                    "Usuário não encontrado com o email: " + email);
            });
    }

    /**
     * Loads user details by user ID for internal operations.
     *
     * <p>
     * This method is useful for JWT filters and internal security checks that
     * already have the user ID extracted from (for example) a JWT token.
     * </p>
     *
     * @param userId the user's primary key
     * @return a UserDetails object if found
     * @throws UsernameNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("Loading user details for user ID: {}", userId);

        return userRepository
            .findWithRolesById(userId)
            .map(user -> {
                log.debug("User found for ID: {}, converting to CustomUserDetails", userId);
                return CustomUserDetails.from(user);
            })
            .orElseThrow(() -> {
                log.warn("User not found for ID: {}", userId);
                return new UsernameNotFoundException(
                    "Usuário não encontrado com o ID: " + userId);
            });
    }

    /**
     * Loads a full User entity by username (returns the JPA entity, not UserDetails).
     *
     * <p>
     * This method is for internal use when the full User entity (with all
     * fields) is needed, rather than just the security details.
     * </p>
     *
     * <p>
     * The returned User has roles and permissions eagerly loaded via EntityGraph.
     * </p>
     *
     * @param username the username to look up
     * @return the User entity if found
     * @throws UsernameNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public User loadUserEntityByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user entity for username: {}", username);

        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> {
                log.warn("User entity not found for username: {}", username);
                return new UsernameNotFoundException(
                    "Usuário não encontrado: " + username);
            });
    }
}
