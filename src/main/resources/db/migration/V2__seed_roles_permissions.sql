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
