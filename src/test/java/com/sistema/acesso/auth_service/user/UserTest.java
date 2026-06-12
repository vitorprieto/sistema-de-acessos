package com.sistema.acesso.auth_service.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.sistema.acesso.auth_service.role.Role;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the User entity.
 *
 * <p>
 * Tests cover:
 * - Entity equality and hash code based on ID
 * - Account state (enabled, locked, can operate)
 * - Default values
 * - Basic constructor and field assignments
 * </p>
 */
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    // ===== Equality and HashCode Tests =====

    private User userWithId(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    @Test
    void equalWhenSameId() {
        assertThat(userWithId(1L)).isEqualTo(userWithId(1L));
        assertThat(userWithId(1L)).hasSameHashCodeAs(userWithId(1L));
    }

    @Test
    void notEqualWhenDifferentId() {
        assertThat(userWithId(1L)).isNotEqualTo(userWithId(2L));
    }

    @Test
    void sameInstanceIsAlwaysEqualEvenWithNullId() {
        User transientUser = new User();
        assertThat(transientUser).isEqualTo(transientUser);
    }

    @Test
    void twoTransientInstancesAreNotEqual() {
        assertThat(new User()).isNotEqualTo(new User());
    }

    @Test
    void notEqualToNullOrOtherType() {
        assertThat(userWithId(1L)).isNotEqualTo(null);
        assertThat(userWithId(1L)).isNotEqualTo(new Role("ROLE_ADMIN", "x"));
    }

    // ===== Default Values Tests =====

    @Test
    void defaultEnabledIsTrue() {
        User newUser = new User();
        assertThat(newUser.isEnabled()).isTrue();
    }

    @Test
    void defaultLockedIsFalse() {
        User newUser = new User();
        assertThat(newUser.isLocked()).isFalse();
    }

    @Test
    void defaultRolesIsEmptySet() {
        User newUser = new User();
        assertThat(newUser.getRoles()).isEmpty();
    }

    // ===== Account State Tests =====

    @Test
    void isAccountNonLockedReturnsTrueWhenNotLocked() {
        user.setLocked(false);
        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    void isAccountNonLockedReturnsFalseWhenLocked() {
        user.setLocked(true);
        assertThat(user.isAccountNonLocked()).isFalse();
    }

    @Test
    void isAccountEnabledReturnsTrueWhenEnabled() {
        user.setEnabled(true);
        assertThat(user.isAccountEnabled()).isTrue();
    }

    @Test
    void isAccountEnabledReturnsFalseWhenDisabled() {
        user.setEnabled(false);
        assertThat(user.isAccountEnabled()).isFalse();
    }

    @Test
    void canOperateReturnsTrueWhenEnabledAndNotLocked() {
        user.setEnabled(true);
        user.setLocked(false);
        assertThat(user.canOperate()).isTrue();
    }

    @Test
    void canOperateReturnsFalseWhenDisabled() {
        user.setEnabled(false);
        user.setLocked(false);
        assertThat(user.canOperate()).isFalse();
    }

    @Test
    void canOperateReturnsFalseWhenLocked() {
        user.setEnabled(true);
        user.setLocked(true);
        assertThat(user.canOperate()).isFalse();
    }

    @Test
    void canOperateReturnsFalseWhenDisabledAndLocked() {
        user.setEnabled(false);
        user.setLocked(true);
        assertThat(user.canOperate()).isFalse();
    }

    // ===== Constructor Tests =====

    @Test
    void allArgsConstructorAssignsAllFields() {
        Instant now = Instant.now();
        User newUser = new User(
            1L,
            "john_doe",
            "John Doe",
            "john@example.com",
            "$2a$10$hashedpassword",
            true,
            false,
            now,
            now,
            null);

        assertThat(newUser.getId()).isEqualTo(1L);
        assertThat(newUser.getUsername()).isEqualTo("john_doe");
        assertThat(newUser.getName()).isEqualTo("John Doe");
        assertThat(newUser.getEmail()).isEqualTo("john@example.com");
        assertThat(newUser.getPassword()).isEqualTo("$2a$10$hashedpassword");
        assertThat(newUser.isEnabled()).isTrue();
        assertThat(newUser.isLocked()).isFalse();
        assertThat(newUser.getCreatedAt()).isEqualTo(now);
        assertThat(newUser.getUpdatedAt()).isEqualTo(now);
    }

    // ===== Field Assignment Tests =====

    @Test
    void canSetAndGetUsername() {
        user.setUsername("john_doe");
        assertThat(user.getUsername()).isEqualTo("john_doe");
    }

    @Test
    void canSetAndGetName() {
        user.setName("John Doe");
        assertThat(user.getName()).isEqualTo("John Doe");
    }

    @Test
    void canSetAndGetEmail() {
        user.setEmail("john@example.com");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void canSetAndGetPassword() {
        user.setPassword("$2a$10$hashedpassword");
        assertThat(user.getPassword()).isEqualTo("$2a$10$hashedpassword");
    }

    @Test
    void canSetAndGetRoles() {
        Role role = new Role("ROLE_USER", "User role");
        user.getRoles().add(role);
        assertThat(user.getRoles()).hasSize(1).contains(role);
    }

    // ===== Additional Tests for Better Coverage =====

    @Test
    void noArgsConstructorCreatesEmptyUser() {
        User newUser = new User();
        assertThat(newUser.getId()).isNull();
        assertThat(newUser.getUsername()).isNull();
        assertThat(newUser.getName()).isNull();
        assertThat(newUser.getEmail()).isNull();
        assertThat(newUser.getPassword()).isNull();
        assertThat(newUser.isEnabled()).isTrue();
        assertThat(newUser.isLocked()).isFalse();
        assertThat(newUser.getRoles()).isNotNull().isEmpty();
    }

    @Test
    void notEqualWhenOtherIdIsNull() {
        User withId = userWithId(1L);
        User withoutId = new User();

        assertThat(withId).isNotEqualTo(withoutId);
    }

    @Test
    void canSetAndGetId() {
        user.setId(999L);
        assertThat(user.getId()).isEqualTo(999L);
    }

    @Test
    void canSetAndGetTimestamps() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-06-12T10:00:00Z");

        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void canRemoveRoleFromUser() {
        Role role1 = new Role("ROLE_ADMIN", "Admin");
        Role role2 = new Role("ROLE_USER", "User");
        user.getRoles().add(role1);
        user.getRoles().add(role2);

        user.getRoles().remove(role1);

        assertThat(user.getRoles()).hasSize(1).contains(role2);
    }

    @Test
    void canHaveMultipleRoles() {
        user.getRoles().add(new Role("ROLE_ADMIN", "Admin"));
        user.getRoles().add(new Role("ROLE_MANAGER", "Manager"));
        user.getRoles().add(new Role("ROLE_USER", "User"));

        assertThat(user.getRoles()).hasSize(3);
    }

    @Test
    void blockedUserCannotOperate() {
        user.setEnabled(true);
        user.setLocked(true);

        assertThat(user.isAccountEnabled()).isTrue();
        assertThat(user.isAccountNonLocked()).isFalse();
        assertThat(user.canOperate()).isFalse();
    }

    @Test
    void disabledUserCannotOperate() {
        user.setEnabled(false);
        user.setLocked(false);

        assertThat(user.isAccountEnabled()).isFalse();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.canOperate()).isFalse();
    }

    @Test
    void multipleUsersAreIndependent() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setLocked(true);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setLocked(false);

        user1.setLocked(false);

        assertThat(user1.isAccountNonLocked()).isTrue();
        assertThat(user2.isAccountNonLocked()).isTrue();
    }

    @Test
    void userWithFullDataIsConsistent() {
        Role role = new Role("ROLE_ADMIN", "Admin");
        role.setId(1L);

        user.setId(5L);
        user.setUsername("test_user");
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("$2a$10$hash");
        user.setEnabled(true);
        user.setLocked(false);
        user.getRoles().add(role);

        assertThat(user.getId()).isEqualTo(5L);
        assertThat(user.getUsername()).isEqualTo("test_user");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.canOperate()).isTrue();
        assertThat(user.getRoles()).hasSize(1);
    }
}
