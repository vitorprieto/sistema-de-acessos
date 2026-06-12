# Arquitetura do Sistema

## Introdução

Este documento descreve a arquitetura do Sistema de Gestão de Acessos.

O objetivo é fornecer uma visão técnica da solução, definindo componentes, camadas, módulos, integrações e decisões arquiteturais que servirão como base para a implementação.

A arquitetura foi projetada para atender aos requisitos de:

- Segurança
- Escalabilidade
- Manutenibilidade
- Governança
- Auditabilidade

---

# Visão Geral da Arquitetura

O sistema será desenvolvido utilizando uma arquitetura de Monólito Modular (Modular Monolith).

Essa abordagem foi escolhida por oferecer:

- Simplicidade operacional
- Facilidade de desenvolvimento
- Baixo custo de manutenção
- Evolução futura para microserviços

---

## Arquitetura Geral

```text
Cliente
    ↓
REST API
    ↓
Controllers
    ↓
Services
    ↓
Repositories
    ↓
PostgreSQL
```

Cross-Cutting Concerns:

```text
Security
Validation
Audit
Exception Handling
Logging
```

---

# Estilo Arquitetural

## Modular Monolith

O sistema será organizado em módulos independentes dentro da mesma aplicação.

Cada módulo possuirá:

- Controller
- Service
- Repository
- DTOs
- Entidades

Objetivos:

- Separação de responsabilidades.
- Baixo acoplamento.
- Alta coesão.
- Facilidade de manutenção.

---

# Tecnologias

## Backend

- Java 21
- Spring Boot 3.5.x
- Spring Security
- Spring Data JPA
- Hibernate
- Maven

---

## Banco de Dados

- PostgreSQL

---

## Versionamento de Banco

- Flyway

---

## Segurança

- JWT
- BCrypt
- RBAC

---

## Documentação

- OpenAPI
- Swagger

---

## Testes

- JUnit 5
- Mockito
- Spring Boot Test

---

# Estrutura de Pacotes

```text
com.sistema.acesso.auth
│
├── config
├── security
├── common
│
├── auth
├── user
├── role
├── permission
├── audit
└── token
```

---

# Módulos do Sistema

## Auth Module

Responsável por:

- Login
- Logout
- Refresh Token
- Geração de JWT

Principais componentes:

```text
AuthenticationController
AuthenticationService
JwtService
RefreshTokenService
```

---

## User Module

Responsável por:

- Cadastro de usuários
- Consulta de usuários
- Atualização de usuários
- Bloqueio de usuários

Principais componentes:

```text
UserController
UserService
UserRepository
```

---

## Role Module

Responsável por:

- Cadastro de perfis
- Associação de usuários

Principais componentes:

```text
RoleController
RoleService
RoleRepository
```

---

## Permission Module

Responsável por:

- Cadastro de permissões
- Associação de permissões aos perfis

Principais componentes:

```text
PermissionController
PermissionService
PermissionRepository
```

---

## Audit Module

Responsável por:

- Registro de autenticações
- Registro de operações administrativas
- Consulta de auditorias

Principais componentes:

```text
AuditController
AuditService
AuditRepository
```

---

# Camadas da Aplicação

## Controller Layer

Responsável por:

- Exposição das APIs REST.
- Validação de entrada.
- Conversão de DTOs.

Não deve conter regra de negócio.

---

## Service Layer

Responsável por:

- Regras de negócio.
- Orquestração de processos.
- Validações funcionais.

Toda regra de negócio deverá ficar nesta camada.

---

## Repository Layer

Responsável por:

- Persistência de dados.
- Consultas ao banco.

Implementado através do Spring Data JPA.

---

## Database Layer

Responsável pelo armazenamento dos dados.

Tecnologia:

```text
PostgreSQL
```

---

# Modelo de Domínio

## User

Representa um usuário autenticável.

Principais atributos:

```text
id
name
email
password
enabled
createdAt
updatedAt
```

---

## Role

Representa um perfil de acesso.

Principais atributos:

```text
id
name
description
```

---

## Permission

Representa uma permissão específica.

Principais atributos:

```text
id
name
description
```

---

## RefreshToken

Representa tokens de renovação.

Principais atributos:

```text
id
token
expiresAt
revoked
```

---

## AuditLog

Representa eventos auditáveis.

Principais atributos:

```text
id
action
resource
userId
timestamp
```

---

# Modelo RBAC

Relacionamentos:

```text
User N:N Role

Role N:N Permission
```

Fluxo:

```text
User
 ↓
Role
 ↓
Permission
 ↓
Endpoint
```

---

# Arquitetura de Segurança

## Autenticação

Baseada em:

```text
JWT
```

Fluxo:

```text
Email + Senha
        ↓
Autenticação
        ↓
JWT
        ↓
Requisições autenticadas
```

---

## Autorização

Baseada em:

```text
RBAC
```

Perfis iniciais:

```text
ROLE_ADMIN
ROLE_MANAGER
ROLE_USER
```

---

## Criptografia

Senhas serão armazenadas utilizando:

```text
BCrypt
```

---

# Banco de Dados

## Tabelas Principais

```text
users

roles

permissions

user_roles

role_permissions

refresh_tokens

audit_logs
```

---

# Tratamento de Exceções

Será implementado um tratamento global utilizando:

```text
@RestControllerAdvice
```

Objetivos:

- Padronização de erros.
- Facilidade de suporte.
- Melhor experiência para consumidores da API.

---

# Auditoria

Eventos auditáveis:

- Login
- Logout
- Cadastro de usuário
- Atualização de usuário
- Alteração de perfil
- Alteração de permissões

---

# Logging

Será utilizado:

```text
SLF4J
Logback
```

Objetivos:

- Monitoramento.
- Diagnóstico.
- Auditoria complementar.

---

# Estratégia de Testes

## Testes Unitários

Cobertura mínima:

```text
80%
```

Ferramentas:

- JUnit 5
- Mockito

---

## Testes de Integração

Objetivos:

- Validar APIs.
- Validar persistência.
- Validar segurança.

---

# Evolução Futura

A arquitetura foi projetada para suportar futuras evoluções:

- OAuth2
- OpenID Connect
- Keycloak
- SSO
- Multi-Tenant
- Microserviços

Sem necessidade de reestruturação significativa do domínio principal.

---

# Conclusão

A arquitetura proposta busca equilibrar simplicidade, segurança e escalabilidade.

A utilização de Java 21, Spring Boot, PostgreSQL, JWT, RBAC e Flyway fornece uma base sólida para construção de uma plataforma de autenticação e autorização corporativa preparada para crescimento futuro.