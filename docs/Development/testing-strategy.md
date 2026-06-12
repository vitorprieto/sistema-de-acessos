# Estratégia de Testes

## Objetivo

- Cobertura mínima de **80%** (gate automatizado via JaCoCo).
- **Testes unitários** para lógica de negócio (services) e componentes de segurança.
- **Testes de integração** para controllers, persistência e security chain — fase posterior.
- JUnit 5 + Mockito + AssertJ (via `spring-boot-starter-test`).

## Pirâmide

| Camada | Tipo | Ferramentas | Status |
|--------|------|-------------|--------|
| Domínio / lógica (services, security helpers) | Unitário | JUnit 5, Mockito, AssertJ | ✅ em uso |
| Repositories | Integração (`@DataJpaTest` + Testcontainers PostgreSQL) | — | ⏳ futuro |
| Controllers / Security filter chain | Integração (`@SpringBootTest` / `@WebMvcTest` + Testcontainers) | spring-security-test | ⏳ futuro |

## Convenções

- Teste espelha o pacote da classe sob teste (`src/test/java/.../<pacote>`).
- Nome: `<Classe>Test` (unitário), `<Classe>IT` (integração, fase futura).
- Unitário **não** sobe contexto Spring nem banco. Dependências mockadas com Mockito.
- Testes de integração que exigem PostgreSQL ficam desabilitados (`@Disabled`) até a configuração de Testcontainers; `AuthServiceApplicationTests` é o caso atual.

## JaCoCo

- Plugin no `pom.xml` (versão fixada).
- `prepare-agent` → instrumentação; `report` no fase `test`; `check` (gate 80% de linha, escopo BUNDLE) no fase `verify`.
- `./mvnw.cmd clean test` roda os testes e gera o relatório (`target/site/jacoco/`) sem aplicar o gate.
- `./mvnw.cmd verify` aplica o gate de 80% — falha o build se a cobertura cair.
- **Exclusões** (infra sem lógica, coberta por integração depois): `AuthServiceApplication`, pacote `config/**`, `JwtProperties`, `package-info`.
- `lombok.config` (`addLombokGeneratedAnnotation = true`) faz o JaCoCo ignorar getters/setters/construtores gerados pelo Lombok — só conta lógica escrita à mão.

## Comandos

```powershell
.\mvnw.cmd clean test     # testes + relatório de cobertura
.\mvnw.cmd verify         # testes + gate de 80%
.\mvnw.cmd test "-Dtest=JwtServiceTest"   # uma classe
```
