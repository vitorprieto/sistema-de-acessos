package com.sistema.acesso.auth_service.role;

import static org.assertj.core.api.Assertions.assertThat;

import com.sistema.acesso.auth_service.permission.Permission;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Role entity.
 *
 * <p>
 * Tests cover:
 * - Entity lifecycle (creation, setters, getters)
 * - Equality and hash code (based on ID)
 * - Many-to-many relationship with Permission
 * - Constructors and field assignments
 * </p>
 */
class RoleTest {

    private Role baseRole;

    @BeforeEach
    void setUp() {
        baseRole = new Role();
        baseRole.setId(1L);
        baseRole.setName("ROLE_ADMIN");
        baseRole.setDescription("Administrador");
    }

    // ===== Constructor Tests =====

    @Test
    void noArgsConstructorCreatesEmptyRole() {
        Role role = new Role();
        assertThat(role.getId()).isNull();
        assertThat(role.getName()).isNull();
        assertThat(role.getDescription()).isNull();
        assertThat(role.getPermissions()).isNotNull().isEmpty();
    }

    @Test
    void twoArgsConstructorSetsNameAndDescription() {
        Role role = new Role("ROLE_USER", "Usuário padrão");
        assertThat(role.getName()).isEqualTo("ROLE_USER");
        assertThat(role.getDescription()).isEqualTo("Usuário padrão");
        assertThat(role.getId()).isNull();
    }

    // ===== Field Assignment Tests =====

    @Test
    void canSetAndGetId() {
        baseRole.setId(999L);
        assertThat(baseRole.getId()).isEqualTo(999L);
    }

    @Test
    void canSetAndGetName() {
        baseRole.setName("ROLE_MANAGER");
        assertThat(baseRole.getName()).isEqualTo("ROLE_MANAGER");
    }

    @Test
    void canSetAndGetDescription() {
        baseRole.setDescription("Gerente de operações");
        assertThat(baseRole.getDescription()).isEqualTo("Gerente de operações");
    }

    @Test
    void canSetAndGetPermissions() {
        Permission perm1 = new Permission("USER_READ", "Visualizar usuários");
        Permission perm2 = new Permission("USER_CREATE", "Criar usuários");
        Set<Permission> permissions = new HashSet<>();
        permissions.add(perm1);
        permissions.add(perm2);

        baseRole.setPermissions(permissions);

        assertThat(baseRole.getPermissions()).hasSize(2).contains(perm1, perm2);
    }

    // ===== Default Values Tests =====

    @Test
    void permissionsDefaultsToEmptyHashSet() {
        Role role = new Role();
        assertThat(role.getPermissions()).isNotNull().isEmpty();
    }

    // ===== Equality and HashCode Tests =====

    private Role roleWithId(Long id) {
        Role r = new Role("ROLE_ADMIN", "Administrador");
        r.setId(id);
        return r;
    }

    @Test
    void equalWhenSameId() {
        assertThat(roleWithId(1L)).isEqualTo(roleWithId(1L));
        assertThat(roleWithId(1L)).hasSameHashCodeAs(roleWithId(1L));
    }

    @Test
    void notEqualWhenDifferentId() {
        assertThat(roleWithId(1L)).isNotEqualTo(roleWithId(2L));
    }

    @Test
    void sameInstanceIsAlwaysEqualEvenWithNullId() {
        Role transientRole = new Role();
        assertThat(transientRole).isEqualTo(transientRole);
    }

    @Test
    void twoTransientInstancesAreNotEqual() {
        assertThat(new Role()).isNotEqualTo(new Role());
    }

    @Test
    void notEqualToNullOrOtherType() {
        assertThat(roleWithId(1L)).isNotEqualTo(null);
        assertThat(roleWithId(1L)).isNotEqualTo(new Permission("USER_READ", "x"));
    }

    @Test
    void notEqualWhenOtherIdIsNull() {
        Role withId = roleWithId(1L);
        Role withoutId = new Role("ROLE_ADMIN", "Administrador");

        assertThat(withId).isNotEqualTo(withoutId);
    }

    // ===== Many-to-Many Relationship Tests =====

    @Test
    void canAddPermissionToRole() {
        Permission permission = new Permission("USER_READ", "Ler usuários");
        baseRole.getPermissions().add(permission);

        assertThat(baseRole.getPermissions()).hasSize(1).contains(permission);
    }

    @Test
    void canRemovePermissionFromRole() {
        Permission perm1 = new Permission("USER_READ", "Ler");
        Permission perm2 = new Permission("USER_CREATE", "Criar");
        baseRole.getPermissions().add(perm1);
        baseRole.getPermissions().add(perm2);

        baseRole.getPermissions().remove(perm1);

        assertThat(baseRole.getPermissions()).hasSize(1).contains(perm2);
    }

    @Test
    void canHaveMultiplePermissions() {
        Set<Permission> permissions = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            permissions.add(new Permission("PERM_" + i, "Permission " + i));
        }

        baseRole.setPermissions(permissions);

        assertThat(baseRole.getPermissions()).hasSize(5);
    }

    // ===== Integration Tests =====

    @Test
    void roleWithFullDataIsConsistent() {
        Permission p1 = new Permission("USER_CREATE", "Create");
        Permission p2 = new Permission("USER_READ", "Read");

        baseRole.setId(5L);
        baseRole.setName("ROLE_ADMIN");
        baseRole.setDescription("Administrator");
        baseRole.getPermissions().add(p1);
        baseRole.getPermissions().add(p2);

        assertThat(baseRole.getId()).isEqualTo(5L);
        assertThat(baseRole.getName()).isEqualTo("ROLE_ADMIN");
        assertThat(baseRole.getDescription()).isEqualTo("Administrator");
        assertThat(baseRole.getPermissions()).hasSize(2);
    }

    @Test
    void multipleRolesWithSamePermissionAreIndependent() {
        Permission sharedPerm = new Permission("USER_READ", "Read users");

        Role role1 = new Role("ROLE_ADMIN", "Admin");
        role1.setId(1L);
        role1.getPermissions().add(sharedPerm);

        Role role2 = new Role("ROLE_USER", "User");
        role2.setId(2L);
        role2.getPermissions().add(sharedPerm);

        role1.getPermissions().clear();

        assertThat(role1.getPermissions()).isEmpty();
        assertThat(role2.getPermissions()).hasSize(1).contains(sharedPerm);
    }
}
