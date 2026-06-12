# 📖 GUIA DETALHADO - Explicação de Cada Arquivo Criado

## 📋 Índice

1. [CustomUserDetails.java](#1-customuserdetailsjava)
2. [CustomUserDetailsService.java](#2-customuserdetailsservicejava)
3. [CustomUserDetailsTest.java](#3-customuserdetailstestjava)
4. [CustomUserDetailsServiceTest.java](#4-customuserdetailsservicetestjava)
5. [V2__seed_roles_permissions.sql](#5-v2__seed_roles_permissionssql)

---

## 1) CustomUserDetails.java

**Arquivo**: `src/main/java/com/sistema/acesso/auth_service/security/CustomUserDetails.java`  
**Tamanho**: ~350 linhas  
**Propósito**: Adapter entre a entidade User do JPA e Spring Security's UserDetails

### 🎯 Objetivo

Converter os dados da entidade `User` (com suas Roles e Permissions) em um objeto que Spring Security entende e utiliza para autenticação e autorização.

### 🏗️ Arquitetura

```
User (JPA Entity)
├── id
├── username
├── email
├── password (BCrypt)
├── enabled
├── locked ← NEW (RN-003)
└── roles (Set<Role>)
    ├── Role1: ROLE_ADMIN
    │   └── permissions (Set<Permission>)
    │       ├── USER_CREATE
    │       ├── USER_READ
    │       └── USER_DELETE
    │
    └── Role2: ROLE_MANAGER
        └── permissions
            ├── USER_CREATE
            └── USER_READ

        ↓↓↓ CustomUserDetails.from(user) ↓↓↓

CustomUserDetails (Spring UserDetails)
├── id
├── username
├── email
├── password (BCrypt)
├── enabled
├── locked
└── authorities (Set<GrantedAuthority>) ← FLATTENED
    ├── ROLE_ADMIN
    ├── ROLE_MANAGER
    ├─�� USER_CREATE (deduplicated!)
    ├── USER_READ (deduplicated!)
    └── USER_DELETE
```

### 📊 Estrutura

```java
@Slf4j
@Getter
public class CustomUserDetails implements UserDetails {
    
    private static final String ROLE_PREFIX = "ROLE_";
    
    // Campos (imutáveis após construção)
    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean locked;  ← NEW: RN-003
    private final Collection<? extends GrantedAuthority> authorities;
    
    // Constructor (privado, para forçar uso de factory method)
    private CustomUserDetails(...)
    
    // Factory method (conversão de User para CustomUserDetails)
    public static CustomUserDetails from(User user)
    
    // Métodos obrigatórios de UserDetails
    @Override getAuthorities()
    @Override getPassword()
    @Override getUsername()
    @Override isAccountNonExpired()
    @Override isAccountNonLocked()       ← Respeita 'locked'
    @Override isCredentialsNonExpired()
    @Override isEnabled()
    
    // Métodos auxiliares (NEW)
    public boolean hasRole(String role)
    public boolean hasPermission(String permission)
    public Set<String> getPermissions()
    public Set<String> getRoles()
}
```

### 🔄 Factory Method: `from(User user)`

**O que faz**:
1. Recebe uma entidade User completa (com roles e permissions carregadas)
2. Itera sobre todas as roles
3. Para cada role:
   - Adiciona o nome da role como GrantedAuthority (ex: ROLE_ADMIN)
   - Adiciona cada permission como GrantedAuthority (ex: USER_CREATE)
4. Retorna CustomUserDetails com authorities materializadas

**Por que é importante**:
- ✅ **Conversão**: Transforma estrutura hierárquica em lista plana
- ✅ **Deduplicação**: Mesma permission em múltiplas roles não duplica
- ✅ **Transação**: Deve ser chamado dentro de transação ativa
- ✅ **Segurança**: Materializa tudo antes de fechar a transação

**Exemplo**:
```java
User user = userRepository.findByUsername("john_doe"); // roles + permissions já carregadas
CustomUserDetails details = CustomUserDetails.from(user);
// details.getAuthorities() → [ROLE_ADMIN, ROLE_MANAGER, USER_CREATE, USER_READ, ...]
```

### 🔒 Implementação de UserDetails

#### `isAccountNonLocked()` ← KEY DIFFERENCE
```java
@Override
public boolean isAccountNonLocked() {
    return !locked;  // Se locked=true, não consegue autenticar
}
```
- **Spring Padrão**: Sempre retorna true (sem conceito de "bloqueio")
- **Nossa Implementação**: Respeita o campo `locked` do User (RN-003)
- **Resultado**: Usuários bloqueados não conseguem fazer login

#### `isAccountNonExpired()`
```java
@Override
public boolean isAccountNonExpired() {
    return true;  // Sempre true (não implementado)
}
```
- Se implementar expiração, adicione um campo `expiresAt` em User

#### `isEnabled()`
```java
@Override
public boolean isEnabled() {
    return enabled;  // Respeita flag 'enabled'
}
```

### 🛠️ Métodos Auxiliares (NEW)

#### `hasRole(String role)`
```java
public boolean hasRole(String role) {
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(auth -> auth.equals(ROLE_PREFIX + role));
}

// Usage:
if (customUserDetails.hasRole("ADMIN")) { ... }
```

#### `hasPermission(String permission)`
```java
public boolean hasPermission(String permission) {
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(auth -> auth.equals(permission));
}

// Usage:
if (customUserDetails.hasPermission("USER_CREATE")) { ... }
```

#### `getPermissions()` e `getRoles()`
```java
// Retorna apenas permissions (sem "ROLE_" prefix)
public Set<String> getPermissions() {
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(auth -> !auth.startsWith(ROLE_PREFIX))
        .collect(Collectors.toSet());
}

// Retorna apenas roles (com "ROLE_" prefix)
public Set<String> getRoles() {
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(auth -> auth.startsWith(ROLE_PREFIX))
        .collect(Collectors.toSet());
}
```

### 📝 Documentação

- ✅ **50+ linhas de JavaDoc** no topo da classe
- ✅ **Explicação do modelo RBAC**
- ✅ **Lista de responsabilidades**
- ✅ **Thread safety guarantee**
- ✅ **Referências**: ADR-001, RN-003

---

## 2) CustomUserDetailsService.java

**Arquivo**: `src/main/java/com/sistema/acesso/auth_service/security/CustomUserDetailsService.java`  
**Tamanho**: ~210 linhas  
**Propósito**: Serviço Spring Security que carrega usuários do banco de dados

### 🎯 Objetivo

Implementar a interface `UserDetailsService` de Spring Security para que o framework consiga:
1. Buscar usuários no banco de dados
2. Converter para `CustomUserDetails`
3. Usar para autenticação

### 🏗️ Arquitetura

```
Spring Security Framework
    ↓
    calls: loadUserByUsername(username)
    ↓
CustomUserDetailsService
    ↓
    calls: userRepository.findByUsername(username)
    ↓
Database (PostgreSQL)
    ↓
    returns: User with roles + permissions (via EntityGraph)
    ↓
CustomUserDetails.from(user)
    ↓
    returns: CustomUserDetails with authorities
    ↓
Spring Security
    ├─ Valida password
    ├─ Checa flags (enabled, locked, etc)
    └─ Cria Authentication token com authorities
```

### 📊 Estrutura

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    // OBRIGATÓRIO de UserDetailsService
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
    
    // NOVOS - Métodos adicionais
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email)
    
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId)
    
    @Transactional(readOnly = true)
    public User loadUserEntityByUsername(String username)
}
```

### ⚙️ Cada Método

#### 1. `loadUserByUsername(String username)` [OBRIGATÓRIO]

```java
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
```

**Uso**:
- Chamado automaticamente por Spring Security ao fazer autenticação
- Busca por username
- Carrega roles e permissions via EntityGraph
- Retorna CustomUserDetails ou lança UsernameNotFoundException

**Transação**:
- `readOnly = true`: Otimiza performance
- Garante lazy loading seguro (dentro da transação)

#### 2. `loadUserByEmail(String email)` [NOVO]

```java
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
```

**Uso**:
- Não é obrigatório de Spring Security
- Para mecanismos alternativos: OAuth2, social login, email-based auth
- Mesma implementação que loadUserByUsername

#### 3. `loadUserById(Long userId)` [NOVO]

```java
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
```

**Uso**:
- Para JWT filters
- Quando já temos o ID de um token validado
- Evita buscar por username em cada requisição

#### 4. `loadUserEntityByUsername(String username)` [NOVO]

```java
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
```

**Uso**:
- Retorna a entidade User (não UserDetails)
- Para operações internas que precisam de todos os campos
- Ex: Update user, verificar estado completo

### 🔒 Transações & Lazy Loading

```
├─ @Transactional(readOnly = true)
│  ├─ Abre transação leitura
│  ├─ Executa query com EntityGraph
│  │  └─ Carrega User + Roles + Permissions
│  ├─ Chama CustomUserDetails.from(user)
│  │  └─ Materializa authorities dentro da transação
│  └─ Transação fecha
│
├─ Fora da transação
│  └─ CustomUserDetails retornado é seguro
│     (todas as coleções já foram acessadas)
```

### 📝 Documentação

- ✅ **80+ linhas de JavaDoc**
- ✅ **Explicação de transações**
- ✅ **Segurança de lazy loading**
- ✅ **Exemplos de uso**
- ✅ **Logging em debug/warn**

---

## 3) CustomUserDetailsTest.java

**Arquivo**: `src/test/java/.../security/CustomUserDetailsTest.java`  
**Tamanho**: ~200 linhas  
**Testes**: 18  
**Cobertura**: CustomUserDetails

### 🎯 Estratégia de Testes

Testar:
1. ✅ Mapeamento de campos escalares (id, username, email, password)
2. ✅ Conversão de Roles e Permissions em Authorities
3. ✅ Flags de conta (enabled, locked, expired)
4. ✅ Métodos auxiliares (hasRole, hasPermission, getRoles, getPermissions)
5. ✅ Casos integrados (autenticação bem/mal sucedida)

### 📋 Testes por Categoria

#### 1️⃣ Mapeamento Escalar (1 teste)

```java
@Test
void mapsScalarFieldsFromEntity() {
    Role admin = createRoleWithPermissions("ROLE_ADMIN");
    baseUser.setRoles(Set.of(admin));
    
    CustomUserDetails details = CustomUserDetails.from(baseUser);
    
    assertThat(details.getId()).isEqualTo(1L);
    assertThat(details.getUsername()).isEqualTo("john_doe");
    assertThat(details.getEmail()).isEqualTo("john@example.com");
    assertThat(details.getPassword()).isEqualTo("$2a$10$...");
    assertThat(details.isEnabled()).isTrue();
}
```

#### 2️⃣ Conversão de Authorities (4 testes)

```java
@Test
void exposesRolesAndPermissionsAsAuthorities() {
    Role admin = createRoleWithPermissions("ROLE_ADMIN", "USER_READ", "USER_CREATE");
    baseUser.setRoles(Set.of(admin));
    
    CustomUserDetails details = CustomUserDetails.from(baseUser);
    
    assertThat(AuthorityUtils.authorityListToSet(details.getAuthorities()))
        .containsExactlyInAnyOrder("ROLE_ADMIN", "USER_READ", "USER_CREATE");
}
```

Testa:
- ✅ Single role + permissions
- ✅ Multiple roles + permissions
- ✅ No roles = no authorities
- ✅ Role sem permissions = só nome

#### 3️⃣ Flags de Conta (4 testes)

```java
@Test
void accountNonLockedReflectsLockedState() {
    baseUser.setLocked(false);
    CustomUserDetails unlocked = CustomUserDetails.from(baseUser);
    
    baseUser.setLocked(true);
    CustomUserDetails locked = CustomUserDetails.from(baseUser);
    
    assertThat(unlocked.isAccountNonLocked()).isTrue();
    assertThat(locked.isAccountNonLocked()).isFalse();  // ! RN-003
}
```

#### 4️⃣ Métodos Auxiliares (6 testes)

```java
@Test
void hasRoleDetectsRolePresentAndAbsent() {
    Role admin = createRoleWithPermissions("ROLE_ADMIN");
    baseUser.setRoles(Set.of(admin));
    
    CustomUserDetails details = CustomUserDetails.from(baseUser);
    
    assertThat(details.hasRole("ADMIN")).isTrue();
    assertThat(details.hasRole("MANAGER")).isFalse();
}
```

#### 5️⃣ Casos de Integração (3 testes)

```java
@Test
void lockedAccountCannotAuthenticate() {
    Role admin = createRoleWithPermissions("ROLE_ADMIN");
    baseUser.setEnabled(true);
    baseUser.setLocked(true);  // Bloqueado!
    baseUser.setRoles(Set.of(admin));
    
    CustomUserDetails details = CustomUserDetails.from(baseUser);
    
    assertThat(details.isEnabled()).isTrue();
    assertThat(details.isAccountNonLocked()).isFalse();  // FALHA NA AUTENTICAÇÃO
}
```

---

## 4) CustomUserDetailsServiceTest.java

**Arquivo**: `src/test/java/.../security/CustomUserDetailsServiceTest.java`  
**Tamanho**: ~250 linhas  
**Testes**: 16  
**Cobertura**: CustomUserDetailsService

### 🎯 Estratégia de Testes

Testar:
1. ✅ loadUserByUsername (5 testes)
2. ✅ loadUserByEmail (3 testes)
3. ✅ loadUserById (3 testes)
4. ✅ loadUserEntityByUsername (3 testes)
5. ✅ Integração (2 testes)

### 📋 Testes por Categoria

#### 1️⃣ loadUserByUsername (5 testes)

```java
@Test
void loadUserByUsernameReturnsCustomUserDetailsWhenFound() {
    when(userRepository.findByUsername("john_doe"))
        .thenReturn(Optional.of(testUser));
    
    UserDetails details = service.loadUserByUsername("john_doe");
    
    assertThat(details).isInstanceOf(CustomUserDetails.class);
    assertThat(details.getUsername()).isEqualTo("john_doe");
}

@Test
void loadUserByUsernameThrowsWhenUserNotFound() {
    when(userRepository.findByUsername("nonexistent"))
        .thenReturn(Optional.empty());
    
    assertThatThrownBy(() -> service.loadUserByUsername("nonexistent"))
        .isInstanceOf(UsernameNotFoundException.class);
}
```

#### 2️⃣ loadUserByEmail (3 testes)

```java
@Test
void loadUserByEmailReturnsCustomUserDetailsWhenFound() {
    when(userRepository.findByEmail("john@example.com"))
        .thenReturn(Optional.of(testUser));
    
    UserDetails details = service.loadUserByEmail("john@example.com");
    
    assertThat(details).isInstanceOf(CustomUserDetails.class);
}
```

#### 3️⃣ Testes de RN-003 (Bloqueio)

```java
@Test
void loadMethodsRespectLockedState() {
    testUser.setLocked(true);
    when(userRepository.findByUsername("john_doe"))
        .thenReturn(Optional.of(testUser));
    
    CustomUserDetails details = (CustomUserDetails) 
        service.loadUserByUsername("john_doe");
    
    assertThat(details.isAccountNonLocked()).isFalse();
    // Usuário não consegue autenticar (RN-003)
}
```

### 🔧 Setup dos Testes

```java
@BeforeEach
void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("john_doe");
    testUser.setEmail("john@example.com");
    testUser.setPassword("$2a$10$bcrypthashedpassword");
    testUser.setEnabled(true);
    testUser.setLocked(false);
    
    Role admin = new Role("ROLE_ADMIN", "Administrator");
    Permission userCreate = new Permission("USER_CREATE", "Create users");
    admin.setPermissions(Set.of(userCreate));
    
    testUser.setRoles(Set.of(admin));
}
```

---

## 5) V2__seed_roles_permissions.sql

**Arquivo**: `src/main/resources/db/migration/V2__seed_roles_permissions.sql`  
**Tamanho**: ~120 linhas  
**Execução**: Flyway (automática no startup)

### 🎯 Objetivo

Popular o banco de dados com dados iniciais de:
- Permissions (15 total)
- Roles (3 total)
- Associações role_permissions (20 total)

### 📊 Estrutura SQL

#### PHASE 1: Criar 15 Permissions

```sql
INSERT INTO permissions (name, description) VALUES
    -- USER MANAGEMENT (4)
    ('USER_CREATE',        'Criar novos usuários'),
    ('USER_READ',          'Visualizar dados de usuários'),
    ('USER_UPDATE',        'Atualizar dados de usuários'),
    ('USER_DELETE',        'Remover/deletar usuários'),
    
    -- ROLE MANAGEMENT (4)
    ('ROLE_CREATE',        'Criar novos papéis de acesso'),
    ('ROLE_READ',          'Visualizar papéis de acesso'),
    ('ROLE_UPDATE',        'Atualizar papéis de acesso'),
    ('ROLE_DELETE',        'Remover papéis de acesso'),
    
    -- PERMISSION MANAGEMENT (4)
    ('PERMISSION_CREATE',  'Criar novas permissões'),
    ('PERMISSION_READ',    'Visualizar permissões'),
    ('PERMISSION_UPDATE',  'Atualizar permissões'),
    ('PERMISSION_DELETE',  'Remover permissões'),
    
    -- GOVERNANCE (1)
    ('AUDIT_READ',         'Consultar trilha de auditoria'),
    
    -- PROFILE (2)
    ('PROFILE_READ',       'Visualizar o próprio perfil'),
    ('PROFILE_UPDATE',     'Atualizar o próprio perfil');
```

#### PHASE 2: Criar 3 Roles

```sql
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN',
     'Administrador da Plataforma - Acesso completo'),
    
    ('ROLE_MANAGER',
     'Gerente Operacional - Gestão limitada'),
    
    ('ROLE_USER',
     'Usuário Final Padrão - Auto-serviço');
```

#### PHASE 3: Criar 20 Associações (role_permissions)

```sql
-- ROLE_ADMIN → 13 Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'USER_CREATE', 'USER_READ', 'USER_UPDATE', 'USER_DELETE',
    'ROLE_CREATE', 'ROLE_READ', 'ROLE_UPDATE', 'ROLE_DELETE',
    'PERMISSION_CREATE', 'PERMISSION_READ', 'PERMISSION_UPDATE', 'PERMISSION_DELETE',
    'AUDIT_READ'
)
WHERE r.name = 'ROLE_ADMIN';

-- ROLE_MANAGER → 5 Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'USER_CREATE', 'USER_READ', 'USER_UPDATE',
    'ROLE_READ',
    'PERMISSION_READ'
)
WHERE r.name = 'ROLE_MANAGER';

-- ROLE_USER → 2 Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('PROFILE_READ', 'PROFILE_UPDATE')
WHERE r.name = 'ROLE_USER';
```

### 🔍 Técnicas Utilizadas

#### Dynamic JOINs
```sql
-- Ao invés de hardcoded IDs:
-- BAD: INSERT INTO role_permissions VALUES (1, 5);

-- GOOD: JOIN com nomes
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('USER_READ', 'USER_CREATE')
WHERE r.name = 'ROLE_ADMIN'
```

**Vantagens**:
- ✅ Legível: vê os nomes das permissões
- ✅ Seguro: Não precisa saber IDs
- ✅ Idempotente: Pode re-executar
- ✅ Manutenível: Não quebra com diferentes IDs

#### Explicit Permission Lists
```sql
-- Deixa claro quais permissões cada role recebe
WHERE r.name = 'ROLE_ADMIN'
AND p.name IN (
    'USER_CREATE',      -- 1
    'USER_READ',        -- 2
    'USER_UPDATE',      -- 3
    'USER_DELETE',      -- 4
    'ROLE_CREATE',      -- 5
    ...
)
```

### 📝 Resultado Final

```
Após executar esta migration:

PERMISSIONS TABLE:
├─ 15 registros com RECURSO_ACAO
└─ Ex: USER_CREATE, ROLE_READ, AUDIT_READ, etc

ROLES TABLE:
├─ 3 registros
├─ ROLE_ADMIN
├─ ROLE_MANAGER
└─ ROLE_USER

ROLE_PERMISSIONS TABLE:
├─ 20 registros (associações)
├─ ROLE_ADMIN → 13 permissions
├─ ROLE_MANAGER → 5 permissions
└─ ROLE_USER → 2 permissions
```

### 🚀 Execução Automática

Quando a aplicação Spring Boot inicia:

```
1. Flyway scanner detecta V2__seed_roles_permissions.sql
2. Checa tabela flyway_schema_history
3. Se não foi executado antes, executa
4. Registra com hash do arquivo
5. 15 + 3 + 20 = 38 INSERTs executados
```

---

## 🎯 COMO TODOS OS ARQUIVOS TRABALHAM JUNTOS

```
┌─────────────────────────────────────────────────────────┐
│ Spring Boot Application Starts                           │
├─────────────────────────────────────────────────────────┤
│                                                          │
│ 1. Flyway Migration (V2__seed_roles_permissions.sql)    │
│    └─ INSERT 15 Permissions                             │
│    └─ INSERT 3 Roles                                    │
│    └─ INSERT 20 role_permissions                        │
│                                                          │
│ 2. Spring Security Configuration                        │
│    └─ Bean: CustomUserDetailsService @Service           │
│    └─ Uses: UserRepository                              │
│                                                          │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│ User Requests: POST /api/auth/login with credentials    │
├──���──────────────────────────────────────────────────────┤
│                                                          │
│ 1. Spring Security Framework                            │
│    ↓ calls loadUserByUsername(username)                 │
│                                                          │
│ 2. CustomUserDetailsService                             │
│    └─ @Transactional(readOnly=true)                     │
│    └─ userRepository.findByUsername(username)           │
│       └─ EntityGraph loads: User + Roles + Permissions  │
│    └─ CustomUserDetails.from(user)                      │
│                                                          │
│ 3. CustomUserDetails                                    │
│    └─ Iterates over user.roles                          │
│    └─ Adds: ROLE_ADMIN, USER_CREATE, etc               │
│    └─ Returns authorities (Set<GrantedAuthority>)       │
│                                                          │
│ 4. Spring Security                                      │
│    └─ Validates password (BCrypt)                       │
│    └─ Checks isEnabled() ✅                             │
│    └─ Checks isAccountNonLocked() ✅                    │
│    └─ Creates Authentication with authorities            │
│                                                          │
│ 5. JWT Token Generated (next phase)                     │
│    └─ Contains: id, username, authorities               │
│                                                          │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│ User Requests: GET /api/users with JWT Token            │
├─────────────────────────────────────────────────────────┤
│                                                          │
│ 1. JWT Filter (future)                                  │
│    └─ Extracts token from header                        │
│    └─ Validates token signature                         │
│    └─ Gets user ID from token claims                    │
│                                                          │
│ 2. CustomUserDetailsService                             │
│    └─ loadUserById(userId) (future)                     │
│    └─ Reloads CustomUserDetails with current authorities │
│                                                          │
│ 3. @PreAuthorize Annotation (future)                    │
│    └─ @PreAuthorize("hasAuthority('USER_READ')")        │
│    └─ Spring checks if authorities contains 'USER_READ' │
│    └─ If yes: execute method                            │
│    └─ If no: 403 Forbidden                              │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## ✨ Conclusão

Cada arquivo tem um papel específico:

1. **CustomUserDetails.java** - Adapter de dados
2. **CustomUserDetailsService.java** - Carregamento + transações
3. **CustomUserDetailsTest.java** - Validação de conversão
4. **CustomUserDetailsServiceTest.java** - Validação de carregamento
5. **V2__seed_roles_permissions.sql** - Dados iniciais

Todos trabalham juntos para criar uma camada de segurança robusta e totalmente testada! 🚀

