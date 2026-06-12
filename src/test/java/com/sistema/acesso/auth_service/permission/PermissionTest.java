package com.sistema.acesso.auth_service.permission;

import static org.assertj.core.api.Assertions.assertThat;

import com.sistema.acesso.auth_service.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Permission entity.
 *
 * <p>
 * Tests cover:
 * - Entity lifecycle (creation, setters, getters)
 * - Equality and hash code (based on ID)
 * - Field assignments
 * - Constructors
 * </p>
 */
class PermissionTest {

    private Permission basePermission;

    @BeforeEach
    void setUp() {
        basePermission = new Permission();
        basePermission.setId(1L);
        basePermission.setName("USER_READ");
        basePermission.setDescription("Visualizar usuários");
    }

    // ===== Constructor Tests =====

    @Test
    void noArgsConstructorCreatesEmptyPermission() {
        Permission permission = new Permission();
        assertThat(permission.getId()).isNull();
        assertThat(permission.getName()).isNull();
        assertThat(permission.getDescription()).isNull();
    }

    @Test
    void twoArgsConstructorSetsNameAndDescription() {
        Permission permission = new Permission("USER_CREATE", "Criar usuários");
        assertThat(permission.getName()).isEqualTo("USER_CREATE");
        assertThat(permission.getDescription()).isEqualTo("Criar usuários");
        assertThat(permission.getId()).isNull();
    }

    // ===== Field Assignment Tests =====

    @Test
    void canSetAndGetId() {
        basePermission.setId(999L);
        assertThat(basePermission.getId()).isEqualTo(999L);
    }

    @Test
    void canSetAndGetName() {
        basePermission.setName("USER_DELETE");
        assertThat(basePermission.getName()).isEqualTo("USER_DELETE");
    }

    @Test
    void canSetAndGetDescription() {
        basePermission.setDescription("Remover usuários");
        assertThat(basePermission.getDescription()).isEqualTo("Remover usuários");
    }

    // ===== Equality and HashCode Tests =====

    private Permission permissionWithId(Long id) {
        Permission p = new Permission("USER_READ", "Visualizar usuários");
        p.setId(id);
        return p;
    }

    @Test
    void equalWhenSameId() {
        assertThat(permissionWithId(1L)).isEqualTo(permissionWithId(1L));
        assertThat(permissionWithId(1L)).hasSameHashCodeAs(permissionWithId(1L));
    }

    @Test
    void notEqualWhenDifferentId() {
        assertThat(permissionWithId(1L)).isNotEqualTo(permissionWithId(2L));
    }

    @Test
    void sameInstanceIsAlwaysEqualEvenWithNullId() {
        Permission transientPermission = new Permission();
        assertThat(transientPermission).isEqualTo(transientPermission);
    }

    @Test
    void twoTransientInstancesAreNotEqual() {
        assertThat(new Permission()).isNotEqualTo(new Permission());
    }

    @Test
    void notEqualToNullOrOtherType() {
        assertThat(permissionWithId(1L)).isNotEqualTo(null);
        assertThat(permissionWithId(1L)).isNotEqualTo(new Role("ROLE_ADMIN", "x"));
    }

    @Test
    void notEqualWhenOtherIdIsNull() {
        Permission withId = permissionWithId(1L);
        Permission withoutId = new Permission("USER_READ", "Read");

        assertThat(withId).isNotEqualTo(withoutId);
    }

    // ===== Permission Naming Convention Tests =====

    @Test
    void canHaveResourceActionNamingPattern() {
        String[] validNames = {
            "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE",
            "ROLE_CREATE", "ROLE_READ", "ROLE_UPDATE", "ROLE_DELETE",
            "PERMISSION_CREATE", "PERMISSION_READ", "AUDIT_READ",
            "PROFILE_READ", "PROFILE_UPDATE"
        };

        for (String name : validNames) {
            Permission permission = new Permission(name, "Permission: " + name);
            assertThat(permission.getName()).isEqualTo(name);
        }
    }

    // ===== Integration Tests =====

    @Test
    void permissionWithFullDataIsConsistent() {
        basePermission.setId(10L);
        basePermission.setName("ROLE_UPDATE");
        basePermission.setDescription("Atualizar papéis");

        assertThat(basePermission.getId()).isEqualTo(10L);
        assertThat(basePermission.getName()).isEqualTo("ROLE_UPDATE");
        assertThat(basePermission.getDescription()).isEqualTo("Atualizar papéis");
    }

    @Test
    void multiplePermissionsAreIndependent() {
        Permission perm1 = new Permission("USER_CREATE", "Create");
        perm1.setId(1L);

        Permission perm2 = new Permission("USER_READ", "Read");
        perm2.setId(2L);

        perm1.setName("ROLE_DELETE");

        assertThat(perm1.getName()).isEqualTo("ROLE_DELETE");
        assertThat(perm2.getName()).isEqualTo("USER_READ");
    }
}
