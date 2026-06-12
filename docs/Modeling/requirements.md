# Requisitos do Sistema

## Introdução

Este documento descreve os requisitos funcionais e não funcionais do Sistema de Gestão de Acessos, bem como os casos de uso, regras de negócio, restrições técnicas e premissas necessárias para a construção da solução.

O objetivo é garantir que os requisitos do produto estejam claramente definidos antes da fase de arquitetura e implementação.

---

# Requisitos Funcionais

## RF-001 - Cadastro de Usuário

O sistema deve permitir o cadastro de usuários informando:

- Nome completo
- E-mail
- Senha

O e-mail deve ser único na plataforma.

---

## RF-002 - Autenticação de Usuário

O sistema deve permitir que usuários autenticados realizem login utilizando:

- E-mail
- Senha

Após autenticação bem-sucedida, o sistema deverá gerar um Access Token JWT.

---

## RF-003 - Gerenciamento de Usuários

O sistema deve permitir:

- Consultar usuários cadastrados
- Atualizar dados cadastrais
- Bloquear usuários
- Desbloquear usuários
- Resetar senha

---

## RF-004 - Gerenciamento de Perfis (Roles)

O sistema deve permitir:

- Criar perfis
- Atualizar perfis
- Consultar perfis
- Remover perfis

---

## RF-005 - Associação de Perfis

O sistema deve permitir associar um ou mais perfis a um usuário.

---

## RF-006 - Gerenciamento de Permissões

O sistema deve permitir:

- Criar permissões
- Atualizar permissões
- Consultar permissões
- Remover permissões

---

## RF-007 - Associação de Permissões

O sistema deve permitir associar permissões a perfis.

---

## RF-008 - Controle de Acesso

O sistema deve restringir o acesso aos recursos com base nas permissões associadas ao usuário.

---

## RF-009 - Auditoria

O sistema deve registrar:

- Logins realizados
- Alterações de usuários
- Alterações de perfis
- Alterações de permissões

---

## RF-010 - Logout

O sistema deve permitir que usuários encerrem suas sessões.

---

# Requisitos Não Funcionais

## RNF-001 - Performance

O tempo médio de resposta das APIs deve ser inferior a 500ms.

---

## RNF-002 - Segurança

A autenticação deverá utilizar JWT.

---

## RNF-003 - Criptografia de Senhas

As senhas deverão ser armazenadas utilizando BCrypt.

---

## RNF-004 - Disponibilidade

O sistema deverá possuir disponibilidade mínima de 99%.

---

## RNF-005 - Escalabilidade

A solução deverá suportar crescimento futuro sem necessidade de reescrita da arquitetura.

---

## RNF-006 - Testabilidade

A cobertura mínima de testes deverá ser de 80%.

---

## RNF-007 - Documentação

Todas as APIs deverão ser documentadas utilizando OpenAPI/Swagger.

---

## RNF-008 - Banco de Dados

O sistema deverá utilizar PostgreSQL como banco de dados relacional.

---

# Casos de Uso

## UC-001 - Realizar Login

### Ator

Usuário

### Fluxo Principal

1. Usuário informa e-mail.
2. Usuário informa senha.
3. Sistema valida credenciais.
4. Sistema gera JWT.
5. Sistema retorna token.

### Fluxo Alternativo

3A. Credenciais inválidas.

4A. Sistema retorna erro de autenticação.

---

## UC-002 - Cadastrar Usuário

### Ator

Administrador

### Fluxo Principal

1. Administrador informa os dados do usuário.
2. Sistema valida os dados.
3. Sistema salva o usuário.
4. Sistema retorna confirmação.

---

## UC-003 - Associar Perfil ao Usuário

### Ator

Administrador

### Fluxo Principal

1. Administrador seleciona um usuário.
2. Administrador seleciona um perfil.
3. Sistema realiza a associação.
4. Sistema retorna confirmação.

---

## UC-004 - Associar Permissão ao Perfil

### Ator

Administrador

### Fluxo Principal

1. Administrador seleciona um perfil.
2. Administrador seleciona uma permissão.
3. Sistema realiza a associação.
4. Sistema retorna confirmação.

---

## UC-005 - Consultar Usuários

### Ator

Administrador

### Fluxo Principal

1. Administrador acessa a consulta.
2. Sistema retorna lista de usuários.

---

# Regras de Negócio

## RN-001

Todo usuário deve possuir pelo menos um perfil.

---

## RN-002

O e-mail do usuário deve ser único.

---

## RN-003

Usuários bloqueados não podem realizar login.

---

## RN-004

As senhas devem ser armazenadas utilizando BCrypt.

---

## RN-005

Permissões não podem ser associadas diretamente a usuários.

As permissões devem ser associadas exclusivamente aos perfis.

---

## RN-006

Apenas administradores podem gerenciar usuários, perfis e permissões.

---

## RN-007

Toda operação administrativa deve ser auditada.

---

# Restrições Técnicas

- Java 21
- Spring Boot 3.5.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- Lombok
- JWT

---

# Premissas

- Os usuários possuirão e-mail válido.
- O banco PostgreSQL estará disponível durante a operação do sistema.
- O sistema será inicialmente disponibilizado como API REST.

---

# Dependências

- PostgreSQL
- Maven Central
- Biblioteca JWT
- Spring Security
- Flyway