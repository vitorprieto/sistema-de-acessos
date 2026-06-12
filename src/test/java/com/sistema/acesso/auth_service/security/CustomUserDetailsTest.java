package com.sistema.acesso.auth_service.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.sistema.acesso.auth_service.permission.Permission;
import com.sistema.acesso.auth_service.role.Role;
import com.sistema.acesso.auth_service.user.User;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Unit tests for CustomUserDetails.
 *
 * <p>
 * Tests the conversion of User entity (with roles and permissions) into
 * Spring Security's UserDetails interface, including authorities materialization
 * and account state flags.
 * </p>
 */
class CustomUserDetailsTest {

    private User baseUser;

    @BeforeEach
    void setUp() {
        baseUser = new User();
        baseUser.setId(1L);
        baseUser.setUsername("john_doe");
        baseUser.setName("John Doe");
        baseUser.setEmail("john@example.com");
        baseUser.setPassword("$2a$10$bcrypthashedpassword");
        baseUser.setEnabled(true);
        baseUser.setLocked(false);
        baseUser.setCreatedAt(Instant.now());
        baseUser.setUpdatedAt(Instant.now());
        baseUser.setRoles(Set.of());
    }

    /**
     * Helper to create a role with permissions.
     */
    private Role createRoleWithPermissions(String roleName, String... permissionNames) {
        Role role = new Role(roleName, "Test role for " + roleName);
        Set<Permission> permissions = new java.util.HashSet<>();
        for (String permName : permissionNames) {
            permissions.add(new Permission(permName, "Permission: " + permName));
        }
        role.setPermissions(permissions);
        return role;
    }

    // ===== Scalar Field Mapping Tests =====

    @Test
    void mapsScalarFieldsFromEntity() {
        Role admin = createRoleWithPermissions("ROLE_ADMIN");
        baseUser.setRoles(Set.of(admin));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(details.getId()).isEqualTo(1L);
        assertThat(details.getUsername()).isEqualTo("john_doe");
        assertThat(details.getEmail()).isEqualTo("john@example.com");
        assertThat(details.getPassword()).isEqualTo("$2a$10$bcrypthashedpassword");
        assertThat(details.isEnabled()).isTrue();
    }

    // ===== Authority Conversion Tests =====

    @Test
    void exposesRolesAndPermissionsAsAuthorities() {
        Role admin = createRoleWithPermissions("ROLE_ADMIN", "USER_READ", "USER_CREATE");
        baseUser.setRoles(Set.of(admin));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(AuthorityUtils.authorityListToSet(details.getAuthorities()))
            .containsExactlyInAnyOrder("ROLE_ADMIN", "USER_READ", "USER_CREATE");
    }

    @Test
    void exposesMultipleRolesAndTheirPermissions() {
        Role admin = createRoleWithPermissions("ROLE_ADMIN", "USER_DELETE");
        Role manager = createRoleWithPermissions("ROLE_MANAGER", "USER_READ", "USER_UPDATE");

        baseUser.setRoles(Set.of(admin, manager));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(AuthorityUtils.authorityListToSet(details.getAuthorities()))
            .containsExactlyInAnyOrder(
                "ROLE_ADMIN", "USER_DELETE",
                "ROLE_MANAGER", "USER_READ", "USER_UPDATE");
    }

    @Test
    void userWithNoRolesHasNoAuthorities() {
        baseUser.setRoles(Set.of());

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void roleWithoutPermissionsExposesOnlyRoleName() {
        Role user = new Role("ROLE_USER", "Standard user");
        user.setPermissions(Set.of());
        baseUser.setRoles(Set.of(user));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(AuthorityUtils.authorityListToSet(details.getAuthorities()))
            .containsExactly("ROLE_USER");
    }

    // ===== Account State Flags Tests =====

    @Test
    void accountEnabledReflectsEntity() {
        baseUser.setEnabled(true);
        CustomUserDetails enabledDetails = CustomUserDetails.from(baseUser);

        baseUser.setEnabled(false);
        CustomUserDetails disabledDetails = CustomUserDetails.from(baseUser);

        assertThat(enabledDetails.isEnabled()).isTrue();
        assertThat(disabledDetails.isEnabled()).isFalse();
    }

    @Test
    void accountNonLockedReflectsLockedState() {
        baseUser.setLocked(false);
        CustomUserDetails unlockedDetails = CustomUserDetails.from(baseUser);

        baseUser.setLocked(true);
        CustomUserDetails lockedDetails = CustomUserDetails.from(baseUser);

        assertThat(unlockedDetails.isAccountNonLocked()).isTrue();
        assertThat(lockedDetails.isAccountNonLocked()).isFalse();
    }

    @Test
    void accountNonExpiredIsAlwaysTrue() {
        CustomUserDetails details = CustomUserDetails.from(baseUser);
        assertThat(details.isAccountNonExpired()).isTrue();
    }

    @Test
    void credentialsNonExpiredIsAlwaysTrue() {
        CustomUserDetails details = CustomUserDetails.from(baseUser);
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }

    // ===== Helper Methods Tests =====

    @Test
    void hasRoleDetectsRolePresentAndAbsent() {
        Role admin = createRoleWithPermissions("ROLE_ADMIN");
        Role user = createRoleWithPermissions("ROLE_USER");
        baseUser.setRoles(Set.of(admin, user));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(details.hasRole("ADMIN")).isTrue();
        assertThat(details.hasRole("USER")).isTrue();
        assertThat(details.hasRole("MANAGER")).isFalse();
    }

    @Test
    void hasPermissionDetectsPermissionPresentAndAbsent() {
        Role admin = createRoleWithPermissions("ROLE_ADMIN", "USER_CREATE", "USER_DELETE");
        baseUser.setRoles(Set.of(admin));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(details.hasPermission("USER_CREATE")).isTrue();
        assertThat(details.hasPermission("USER_DELETE")).isTrue();
        assertThat(details.hasPermission("USER_READ")).isFalse();
    }

    @Test
    void getPermissionsReturnsOnlyPermissions() {
        Role admin = createRoleWithPermissions("ROLE_ADMIN", "USER_CREATE");
        Role user = createRoleWithPermissions("ROLE_USER", "PROFILE_READ");
        baseUser.setRoles(Set.of(admin, user));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(details.getPermissions())
            .containsExactlyInAnyOrder("USER_CREATE", "PROFILE_READ");
    }

    @Test
    void getRolesReturnsOnlyRoles() {
        Role admin = createRoleWithPermissions("ROLE_ADMIN", "USER_CREATE");
        Role user = createRoleWithPermissions("ROLE_USER", "PROFILE_READ");
        baseUser.setRoles(Set.of(admin, user));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(details.getRoles())
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void getPermissionsEmptyWhenNoRoles() {
        baseUser.setRoles(Set.of());

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(details.getPermissions()).isEmpty();
    }

    @Test
    void getRolesEmptyWhenNoRoles() {
        baseUser.setRoles(Set.of());

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(details.getRoles()).isEmpty();
    }

    // ===== Integration Tests =====

    @Test
    void canAuthenticateBasedOnAllFlags() {
        Role admin = createRoleWithPermissions("ROLE_ADMIN", "USER_MANAGE");
        baseUser.setEnabled(true);
        baseUser.setLocked(false);
        baseUser.setRoles(Set.of(admin));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        // All conditions for successful authentication
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();

        // Has required permissions
        assertThat(details.hasRole("ADMIN")).isTrue();
        assertThat(details.hasPermission("USER_MANAGE")).isTrue();
    }

    @Test
    void disabledAccountCannotAuthenticate() {
        Role admin = createRoleWithPermissions("ROLE_ADMIN");
        baseUser.setEnabled(false);
        baseUser.setLocked(false);
        baseUser.setRoles(Set.of(admin));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonLocked()).isTrue();
        // But still cannot authenticate because enabled=false
    }

    @Test
    void lockedAccountCannotAuthenticate() {
        Role admin = createRoleWithPermissions("ROLE_ADMIN");
        baseUser.setEnabled(true);
        baseUser.setLocked(true);
        baseUser.setRoles(Set.of(admin));

        CustomUserDetails details = CustomUserDetails.from(baseUser);

        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isFalse();
        // But still cannot authenticate because locked=true (RN-003)
    }
}
