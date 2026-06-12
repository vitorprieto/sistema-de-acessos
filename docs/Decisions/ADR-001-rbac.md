# ADR-001 - Adoção do Modelo RBAC (Role-Based Access Control)

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

O Sistema de Gestão de Acessos necessita controlar quais funcionalidades e recursos cada usuário poderá acessar.

A solução deverá permitir:

- Controle centralizado de permissões.
- Facilidade de administração.
- Escalabilidade para crescimento futuro.
- Simplicidade na manutenção.
- Integração com múltiplas aplicações.
- Governança de acessos.
- Auditoria das operações realizadas pelos usuários.

O principal desafio consiste em definir uma estratégia de autorização que seja flexível o suficiente para atender às necessidades atuais do sistema, mas sem adicionar complexidade excessiva ao MVP.

Dessa forma, foi necessário avaliar diferentes modelos de controle de acesso antes da implementação da solução.

---

## Alternativas Avaliadas

### Alternativa 1 - ACL (Access Control List)

Neste modelo, cada recurso possui uma lista específica de usuários autorizados.

#### Exemplo

```text
Recurso: Relatório Financeiro

Usuário A -> Permitido
Usuário B -> Permitido
Usuário C -> Negado
```

#### Vantagens

- Controle granular.
- Fácil entendimento em cenários pequenos.
- Permite personalizações específicas por recurso.

#### Desvantagens

- Difícil manutenção em larga escala.
- Crescimento exponencial de regras.
- Elevado custo administrativo.
- Baixa escalabilidade.

---

### Alternativa 2 - RBAC (Role-Based Access Control)

Neste modelo, os usuários recebem perfis (Roles) e os perfis recebem permissões.

#### Exemplo

```text
Usuário
    ↓
Role
    ↓
Permissões
```

#### Vantagens

- Fácil administração.
- Alta reutilização de permissões.
- Escalabilidade.
- Facilidade de manutenção.
- Integração natural com Spring Security.
- Amplamente utilizado pelo mercado.

#### Desvantagens

- Menor flexibilidade quando comparado ao ABAC.
- Necessidade de governança para evitar excesso de perfis.

---

### Alternativa 3 - ABAC (Attribute-Based Access Control)

Neste modelo, as decisões de acesso são tomadas com base em atributos.

#### Exemplo

```text
Departamento = Financeiro
Cargo = Gerente
Horário = Comercial
Localização = Escritório
```

#### Vantagens

- Elevada flexibilidade.
- Controle extremamente granular.
- Permite regras complexas.

#### Desvantagens

- Implementação complexa.
- Maior curva de aprendizado.
- Maior esforço operacional.
- Complexidade desnecessária para o MVP.

---

## Decisão

Foi adotado o modelo RBAC (Role-Based Access Control).

A estrutura será composta por:

```text
User
  ↕
Role
  ↕
Permission
```

Relacionamentos:

```text
User N:N Role

Role N:N Permission
```

As permissões serão atribuídas exclusivamente aos perfis (Roles).

Usuários não possuirão permissões diretamente associadas.

Toda autorização será realizada através das permissões herdadas dos perfis associados ao usuário.

---

## Modelo RBAC Definido

### Entidades Principais

#### User

Representa os usuários autenticáveis do sistema.

Responsabilidades:

- Autenticação.
- Identificação.
- Associação de perfis.

---

#### Role

Representa grupos de responsabilidades.

Responsabilidades:

- Agrupar permissões.
- Facilitar administração de acessos.
- Reduzir duplicação de regras.

---

#### Permission

Representa uma ação específica que poderá ser executada no sistema.

Exemplos:

```text
USER_CREATE
USER_READ
USER_UPDATE
USER_DELETE
```

---

## Roles Iniciais do Sistema

Para a primeira versão serão disponibilizados os seguintes perfis padrão.

---

### ROLE_ADMIN

Perfil responsável pela administração completa da plataforma.

#### Permissões previstas

```text
USER_CREATE
USER_READ
USER_UPDATE
USER_DELETE

ROLE_CREATE
ROLE_READ
ROLE_UPDATE
ROLE_DELETE

PERMISSION_CREATE
PERMISSION_READ
PERMISSION_UPDATE
PERMISSION_DELETE

AUDIT_READ
```

#### Responsabilidades

- Gerenciar usuários.
- Gerenciar perfis.
- Gerenciar permissões.
- Consultar auditorias.
- Administrar a plataforma.

---

### ROLE_MANAGER

Perfil responsável pela gestão operacional dos usuários.

#### Permissões previstas

```text
USER_CREATE
USER_READ
USER_UPDATE

ROLE_READ

PERMISSION_READ
```

#### Responsabilidades

- Consultar usuários.
- Atualizar usuários.
- Associar perfis existentes.
- Consultar permissões.

#### Restrições

Não poderá:

- Excluir usuários.
- Criar perfis.
- Criar permissões.
- Alterar configurações críticas.

---

### ROLE_USER

Perfil padrão para usuários finais.

#### Permissões previstas

```text
PROFILE_READ
PROFILE_UPDATE
```

#### Responsabilidades

- Realizar login.
- Consultar seus próprios dados.
- Atualizar informações pessoais permitidas.

#### Restrições

Não poderá:

- Gerenciar usuários.
- Gerenciar perfis.
- Gerenciar permissões.
- Consultar auditorias.

---

## Estratégia de Permissões

As permissões seguirão o padrão:

```text
RECURSO_ACAO
```

Exemplos:

```text
USER_CREATE
USER_READ
USER_UPDATE
USER_DELETE

ROLE_CREATE
ROLE_READ
ROLE_UPDATE
ROLE_DELETE

PERMISSION_CREATE
PERMISSION_READ
PERMISSION_UPDATE
PERMISSION_DELETE

AUDIT_READ
```

Essa convenção foi escolhida por:

- Facilidade de leitura.
- Facilidade de manutenção.
- Integração com Spring Security.
- Facilidade de auditoria.
- Escalabilidade futura.

---

## Justificativa da Decisão

O modelo RBAC foi escolhido porque oferece o melhor equilíbrio entre:

- Simplicidade.
- Escalabilidade.
- Segurança.
- Governança.
- Facilidade de manutenção.

Além disso:

- É amplamente utilizado em aplicações corporativas.
- Possui integração nativa com Spring Security.
- Atende completamente aos requisitos do MVP.
- Permite evolução futura para modelos mais avançados.

---

## Consequências

### Impactos Positivos

- Centralização da gestão de permissões.
- Facilidade na administração de acessos.
- Redução da duplicidade de regras.
- Melhor governança.
- Maior rastreabilidade.
- Facilidade de integração com novas aplicações.
- Baixa complexidade de implementação.

---

### Impactos Negativos

- Possível aumento do número de perfis ao longo do tempo.
- Necessidade de governança para evitar sobreposição de permissões.
- Menor flexibilidade quando comparado ao ABAC.

---

## Impacto na Arquitetura

Entidades previstas:

```text
User
Role
Permission
AuditLog
```

Tabelas previstas:

```text
users

roles

permissions

user_roles

role_permissions

audit_logs
```

O Spring Security utilizará as permissões derivadas dos perfis associados aos usuários para controlar o acesso aos endpoints da aplicação.

---

## Revisão Futura

Após a estabilização do MVP deverá ser avaliada a necessidade de evolução para:

- RBAC Hierárquico.
- ABAC.
- Multi-Tenant RBAC.
- Integração com Identity Providers externos.

A revisão será realizada após a primeira versão produtiva do sistema.