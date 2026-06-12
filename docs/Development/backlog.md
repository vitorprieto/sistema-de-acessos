# Product Backlog

## Introdução

Este documento organiza as funcionalidades do Sistema de Gestão de Acessos em Épicos, Features e Tasks.

O objetivo é fornecer um roadmap claro para desenvolvimento incremental do produto, permitindo entregas contínuas e redução de riscos.

As prioridades seguem a classificação:

- P0 → Crítico (necessário para funcionamento do sistema)
- P1 → Importante (agrega valor significativo)
- P2 → Evolução futura

---

# Roadmap

## Fase 1 - Foundation

Objetivo:

Preparar toda a infraestrutura necessária para o desenvolvimento do sistema.

Prioridade:

P0

---

### FEATURE 1.1 - Configuração do Projeto

#### TASKS

- Configurar Java 21
- Configurar Spring Boot 3.5
- Configurar Maven
- Configurar Lombok
- Configurar Profiles (dev, test, prod)
- Configurar application.yml

---

### FEATURE 1.2 - Banco de Dados

#### TASKS

- Configurar PostgreSQL
- Configurar Spring Data JPA
- Configurar Flyway
- Criar primeira migration
- Configurar datasource

---

### FEATURE 1.3 - Tratamento Global

#### TASKS

- Criar GlobalExceptionHandler
- Criar ErrorResponse
- Padronizar mensagens de erro

---

### FEATURE 1.4 - Observabilidade

#### TASKS

- Configurar logs
- Configurar SLF4J
- Configurar Logback

---

# Fase 2 - Segurança Base

Objetivo:

Preparar a infraestrutura de autenticação e autorização.

Prioridade:

P0

---

### FEATURE 2.1 - Spring Security

#### TASKS

- Criar SecurityConfig
- Configurar SecurityFilterChain
- Configurar PasswordEncoder
- Configurar AuthenticationManager

---

### FEATURE 2.2 - JWT

#### TASKS

- Criar JwtService
- Criar geração de token
- Criar validação de token
- Criar extração de claims

---

### FEATURE 2.3 - Refresh Token

#### TASKS

- Criar entidade RefreshToken
- Criar repository
- Criar service
- Criar fluxo de renovação

---

# Fase 3 - RBAC

Objetivo:

Implementar o modelo de autorização.

Prioridade:

P0

---

### FEATURE 3.1 - Role

#### TASKS

- Criar entidade Role
- Criar repository
- Criar service
- Criar controller

---

### FEATURE 3.2 - Permission

#### TASKS

- Criar entidade Permission
- Criar repository
- Criar service
- Criar controller

---

### FEATURE 3.3 - Associação Role x Permission

#### TASKS

- Criar relacionamento N:N
- Criar endpoints de associação
- Criar validações

---

# Fase 4 - Gestão de Usuários

Objetivo:

Implementar o gerenciamento de usuários.

Prioridade:

P0

---

### FEATURE 4.1 - Cadastro de Usuários

#### TASKS

- Criar entidade User
- Criar DTOs
- Criar repository
- Criar service
- Criar controller

---

### FEATURE 4.2 - Consulta de Usuários

#### TASKS

- Buscar usuário por ID
- Buscar usuário por e-mail
- Listar usuários

---

### FEATURE 4.3 - Atualização de Usuários

#### TASKS

- Atualizar dados cadastrais
- Atualizar status

---

### FEATURE 4.4 - Bloqueio de Usuários

#### TASKS

- Bloquear usuário
- Desbloquear usuário

---

### FEATURE 4.5 - Associação de Roles

#### TASKS

- Associar role ao usuário
- Remover role do usuário

---

# Fase 5 - Autenticação

Objetivo:

Disponibilizar autenticação completa.

Prioridade:

P0

---

### FEATURE 5.1 - Login

#### TASKS

- Criar endpoint de login
- Validar credenciais
- Gerar JWT
- Gerar Refresh Token

---

### FEATURE 5.2 - Logout

#### TASKS

- Invalidar Refresh Token
- Registrar auditoria

---

### FEATURE 5.3 - Renovação de Token

#### TASKS

- Criar endpoint refresh
- Validar Refresh Token
- Gerar novo Access Token

---

# Fase 6 - Autorização

Objetivo:

Restringir acesso aos recursos.

Prioridade:

P0

---

### FEATURE 6.1 - Proteção de Endpoints

#### TASKS

- Configurar roles
- Configurar permissions
- Configurar autorização

---

### FEATURE 6.2 - Controle por Permissão

#### TASKS

- Utilizar @PreAuthorize
- Criar regras de acesso

---

# Fase 7 - Auditoria

Objetivo:

Garantir rastreabilidade.

Prioridade:

P1

---

### FEATURE 7.1 - Auditoria de Login

#### TASKS

- Registrar autenticações
- Registrar falhas de login

---

### FEATURE 7.2 - Auditoria Administrativa

#### TASKS

- Registrar alterações de usuários
- Registrar alterações de roles
- Registrar alterações de permissões

---

### FEATURE 7.3 - Consulta de Auditoria

#### TASKS

- Criar endpoint de consulta
- Criar filtros

---

# Fase 8 - Documentação

Objetivo:

Documentar a API.

Prioridade:

P1

---

### FEATURE 8.1 - OpenAPI

#### TASKS

- Configurar Swagger
- Documentar endpoints
- Documentar DTOs

---

# Fase 9 - Testes

Objetivo:

Garantir qualidade.

Prioridade:

P1

---

### FEATURE 9.1 - Testes Unitários

#### TASKS

- Services
- Security
- JWT

---

### FEATURE 9.2 - Testes de Integração

#### TASKS

- Controllers
- Banco de dados
- Segurança

---

### FEATURE 9.3 - Cobertura

#### TASKS

- Garantir cobertura mínima de 80%

---

# Fase 10 - Evoluções Futuras

Objetivo:

Preparar o roadmap de crescimento.

Prioridade:

P2

---

### FEATURE 10.1 - OAuth2

#### TASKS

- Implementar Authorization Server
- Integração OAuth2

---

### FEATURE 10.2 - OpenID Connect

#### TASKS

- Implementar OIDC

---

### FEATURE 10.3 - Single Sign-On

#### TASKS

- Implementar SSO

---

### FEATURE 10.4 - Keycloak

#### TASKS

- Integrar com Keycloak

---

### FEATURE 10.5 - Multi-Tenant

#### TASKS

- Isolamento por tenant
- Controle de tenants

---

# Definição de Pronto (Definition of Done)

Uma funcionalidade será considerada concluída quando:

- Código implementado.
- Testes unitários criados.
- Testes executados com sucesso.
- Documentação atualizada.
- Revisão realizada.
- Build executado com sucesso.
- Sem vulnerabilidades críticas.