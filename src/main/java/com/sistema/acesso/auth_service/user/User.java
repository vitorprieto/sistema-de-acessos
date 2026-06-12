package com.sistema.acesso.auth_service.user;

import com.sistema.acesso.auth_service.role.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * User entity representing an authenticable user in the system.
 *
 * <p>
 * This entity implements the User concept from the RBAC model defined in ADR-001.
 * Users are associated with Roles through a many-to-many relationship, and through
 * roles they inherit permissions.
 * </p>
 *
 * <p>
 * Key responsibilities:
 * - Authentication (via email and password)
 * - Identification
 * - Association with roles (permissions granted through roles)
 * </p>
 *
 * <p>
 * Security considerations:
 * - Passwords MUST be stored as BCrypt hash (RN-004)
 * - Email MUST be unique (RN-002)
 * - Blocked users cannot authenticate (RN-003)
 * </p>
 *
 * @see Role
 * @see com.sistema.acesso.auth_service.permission.Permission
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "roles")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username for login operations.
     * Must be unique and not blank.
     */
    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Full name of the user.
     * Used for identification and audit purposes.
     */
    @NotBlank(message = "Name must not be blank")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Email address of the user.
     * Must be unique and valid (RN-002).
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Password hash (BCrypt).
     * NEVER store plaintext passwords.
     * Always use BCrypt for hashing (RN-004).
     */
    @NotBlank(message = "Password must not be blank")
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * Indicates whether the user account is enabled.
     * Disabled users cannot authenticate.
     * Default: true
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Indicates whether the user account is locked (blocked).
     * Locked users cannot authenticate even if enabled (RN-003).
     * Default: false (not locked)
     */
    @Column(nullable = false)
    private boolean locked = false;

    /**
     * Timestamp when the user was created.
     * Automatically set on entity creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when the user was last updated.
     * Automatically updated on entity modification.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Many-to-many relationship with Roles.
     * Users inherit permissions from their associated roles.
     * This relationship should always have at least one role (RN-001).
     *
     * @see Role
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /**
     * Checks if the user account is active for authentication.
     * An account is active if it's enabled AND not locked.
     *
     * @return true if the account can authenticate, false otherwise
     */
    public boolean isAccountNonLocked() {
        return !locked;
    }

    /**
     * Checks if the user account is non-disabled.
     *
     * @return true if the account is enabled, false otherwise
     */
    public boolean isAccountEnabled() {
        return enabled;
    }

    /**
     * Checks if the user can perform operations requiring an active account.
     * A user can perform operations if enabled and not locked.
     *
     * @return true if the user can operate, false otherwise
     */
    public boolean canOperate() {
        return enabled && !locked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User other) || id == null) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
