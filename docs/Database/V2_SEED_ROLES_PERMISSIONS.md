# V2__seed_roles_permissions.sql - SQL Completo

## 📋 Resumo

Este arquivo de migration Flyway popula o banco de dados com:
- **15 Permissions** (conforme ADR-001)
- **3 Roles** iniciais (ROLE_ADMIN, ROLE_MANAGER, ROLE_USER)
- **Associações** entre roles e permissions

---

## 🗂️ Estrutura do SQL

```sql
/**
 * MIGRATION V2: Seed de Roles, Permissions e associações
 *
 * Objetivo:
 *   Carregar dados iniciais de papéis de acesso (roles), permissões e
 *   suas associações conforme definido em ADR-001-RBAC.
 *
 * Modelo RBAC:
 *   User N:N Role
 *   Role N:N Permission
 *
 * Convenção:
 *   - Roles sempre prefixadas com ROLE_ (e.g., ROLE_ADMIN)
 *   - Permissions no padrão RECURSO_ACAO (e.g., USER_CREATE, ROLE_UPDATE)
 *   - Permissões associadas APENAS a Roles, nunca diretamente a Users
 *
 * Fonte canônica: docs/Decisions/ADR-001-rbac.md
 */
```

---

## 📦 PHASE 1: Permissions (15 total)

### Categorias de Permissions:

#### 1️⃣ **User Management** (USER_*)
```sql
('USER_CREATE',        'Criar novos usuários'),
('USER_READ',          'Visualizar dados de usuários'),
('USER_UPDATE',        'Atualizar dados de usuários'),
('USER_DELETE',        'Remover/deletar usuários'),
```

#### 2️⃣ **Role Management** (ROLE_*)
```sql
('ROLE_CREATE',        'Criar novos papéis de acesso (Roles)'),
('ROLE_READ',          'Visualizar papéis de acesso (Roles)'),
('ROLE_UPDATE',        'Atualizar papéis de acesso (Roles)'),
('ROLE_DELETE',        'Remover papéis de acesso (Roles)'),
```

#### 3️⃣ **Permission Management** (PERMISSION_*)
```sql
('PERMISSION_CREATE',  'Criar novas permissões'),
('PERMISSION_READ',    'Visualizar permissões disponíveis'),
('PERMISSION_UPDATE',  'Atualizar permissões'),
('PERMISSION_DELETE',  'Remover permissões'),
```

#### 4️⃣ **Audit & Governance**
```sql
('AUDIT_READ',         'Consultar trilha de auditoria (logs de operações)'),
```

#### 5️⃣ **User Profile (Self-Service)**
```sql
('PROFILE_READ',       'Visualizar o próprio perfil e dados pessoais'),
('PROFILE_UPDATE',     'Atualizar o próprio perfil e dados pessoais'),
```

---

## 👥 PHASE 2: Roles (3 total)

### 1️⃣ ROLE_ADMIN
**Descrição**: Administrador da Plataforma - Acesso completo

```sql
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN',
     'Administrador da Plataforma - Acesso completo para gerenciar usuários, papéis, permissões e auditoria');
```

---

### 2️⃣ ROLE_MANAGER
**Descrição**: Gerente Operacional - Gestão limitada

```sql
INSERT INTO roles (name, description) VALUES
    ('ROLE_MANAGER',
     'Gerente Operacional - Gestão de usuários e consulta de papéis e permissões (sem criar/deletar)');
```

---

### 3️⃣ ROLE_USER
**Descrição**: Usuário Final Padrão - Acesso restrito

```sql
INSERT INTO roles (name, description) VALUES
    ('ROLE_USER',
     'Usuário Final Padrão - Acesso limitado ao próprio perfil');
```

---

## 🔗 PHASE 3: Associações Role ↔ Permission

### ROLE_ADMIN - 13 Permissions
**Responsabilidades** (conforme ADR-001):
- ✅ Gerenciar usuários (CREATE, READ, UPDATE, DELETE)
- ✅ Gerenciar papéis (CREATE, READ, UPDATE, DELETE)
- ✅ Gerenciar permissões (CREATE, READ, UPDATE, DELETE)
- ✅ Consultar auditoria
- ❌ NÃO tem acesso a PROFILE_* (perfil é auto-serviço)

```sql
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    -- User Management (FULL)
    'USER_CREATE', 'USER_READ', 'USER_UPDATE', 'USER_DELETE',
    -- Role Management (FULL)
    'ROLE_CREATE', 'ROLE_READ', 'ROLE_UPDATE', 'ROLE_DELETE',
    -- Permission Management (FULL)
    'PERMISSION_CREATE', 'PERMISSION_READ', 'PERMISSION_UPDATE', 'PERMISSION_DELETE',
    -- Audit & Governance
    'AUDIT_READ'
)
WHERE r.name = 'ROLE_ADMIN';
```

| Permissão | Status | Razão |
|-----------|--------|-------|
| USER_CREATE, READ, UPDATE, DELETE | ✅ | Acesso completo a usuários |
| ROLE_CREATE, READ, UPDATE, DELETE | ✅ | Gerencia papéis |
| PERMISSION_CREATE, READ, UPDATE, DELETE | ✅ | Gerencia permissões |
| AUDIT_READ | ✅ | Consulta logs |
| PROFILE_READ, UPDATE | ❌ | Uso auto-serviço via ROLE_USER |

---

### ROLE_MANAGER - 5 Permissions
**Responsabilidades** (conforme ADR-001):
- ✅ Gerenciar usuários: CREATE, READ, UPDATE (SEM DELETE)
- ✅ Visualizar papéis (READ only)
- ✅ Visualizar permissões (READ only)
- ❌ Não acessa auditoria
- ❌ NÃO tem acesso a PROFILE_*

**Restrições**:
- ❌ Não pode deletar usuários
- ❌ Não pode criar/atualizar papéis
- ❌ Não pode criar permissões
- ❌ Não pode alterar configurações críticas

```sql
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    -- User Management (LIMITED)
    'USER_CREATE', 'USER_READ', 'USER_UPDATE',
    -- Role Management (READ-ONLY)
    'ROLE_READ',
    -- Permission Management (READ-ONLY)
    'PERMISSION_READ'
)
WHERE r.name = 'ROLE_MANAGER';
```

| Permissão | Status | Razão |
|-----------|--------|-------|
| USER_CREATE, READ, UPDATE | ✅ | Gestão operacional de usuários |
| USER_DELETE | ❌ | Não pode deletar |
| ROLE_CREATE, UPDATE, DELETE | ❌ | Sem acesso de escrita |
| ROLE_READ | ✅ | Consulta papéis |
| PERMISSION_CREATE, UPDATE, DELETE | ❌ | Sem acesso de escrita |
| PERMISSION_READ | ✅ | Consulta permissões |
| AUDIT_READ | ❌ | Sem acesso à auditoria |
| PROFILE_READ, UPDATE | ❌ | Uso auto-serviço via ROLE_USER |

---

### ROLE_USER - 2 Permissions
**Responsabilidades** (conforme ADR-001):
- ✅ Realizar login
- ✅ Consultar seus próprios dados
- ✅ Atualizar informações pessoais permitidas

**Restrições**:
- ❌ Não pode gerenciar usuários
- ❌ Não pode gerenciar papéis
- ❌ Não pode gerenciar permissões
- ❌ Não pode consultar auditoria

```sql
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    -- User Profile (SELF-SERVICE ONLY)
    'PROFILE_READ', 'PROFILE_UPDATE'
)
WHERE r.name = 'ROLE_USER';
```

| Permissão | Status | Razão |
|-----------|--------|-------|
| PROFILE_READ, UPDATE | ✅ | Auto-serviço: próprio perfil |
| Todas outras | ❌ | Sem acesso |

---

## 📊 Matriz de Permissões por Role

```
┌──────────────────────┬──────────┬──────────┬──────────┐
│ Permissão            │  ADMIN   │ MANAGER  │   USER   │
├──────────────────────┼──────────┼──────────┼──────────┤
│ USER_CREATE          │    ✅    │    ✅    │   ❌     │
│ USER_READ            │    ✅    │    ✅    │   ❌     │
│ USER_UPDATE          │    ✅    │    ✅    │   ❌     │
│ USER_DELETE          │    ✅    │    ❌    │   ❌     │
│ ROLE_CREATE          │    ✅    │    ❌    │   ❌     │
│ ROLE_READ            │    ✅    │    ✅    │   ❌     │
│ ROLE_UPDATE          │    ✅    │    ❌    │   ❌     │
│ ROLE_DELETE          │    ✅    │    ❌    │   ❌     │
│ PERMISSION_CREATE    │    ✅    │    ❌    │   ❌     │
│ PERMISSION_READ      │    ✅    │    ✅    │   ❌     │
│ PERMISSION_UPDATE    │    ✅    │    ❌    │   ❌     │
│ PERMISSION_DELETE    │    ✅    │    ❌    │   ❌     │
│ AUDIT_READ           │    ✅    │    ❌    │   ❌     │
│ PROFILE_READ         │    ❌    │    ❌    │   ✅     │
│ PROFILE_UPDATE       │    ❌    │    ❌    │   ✅     │
└──────────────────────┴──────────┴──────────┴──────────┘
```

---

## 🔍 Detalhes Técnicos

### Tabelas Envolvidas

```
permissions
├── id (PK, Auto-increment)
├── name (VARCHAR 100, UNIQUE) ← RECURSO_ACAO
└── description (VARCHAR 255)

roles
├── id (PK, Auto-increment)
├── name (VARCHAR 50, UNIQUE) ← com prefixo ROLE_
└── description (VARCHAR 255)

role_permissions (N:N Junction Table)
├── role_id (FK → roles.id)
├── permission_id (FK → permissions.id)
└── PRIMARY KEY (role_id, permission_id)
```

### SQL Joins Utilizados

```sql
-- Padrão usado para associações:
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('PERMISSION_1', 'PERMISSION_2', ...)
WHERE r.name = 'ROLE_NAME'
```

**Vantagens**:
- ✅ Dinâmico: busca por nome, não ID (mais legível)
- ✅ Seguro: usa nomes de permissões explícitas
- ✅ Manutenível: cada INSERT é independente
- ✅ Idempotente: pode ser re-executado sem erro

---

## 🚀 Execução

Quando o aplicativo Spring Boot inicia, o Flyway automaticamente:
1. Detecta V2__seed_roles_permissions.sql
2. Executa os INSERTs em ordem
3. Registra a migração em flyway_schema_history

**Resultado final**:
- ✅ 15 Permissions criadas
- ✅ 3 Roles criadas
- ✅ 20 Associações role_permissions criadas (13 + 5 + 2)

---

## 📝 ADR-001 Compliance

| Requisito ADR-001 | Implementação |
|------------------|--------------|
| User N:N Role | ✅ Via tabela user_roles |
| Role N:N Permission | ✅ Via tabela role_permissions (V2) |
| Permissões em Roles | ✅ Apenas em roles, não em users diretamente |
| Padrão RECURSO_ACAO | ✅ USER_CREATE, ROLE_READ, etc |
| ROLE_ADMIN | ✅ Com 13 permissões (completo) |
| ROLE_MANAGER | ✅ Com 5 permissões (operacional) |
| ROLE_USER | ✅ Com 2 permissões (auto-serviço) |
| RN-003 (Bloqueio) | ✅ Campo locked em User (V3) |

---

## 🔄 Próximas Etapas

1. **V4**: Criar índices adicionais em role_permissions para performance
2. **V5**: Seed de usuários iniciais com roles (ex: admin@sistema.local)
3. **V6**: Adicionar novas permissões conforme recursos forem criados

---

## 📚 Referências

- **ADR-001-rbac.md**: Decisão arquitetural para RBAC
- **architecture.md**: Visão geral de tabelas e relacionamentos
- **requirements.md**: Requisitos funcionais de autenticação/autorização

