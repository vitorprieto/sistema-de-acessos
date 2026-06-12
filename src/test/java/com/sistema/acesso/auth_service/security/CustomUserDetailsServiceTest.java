package com.sistema.acesso.auth_service.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sistema.acesso.auth_service.permission.Permission;
import com.sistema.acesso.auth_service.role.Role;
import com.sistema.acesso.auth_service.user.User;
import com.sistema.acesso.auth_service.user.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Unit tests for CustomUserDetailsService.
 *
 * <p>
 * Tests the loading of user details from the repository and conversion to
 * CustomUserDetails by username, email, or ID.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john_doe");
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("$2a$10$bcrypthashedpassword");
        testUser.setEnabled(true);
        testUser.setLocked(false);
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());

        Role admin = new Role("ROLE_ADMIN", "Administrator");
        Permission userCreate = new Permission("USER_CREATE", "Create users");
        admin.setPermissions(Set.of(userCreate));

        testUser.setRoles(Set.of(admin));
    }

    // ===== loadUserByUsername Tests =====

    @Test
    void loadUserByUsernameReturnsCustomUserDetailsWhenFound() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        UserDetails details = service.loadUserByUsername("john_doe");

        assertThat(details).isInstanceOf(CustomUserDetails.class);
        assertThat(details.getUsername()).isEqualTo("john_doe");
        assertThat(details.getPassword()).isEqualTo("$2a$10$bcrypthashedpassword");
        verify(userRepository).findByUsername("john_doe");
    }

    @Test
    void loadUserByUsernameThrowsWhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("nonexistent"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("nonexistent")
            .hasMessageContaining("Usuário não encontrado");

        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void loadUserByUsernameIncludesAuthorities() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        UserDetails details = service.loadUserByUsername("john_doe");

        assertThat(details.getAuthorities())
            .map(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_ADMIN", "USER_CREATE");
    }

    @Test
    void loadUserByUsernameIncludesEnabledFlag() {
        testUser.setEnabled(true);
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        UserDetails details = service.loadUserByUsername("john_doe");

        assertThat(details.isEnabled()).isTrue();

        testUser.setEnabled(false);
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        UserDetails disabledDetails = service.loadUserByUsername("john_doe");

        assertThat(disabledDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsernameIncludesLockedFlag() {
        testUser.setLocked(false);
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        CustomUserDetails details = (CustomUserDetails) service.loadUserByUsername("john_doe");

        assertThat(details.isAccountNonLocked()).isTrue();

        testUser.setLocked(true);
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        CustomUserDetails lockedDetails = (CustomUserDetails) service.loadUserByUsername("john_doe");

        assertThat(lockedDetails.isAccountNonLocked()).isFalse();
    }

    // ===== loadUserByEmail Tests =====

    @Test
    void loadUserByEmailReturnsCustomUserDetailsWhenFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        UserDetails details = service.loadUserByEmail("john@example.com");

        assertThat(details).isInstanceOf(CustomUserDetails.class);
        assertThat(((CustomUserDetails) details).getEmail()).isEqualTo("john@example.com");
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void loadUserByEmailThrowsWhenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByEmail("nonexistent@example.com"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("nonexistent@example.com")
            .hasMessageContaining("Usuário não encontrado");

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void loadUserByEmailIncludesCorrectUserData() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        UserDetails details = service.loadUserByEmail("john@example.com");

        assertThat(details.getUsername()).isEqualTo("john_doe");
        assertThat(details.getPassword()).isEqualTo("$2a$10$bcrypthashedpassword");
    }

    // ===== loadUserById Tests =====

    @Test
    void loadUserByIdReturnsCustomUserDetailsWhenFound() {
        when(userRepository.findWithRolesById(1L)).thenReturn(Optional.of(testUser));

        UserDetails details = service.loadUserById(1L);

        assertThat(details).isInstanceOf(CustomUserDetails.class);
        assertThat(((CustomUserDetails) details).getId()).isEqualTo(1L);
        verify(userRepository).findWithRolesById(1L);
    }

    @Test
    void loadUserByIdThrowsWhenUserNotFound() {
        when(userRepository.findWithRolesById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserById(999L))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("999")
            .hasMessageContaining("Usuário não encontrado");

        verify(userRepository).findWithRolesById(999L);
    }

    @Test
    void loadUserByIdIncludesAuthorities() {
        when(userRepository.findWithRolesById(1L)).thenReturn(Optional.of(testUser));

        UserDetails details = service.loadUserById(1L);

        assertThat(details.getAuthorities())
            .map(auth -> auth.getAuthority())
            .containsExactlyInAnyOrder("ROLE_ADMIN", "USER_CREATE");
    }

    // ===== loadUserEntityByUsername Tests =====

    @Test
    void loadUserEntityByUsernameReturnsUserWhenFound() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        User user = service.loadUserEntityByUsername("john_doe");

        assertThat(user).isEqualTo(testUser);
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getId()).isEqualTo(1L);
        verify(userRepository).findByUsername("john_doe");
    }

    @Test
    void loadUserEntityByUsernameThrowsWhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserEntityByUsername("nonexistent"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("nonexistent")
            .hasMessageContaining("Usuário não encontrado");

        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void loadUserEntityByUsernameReturnsFullUserEntity() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        User user = service.loadUserEntityByUsername("john_doe");

        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isLocked()).isFalse();
        assertThat(user.getRoles()).hasSize(1);
    }

    // ===== Integration Tests =====

    @Test
    void allLoadMethodsReturnConsistentData() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findWithRolesById(1L)).thenReturn(Optional.of(testUser));

        UserDetails byUsername = service.loadUserByUsername("john_doe");
        UserDetails byEmail = service.loadUserByEmail("john@example.com");
        UserDetails byId = service.loadUserById(1L);

        assertThat(byUsername.getUsername()).isEqualTo(byEmail.getUsername()).isEqualTo(byId.getUsername());
        assertThat(byUsername.getPassword()).isEqualTo(byEmail.getPassword()).isEqualTo(byId.getPassword());
        assertThat(byUsername.getAuthorities()).isEqualTo(byEmail.getAuthorities()).isEqualTo(byId.getAuthorities());
    }

    @Test
    void loadMethodsRespectLockedState() {
        testUser.setLocked(true);
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

        CustomUserDetails details = (CustomUserDetails) service.loadUserByUsername("john_doe");

        assertThat(details.isAccountNonLocked()).isFalse();
        // User cannot authenticate even if enabled (RN-003)
    }
}
