# ADR-003 - Adoção do Flyway para Versionamento do Banco de Dados

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

O Sistema de Gestão de Acessos utilizará PostgreSQL como banco de dados principal.

Durante a evolução do produto, será necessário:

- Criar tabelas.
- Alterar estruturas existentes.
- Adicionar índices.
- Criar constraints.
- Executar correções estruturais.
- Garantir rastreabilidade das mudanças.

Além disso, o sistema deverá suportar múltiplos ambientes:

- Desenvolvimento
- Homologação
- Produção

Diante desse cenário, foi necessário definir uma estratégia segura para controle de versões do banco de dados.

---

## Problema

Sem um mecanismo de versionamento de banco de dados, surgem problemas como:

- Diferenças entre ambientes.
- Scripts executados manualmente.
- Falta de rastreabilidade.
- Dificuldade de rollback.
- Conflitos entre desenvolvedores.
- Erros em deploys.

O objetivo é garantir que toda mudança estrutural seja controlada, auditável e reproduzível.

---

## Alternativas Avaliadas

### Alternativa 1 - Hibernate DDL Auto

Configuração:

```properties
spring.jpa.hibernate.ddl-auto=update
```

#### Funcionamento

O Hibernate atualiza automaticamente o banco com base nas entidades JPA.

#### Vantagens

- Simplicidade.
- Rápida configuração.
- Boa opção para protótipos.

#### Desvantagens

- Baixa rastreabilidade.
- Ausência de histórico.
- Risco em produção.
- Difícil controle das alterações.
- Não recomendado para ambientes corporativos.

---

### Alternativa 2 - Scripts SQL Manuais

Neste modelo os scripts são executados manualmente pelos desenvolvedores.

#### Vantagens

- Controle total das alterações.
- Simplicidade conceitual.

#### Desvantagens

- Alto risco de erro humano.
- Dificuldade de sincronização.
- Ausência de automação.
- Baixa governança.

---

### Alternativa 3 - Flyway

Ferramenta de versionamento de banco de dados baseada em migrations.

#### Funcionamento

Cada alteração estrutural é registrada através de scripts versionados.

Exemplo:

```text
V1__create_users_table.sql

V2__create_roles_table.sql

V3__create_permissions_table.sql
```

O Flyway controla automaticamente quais scripts já foram executados.

#### Vantagens

- Controle de versão.
- Histórico completo.
- Integração nativa com Spring Boot.
- Reprodutibilidade.
- Facilidade de auditoria.
- Suporte corporativo.

#### Desvantagens

- Necessidade de disciplina na criação das migrations.
- Curva de aprendizado inicial.

---

## Decisão

Foi adotado o Flyway como ferramenta oficial de versionamento do banco de dados.

Todas as alterações estruturais deverão ser realizadas através de migrations versionadas.

Não será permitido:

```properties
spring.jpa.hibernate.ddl-auto=update
```

em ambientes corporativos.

O Hibernate será utilizado apenas para mapeamento ORM.

---

## Estratégia de Versionamento

As migrations seguirão o padrão:

```text
V<versão>__<descrição>.sql
```

Exemplos:

```text
V1__create_users_table.sql

V2__create_roles_table.sql

V3__create_permissions_table.sql

V4__create_user_roles_table.sql

V5__create_role_permissions_table.sql
```

---

## Estrutura Prevista

Diretório:

```text
src/main/resources/db/migration
```

Exemplo:

```text
db
└── migration
    ├── V1__create_users_table.sql
    ├── V2__create_roles_table.sql
    ├── V3__create_permissions_table.sql
    ├── V4__create_user_roles_table.sql
    ├── V5__create_role_permissions_table.sql
    └── V6__create_audit_logs_table.sql
```

---

## Fluxo de Desenvolvimento

### Nova Funcionalidade

1. Desenvolvedor cria migration.
2. Migration é versionada no Git.
3. Aplicação inicia.
4. Flyway verifica histórico.
5. Migration é executada automaticamente.
6. Histórico é registrado.

---

### Controle de Execução

O Flyway manterá controle através da tabela:

```text
flyway_schema_history
```

Tabela criada automaticamente.

Responsabilidades:

- Registrar migrations executadas.
- Controlar versões.
- Evitar execuções duplicadas.
- Garantir consistência entre ambientes.

---

## Integração com Spring Boot

Dependência prevista:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

Configuração:

```properties
spring.flyway.enabled=true
```

---

## Justificativa da Decisão

O Flyway foi escolhido porque:

- É amplamente adotado pelo mercado.
- Possui integração nativa com Spring Boot.
- Garante rastreabilidade.
- Facilita auditorias.
- Permite automação de deploys.
- Reduz riscos operacionais.
- Melhora a governança dos dados.

Além disso, atende às boas práticas de engenharia de software e DevOps.

---

## Consequências

### Impactos Positivos

- Histórico completo das alterações.
- Padronização dos ambientes.
- Automação de deploy.
- Maior governança.
- Melhor rastreabilidade.
- Facilidade de auditoria.

---

### Impactos Negativos

- Necessidade de disciplina na criação de migrations.
- Maior atenção em alterações destrutivas.
- Curva de aprendizado para novos desenvolvedores.

---

## Impacto na Arquitetura

Será criada a seguinte estrutura:

```text
src/main/resources/db/migration
```

A evolução das entidades:

```text
User
Role
Permission
RefreshToken
AuditLog
```

deverá ser acompanhada pelas respectivas migrations.

Nenhuma alteração estrutural deverá ser realizada diretamente no banco de dados.

---

## Boas Práticas Adotadas

### Obrigatório

- Toda alteração estrutural deve possuir migration.
- Toda migration deve ser versionada no Git.
- Toda migration deve possuir nome descritivo.

---

### Proibido

- Alterações manuais em produção.
- Uso de ddl-auto=update.
- Scripts sem versionamento.
- Alterações sem revisão.

---

## Revisão Futura

Após a evolução do produto, poderá ser avaliada a adoção de:

- Flyway Teams Edition.
- Estratégias de rollback automatizado.
- Pipelines CI/CD com validação automática de migrations.
- Versionamento avançado para múltiplos bancos.

A revisão ocorrerá conforme a maturidade da plataforma e das necessidades operacionais.