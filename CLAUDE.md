# CLAUDE.md

## Project

Sistema de Gestão de Acessos (Auth Service)

Objetivo:
Gerenciar autenticação, autorização e controle de acesso utilizando RBAC.

Tecnologias:

- Java 21
- Spring Boot 3.5.x
- PostgreSQL
- Spring Security
- JWT
- Flyway
- Maven
- Lombok
- JPA/Hibernate

## Architecture

Arquitetura:

- Modular Monolith
- Clean Architecture
- SOLID
- REST API

Estrutura:

com.sistema.acesso.auth_service

├── auth
├── user
├── role
├── permission
├── security
├── config
├── common
└── audit

## Domain

RBAC Model

User
Role
Permission

Relacionamentos:

User N:N Role
Role N:N Permission

## Development Rules

Sempre:

- Utilizar DTOs
- Utilizar Bean Validation
- Utilizar ResponseEntity
- Utilizar Service Layer
- Criar testes unitários
- Utilizar Flyway para migrations
- Utilizar Lombok

Nunca:

- Colocar regra de negócio em Controller
- Utilizar Entity diretamente na API
- Utilizar código duplicado
- Utilizar senha sem BCrypt

## Security

Autenticação:

- JWT Access Token
- Refresh Token

Autorização:

- ROLE_ADMIN
- ROLE_MANAGER
- ROLE_USER

## Commands

Windows

.\mvnw.cmd clean package
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run

Linux

./mvnw clean package
./mvnw test
./mvnw spring-boot:run