-- Fase 4: Adição de campos faltantes na tabela users
-- Campos adicionados conforme entity User.java e requisitos ADR-001/RN-003

-- Adiciona coluna 'name' para armazenar o nome completo do usuário
ALTER TABLE users
ADD COLUMN name VARCHAR(255) NOT NULL DEFAULT 'Unknown';

-- Adiciona coluna 'locked' para rastreamento de usuários bloqueados (RN-003)
-- Usuários bloqueados (locked=true) não podem realizar login
ALTER TABLE users
ADD COLUMN locked BOOLEAN NOT NULL DEFAULT FALSE;

-- Remove a constraint DEFAULT após os dados serem populados
ALTER TABLE users
ALTER COLUMN name DROP DEFAULT;

-- Cria índices para melhorar performance
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_username ON users (username);

