package com.sistema.acesso.auth_service.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for User entity.
 *
 * <p>
 * Provides database access for User operations with optimized queries
 * using EntityGraph to load roles and permissions eagerly when needed.
 * </p>
 *
 * <p>
 * Key methods:
 * - findByUsername: For user authentication by username
 * - findByEmail: For user authentication by email
 * - findWithRolesById: For loading user with all roles and permissions
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username with eager loading of roles and permissions.
     * Used primarily for authentication.
     *
     * @param username the username to search for
     * @return Optional containing the user if found, otherwise empty
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email with eager loading of roles and permissions.
     * Used for email-based authentication and user lookup.
     *
     * @param email the email address to search for
     * @return Optional containing the user if found, otherwise empty
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by id with eager loading of roles and permissions.
     * Used by JWT filters and authentication filters to load complete user context.
     *
     * @param id the user id to search for
     * @return Optional containing the user if found, otherwise empty
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findWithRolesById(Long id);

    /**
     * Checks if a user exists by username.
     *
     * @param username the username to check
     * @return true if a user with this username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user exists by email.
     *
     * @param email the email to check
     * @return true if a user with this email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
