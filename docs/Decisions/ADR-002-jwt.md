# ADR-002 - Adoção de JWT (JSON Web Token) para Autenticação

## Status

Accepted ✅

---

## Data

2026-06-11

---

## Responsável pela Decisão

Vitor Prieto

---

## Contexto

O Sistema de Gestão de Acessos necessita de um mecanismo seguro, escalável e independente de sessão para autenticação de usuários.

A solução deverá permitir:

- Autenticação de usuários.
- Integração com múltiplas aplicações.
- Escalabilidade horizontal.
- Baixo acoplamento entre serviços.
- Compatibilidade com APIs REST.
- Facilidade de integração futura com microserviços.

Além disso, o sistema deverá ser preparado para futuras integrações com aplicações externas sem depender de sessões armazenadas no servidor.

Diante desse cenário, foi necessário avaliar diferentes estratégias de autenticação.

---

## Alternativas Avaliadas

### Alternativa 1 - Session-Based Authentication

Neste modelo, após o login, o servidor cria uma sessão e mantém o estado do usuário.

#### Fluxo

```text
Usuário
    ↓
Login
    ↓
Servidor cria sessão
    ↓
Session ID
    ↓
Próximas requisições
```

#### Vantagens

- Simples implementação.
- Fácil entendimento.
- Muito utilizado em aplicações monolíticas tradicionais.

#### Desvantagens

- Dependência de estado no servidor.
- Escalabilidade limitada.
- Dificulta arquiteturas distribuídas.
- Necessidade de replicação de sessões.
- Menor compatibilidade com APIs modernas.

---

### Alternativa 2 - JWT (JSON Web Token)

Neste modelo, após autenticação, o sistema gera um token assinado digitalmente contendo as informações necessárias para validação.

#### Fluxo

```text
Usuário
    ↓
Login
    ↓
JWT
    ↓
Requisições autenticadas
```

#### Vantagens

- Stateless.
- Escalabilidade elevada.
- Integração simples com APIs REST.
- Redução de carga no servidor.
- Fácil integração com microserviços.
- Amplamente adotado pelo mercado.

#### Desvantagens

- Revogação de tokens mais complexa.
- Necessidade de gestão adequada do tempo de expiração.
- Maior cuidado com armazenamento do token.

---

### Alternativa 3 - OAuth2 Authorization Server

Neste modelo, um servidor de autorização dedicado é responsável pela autenticação.

#### Vantagens

- Alto nível de segurança.
- Padrão amplamente utilizado.
- Ideal para ambientes corporativos complexos.

#### Desvantagens

- Complexidade elevada.
- Infraestrutura adicional.
- Sobrecarga para o MVP.

---

## Decisão

Foi adotado o modelo de autenticação baseado em JWT (JSON Web Token).

A autenticação seguirá uma abordagem stateless.

Após autenticação bem-sucedida:

1. O usuário informa suas credenciais.
2. O sistema valida as credenciais.
3. O sistema gera um Access Token JWT.
4. O sistema gera um Refresh Token.
5. O sistema retorna ambos ao cliente.

---

## Modelo de Autenticação Definido

### Access Token

Responsável pela autenticação das requisições.

Características:

- Curta duração.
- Contém informações básicas do usuário.
- Contém perfis (Roles).
- Assinado digitalmente.

#### Exemplo de Conteúdo

```json
{
  "sub": "123",
  "email": "usuario@email.com",
  "roles": ["ROLE_USER"],
  "iat": 1750000000,
  "exp": 1750003600
}
```

---

### Refresh Token

Responsável pela renovação do Access Token.

Características:

- Longa duração.
- Utilizado apenas para renovação.
- Pode ser revogado.
- Não será utilizado para acesso direto às APIs.

---

## Estratégia de Expiração

### Access Token

Tempo inicial previsto:

```text
15 minutos
```

Objetivo:

- Reduzir impacto em caso de vazamento.

---

### Refresh Token

Tempo inicial previsto:

```text
7 dias
```

Objetivo:

- Melhorar experiência do usuário.
- Reduzir necessidade de login frequente.

---

## Estrutura do Fluxo de Autenticação

### Login

```text
Usuário
    ↓
Email + Senha
    ↓
Validação
    ↓
JWT + Refresh Token
```

---

### Acesso às APIs

```text
Cliente
    ↓
Authorization: Bearer JWT
    ↓
Validação
    ↓
Acesso liberado
```

---

### Renovação do Token

```text
Refresh Token
    ↓
Validação
    ↓
Novo JWT
```

---

## Integração com Spring Security

O Spring Security será responsável por:

- Interceptar requisições.
- Validar JWT.
- Extrair permissões.
- Popular o contexto de segurança.
- Controlar autorização dos endpoints.

Componentes previstos:

```text
SecurityFilterChain

JwtAuthenticationFilter

JwtService

AuthenticationService

TokenRepository
```

---

## Justificativa da Decisão

JWT foi escolhido porque:

- É amplamente utilizado em APIs REST.
- Permite arquitetura stateless.
- Facilita escalabilidade horizontal.
- Possui excelente integração com Spring Security.
- É adequado para futura adoção de microserviços.
- Atende plenamente os requisitos do MVP.

Além disso, o uso combinado de Refresh Token reduz impactos relacionados à expiração curta dos Access Tokens.

---

## Consequências

### Impactos Positivos

- Escalabilidade.
- Baixo acoplamento.
- Menor consumo de recursos do servidor.
- Integração simples com aplicações externas.
- Compatibilidade com arquiteturas modernas.

---

### Impactos Negativos

- Revogação de tokens mais complexa.
- Necessidade de armazenamento seguro do Refresh Token.
- Necessidade de monitoramento de tokens expirados.

---

## Impacto na Arquitetura

Serão criados os seguintes componentes:

```text
AuthenticationController

AuthenticationService

JwtService

JwtAuthenticationFilter

RefreshTokenService
```

Entidades previstas:

```text
User

RefreshToken
```

Tabela prevista:

```text
refresh_tokens
```

---

## Segurança

As seguintes práticas deverão ser adotadas:

- Senhas armazenadas com BCrypt.
- JWT assinado com chave segura.
- Tokens com tempo de vida reduzido.
- Refresh Tokens revogáveis.
- HTTPS obrigatório em produção.
- Proteção contra brute force.
- Auditoria de autenticações.

---

## Revisão Futura

Após a estabilização do MVP deverá ser avaliada a adoção de:

- OAuth2 Authorization Server.
- OpenID Connect (OIDC).
- Single Sign-On (SSO).
- Integração com Keycloak.
- Integração com Identity Providers corporativos.

A revisão será realizada conforme a evolução do produto e necessidades do negócio.