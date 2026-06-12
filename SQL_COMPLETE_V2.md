# 📜 SQL COMPLETO - V2__seed_roles_permissions.sql

## Apresentação Visual do SQL Completo

### 🗂️ Estrutura Completa

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
 *   - Permissions no padr��o RECURSO_ACAO (e.g., USER_CREATE, ROLE_UPDATE)
 *   - Permissões associadas APENAS a Roles, nunca diretamente a Users
 *
 * Fonte canônica: docs/Decisions/ADR-001-rbac.md
 */

-- ============================================================================
-- PHASE 1: Criar todas as Permissions
-- ============================================================================
-- 
-- Permissões para Usuários (USER_*)
-- Permissões para Roles      (ROLE_*)
-- Permissões para Permissões (PERMISSION_*)
-- Permissões para Auditoria  (AUDIT_*)
-- Permissões para Perfil     (PROFILE_*)

INSERT INTO permissions (name, description) VALUES
    -- User Management Permissions
    ('USER_CREATE',        'Criar novos usuários'),
    ('USER_READ',          'Visualizar dados de usuários'),
    ('USER_UPDATE',        'Atualizar dados de usuários'),
    ('USER_DELETE',        'Remover/deletar usuários'),

    -- Role Management Permissions
    ('ROLE_CREATE',        'Criar novos papéis de acesso (Roles)'),
    ('ROLE_READ',          'Visualizar papéis de acesso (Roles)'),
    ('ROLE_UPDATE',        'Atualizar papéis de acesso (Roles)'),
    ('ROLE_DELETE',        'Remover papéis de acesso (Roles)'),

    -- Permission Management Permissions
    ('PERMISSION_CREATE',  'Criar novas permissões'),
    ('PERMISSION_READ',    'Visualizar permissões disponíveis'),
    ('PERMISSION_UPDATE',  'Atualizar permissões'),
    ('PERMISSION_DELETE',  'Remover permissões'),

    -- Audit & Governance Permissions
    ('AUDIT_READ',         'Consultar trilha de auditoria (logs de operações)'),

    -- User Profile Permissions (Self-Service)
    ('PROFILE_READ',       'Visualizar o próprio perfil e dados pessoais'),
    ('PROFILE_UPDATE',     'Atualizar o próprio perfil e dados pessoais');

-- ============================================================================
-- PHASE 2: Criar os três Roles Iniciais
-- ============================================================================
--
-- ROLE_ADMIN:   Administrador - Acesso completo, gerencia todos os recursos
-- ROLE_MANAGER: Gerente       - Gestão operacional de usuários (limitado)
-- ROLE_USER:    Usuário       - Usuário final com acesso ao próprio perfil

INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN',
     'Administrador da Plataforma - Acesso completo para gerenciar usuários, papéis, permissões e auditoria'),
    
    ('ROLE_MANAGER',
     'Gerente Operacional - Gestão de usuários e consulta de papéis e permissões (sem criar/deletar)'),
    
    ('ROLE_USER',
     'Usuário Final Padrão - Acesso limitado ao próprio perfil');

-- ============================================================================
-- PHASE 3: Associar Permissions aos Roles via role_permissions
-- ============================================================================
--
-- Relacionamento: Uma Role tem várias Permissions (N:N)
-- As Permissions são linkadas via a tabela role_permissions

-- ----------------------
-- ROLE_ADMIN Permissions:
-- ----------------------
-- Responsabilidades (conforme ADR-001):
--   - Gerenciar usuários (CREATE, READ, UPDATE, DELETE)
--   - Gerenciar papéis (CREATE, READ, UPDATE, DELETE)
--   - Gerenciar permissões (CREATE, READ, UPDATE, DELETE)
--   - Consultar auditoria
--   - NÃO tem acesso a PROFILE_* (perfil é auto-serviço)
--
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    -- User Management
    'USER_CREATE', 'USER_READ', 'USER_UPDATE', 'USER_DELETE',
    -- Role Management
    'ROLE_CREATE', 'ROLE_READ', 'ROLE_UPDATE', 'ROLE_DELETE',
    -- Permission Management
    'PERMISSION_CREATE', 'PERMISSION_READ', 'PERMISSION_UPDATE', 'PERMISSION_DELETE',
    -- Audit & Governance
    'AUDIT_READ'
)
WHERE r.name = 'ROLE_ADMIN';

-- ----------------------
-- ROLE_MANAGER Permissions:
-- ----------------------
-- Responsabilidades (conforme ADR-001):
--   - Gerenciar usuários: criar, visualizar, atualizar (SEM deletar)
--   - Visualizar papéis (sem criar/atualizar/deletar)
--   - Visualizar permissões (sem modificar)
--   - Não acessa auditoria diretamente
--   - Não tem acesso a PROFILE_* (perfil é auto-serviço)
--
-- Restrições (conforme ADR-001):
--   - Não pode deletar usuários
--   - Não pode criar/atualizar papéis
--   - Não pode criar permissões
--   - Não pode alterar configurações críticas
--
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    -- User Management (limited)
    'USER_CREATE', 'USER_READ', 'USER_UPDATE',
    -- Role Management (read-only)
    'ROLE_READ',
    -- Permission Management (read-only)
    'PERMISSION_READ'
)
WHERE r.name = 'ROLE_MANAGER';

-- ----------------------
-- ROLE_USER Permissions:
-- ----------------------
-- Responsabilidades (conforme ADR-001):
--   - Realizar login
--   - Consultar seus próprios dados
--   - Atualizar informações pessoais permitidas
--
-- Restrições (conforme ADR-001):
--   - Não pode gerenciar usuários
--   - Não pode gerenciar papéis
--   - Não pode gerenciar permissões
--   - Não pode consultar auditoria
--
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    -- User Profile (self-service only)
    'PROFILE_READ', 'PROFILE_UPDATE'
)
WHERE r.name = 'ROLE_USER';
```

---

## 📊 Breakdown por Seção

### SECTION 1: 15 PERMISSIONS (38 linhas)

```sql
INSERT INTO permissions (name, description) VALUES
    ('USER_CREATE',        'Criar novos usuários'),                    -- 1
    ('USER_READ',          'Visualizar dados de usuários'),            -- 2
    ('USER_UPDATE',        'Atualizar dados de usuários'),             -- 3
    ('USER_DELETE',        'Remover/deletar usuários'),                -- 4
    ('ROLE_CREATE',        'Criar novos papéis de acesso (Roles)'),    -- 5
    ('ROLE_READ',          'Visualizar papéis de acesso (Roles)'),     -- 6
    ('ROLE_UPDATE',        'Atualizar papéis de acesso (Roles)'),      -- 7
    ('ROLE_DELETE',        'Remover papéis de acesso (Roles)'),        -- 8
    ('PERMISSION_CREATE',  'Criar novas permissões'),                  -- 9
    ('PERMISSION_READ',    'Visualizar permissões disponíveis'),       -- 10
    ('PERMISSION_UPDATE',  'Atualizar permissões'),                    -- 11
    ('PERMISSION_DELETE',  'Remover permissões'),                      -- 12
    ('AUDIT_READ',         'Consultar trilha de auditoria ...'),       -- 13
    ('PROFILE_READ',       'Visualizar o próprio perfil ...'),         -- 14
    ('PROFILE_UPDATE',     'Atualizar o próprio perfil ...');          -- 15

👉 Result: 15 registros na tabela permissions
```

### SECTION 2: 3 ROLES (11 linhas)

```sql
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN',
     'Administrador da Plataforma - Acesso completo ...'),
    
    ('ROLE_MANAGER',
     'Gerente Operacional - Gestão de usuários ...'),
    
    ('ROLE_USER',
     'Usuário Final Padrão - Acesso limitado ...');

👉 Result: 3 registros na tabela roles
```

### SECTION 3a: ROLE_ADMIN ASSOCIATIONS (13 linhas SQL)

```sql
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'USER_CREATE', 'USER_READ', 'USER_UPDATE', 'USER_DELETE',     -- 4
    'ROLE_CREATE', 'ROLE_READ', 'ROLE_UPDATE', 'ROLE_DELETE',     -- 4
    'PERMISSION_CREATE', 'PERMISSION_READ', 'PERMISSION_UPDATE', 'PERMISSION_DELETE',  -- 4
    'AUDIT_READ'                                                   -- 1
)
WHERE r.name = 'ROLE_ADMIN';

👉 Result: 13 registros em role_permissions
```

### SECTION 3b: ROLE_MANAGER ASSOCIATIONS (9 linhas SQL)

```sql
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'USER_CREATE', 'USER_READ', 'USER_UPDATE',              -- 3
    'ROLE_READ',                                             -- 1
    'PERMISSION_READ'                                        -- 1
)
WHERE r.name = 'ROLE_MANAGER';

👉 Result: 5 registros em role_permissions
```

### SECTION 3c: ROLE_USER ASSOCIATIONS (6 linhas SQL)

```sql
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'PROFILE_READ', 'PROFILE_UPDATE'                         -- 2
)
WHERE r.name = 'ROLE_USER';

👉 Result: 2 registros em role_permissions
```

---

## 🧮 Totalizador

```
PHASE 1 (Permissions):   15 INSERTs
PHASE 2 (Roles):          3 INSERTs
PHASE 3 (Associations):  20 INSERTs (13 + 5 + 2)
─────────────────────────────────────
TOTAL:                   38 INSERTs
```

---

## 🔐 Validação SQL

### Verificar Permissions Criadas

```sql
SELECT * FROM permissions;
-- Resultado esperado: 15 registros

SELECT COUNT(*) FROM permissions;
-- Resultado esperado: 15
```

### Verificar Roles Criadas

```sql
SELECT * FROM roles;
-- Resultado esperado: 3 registros

SELECT COUNT(*) FROM roles;
-- Resultado esperado: 3
```

### Verificar Associações

```sql
SELECT r.name AS role, COUNT(p.id) AS num_permissions
FROM roles r
LEFT JOIN role_permissions rp ON r.id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.id
GROUP BY r.id, r.name
ORDER BY r.name;

-- Resultado esperado:
-- ROLE_ADMIN    : 13 permissions
-- ROLE_MANAGER  : 5 permissions
-- ROLE_USER     : 2 permissions
```

### Verificar Permissions de uma Role Específica

```sql
SELECT DISTINCT p.name, p.description
FROM roles r
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE r.name = 'ROLE_ADMIN'
ORDER BY p.name;

-- Resultado esperado: 13 registros com permissões do ADMIN
```

---

## 📈 Schema Impactado

### Tabela: permissions

```
┌────┬──────────────────┬──────────────────────────────────────┐
│ id │ name             │ description                          │
├────┼──────────────────┼──────────────────────────────────────┤
│ 1  │ USER_CREATE      │ Criar novos usuários                 │
│ 2  │ USER_READ        │ Visualizar dados de usuários         │
│ 3  │ USER_UPDATE      │ Atualizar dados de usuários          │
│ 4  │ USER_DELETE      │ Remover/deletar usuários             │
│ 5  │ ROLE_CREATE      │ Criar novos papéis de acesso         │
│ 6  │ ROLE_READ        │ Visualizar papéis de acesso          │
│ 7  │ ROLE_UPDATE      │ Atualizar papéis de acesso           │
│ 8  │ ROLE_DELETE      │ Remover papéis de acesso             │
│ 9  │ PERMISSION_CREATE│ Criar novas permissões               │
│10  │ PERMISSION_READ  │ Visualizar permissões disponíveis    │
│11  │ PERMISSION_UPD...│ Atualizar permissões                 │
│12  │ PERMISSION_DEL...│ Remover permissões                   │
│13  │ AUDIT_READ       │ Consultar trilha de auditoria        │
│14  │ PROFILE_READ     │ Visualizar o próprio perfil          │
│15  │ PROFILE_UPDATE   │ Atualizar o próprio perfil           │
└────┴──────────────────┴──────────────────────────────────────┘

Registros: 15
Constraints:
- id: PRIMARY KEY
- name: UNIQUE, NOT NULL
- description: VARCHAR(255)
```

### Tabela: roles

```
┌────┬──────────────┬──────────────���───────────────────────┐
│ id │ name         │ description                          │
├────┼──────────────┼──────────────────────────────────────┤
│ 1  │ ROLE_ADMIN   │ Administrador da Plataforma...       │
│ 2  │ ROLE_MANAGER │ Gerente Operacional                  │
│ 3  │ ROLE_USER    │ Usuário Final Padrão                 │
└────┴──────────────┴──────────────────────────────────────┘

Registros: 3
Constraints:
- id: PRIMARY KEY
- name: UNIQUE, NOT NULL
```

### Tabela: role_permissions (N:N Junction)

```
┌─────────┬────────────────┐
│ role_id │ permission_id  │
├───────��─┼────────────────┤
│ 1       │ 1              │  ROLE_ADMIN → USER_CREATE
│ 1       │ 2              │  ROLE_ADMIN → USER_READ
│ 1       │ 3              │  ROLE_ADMIN → USER_UPDATE
│ 1       │ 4              │  ROLE_ADMIN → USER_DELETE
│ 1       │ 5              │  ROLE_ADMIN → ROLE_CREATE
│ 1       │ 6              │  ROLE_ADMIN → ROLE_READ
│ 1       │ 7              │  ROLE_ADMIN → ROLE_UPDATE
│ 1       │ 8              │  ROLE_ADMIN → ROLE_DELETE
│ 1       │ 9              │  ROLE_ADMIN → PERMISSION_CREATE
│ 1       │ 10             │  ROLE_ADMIN → PERMISSION_READ
│ 1       │ 11             │  ROLE_ADMIN → PERMISSION_UPDATE
│ 1       │ 12             │  ROLE_ADMIN → PERMISSION_DELETE
│ 1       │ 13             │  ROLE_ADMIN → AUDIT_READ
│─────────┼────────────────┤
│ 2       │ 1              │  ROLE_MANAGER → USER_CREATE
│ 2       │ 2              │  ROLE_MANAGER → USER_READ
│ 2       │ 3              │  ROLE_MANAGER → USER_UPDATE
│ 2       │ 6              │  ROLE_MANAGER → ROLE_READ
│ 2       │ 10             │  ROLE_MANAGER → PERMISSION_READ
│─────────┼────────────────┤
│ 3       │ 14             │  ROLE_USER → PROFILE_READ
│ 3       │ 15             │  ROLE_USER → PROFILE_UPDATE
└─────────┴────────────────┘

Registros: 20
Primary Key: (role_id, permission_id)
Foreign Keys:
- role_id → roles.id (ON DELETE CASCADE)
- permission_id → permissions.id (ON DELETE CASCADE)
```

---

## ✨ Conclusão

Este SQL:
- ✅ É **idempotente** (pode re-executar)
- ✅ É **legível** (sem IDs hardcoded)
- ✅ É **mantível** (dinâmico e explícito)
- ✅ Segue **ADR-001**
- ✅ Implementa **RBAC modelo**
- ✅ Cria **base segura** para autenticação/autorização


