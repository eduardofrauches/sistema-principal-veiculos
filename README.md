# sistema-principal-veiculos

Servico responsavel pelo cadastro e edicao de veiculos — a fonte de
verdade (dados-mestre) da plataforma de revenda de veiculos. Faz parte
de uma arquitetura de dois servicos independentes; o outro,
[servico-vendas-veiculos](../servico-vendas-veiculos), cuida das
listagens e da venda propriamente dita.

## O que ele faz

- Cadastra e edita veiculos (`marca`, `modelo`, `ano`, `cor`, `preco`, `placa`).
- Mantem o status do veiculo (`DISPONIVEL`, `RESERVADO`, `VENDIDO`)
  coerente, recebendo callbacks do `servico-vendas-veiculos` quando uma
  venda muda de estado.
- Sincroniza (via HTTP) uma copia resumida de cada veiculo com o
  `servico-vendas-veiculos`, para que ele possa listar/vender sem
  depender deste servico estar no ar.

## Stack

- Java 17
- Spring Boot 3.5.x (Web, Data JPA, Validation)
- PostgreSQL
- Lombok
- Arquitetura em camadas inspirada em Clean Architecture (ver [CLAUDE.md](CLAUDE.md))

## Endpoints

| Metodo | Rota | Descricao |
|---|---|---|
| `POST` | `/veiculos` | Cadastra um veiculo novo |
| `PUT` | `/veiculos/{id}` | Edita um veiculo existente (bloqueado se `VENDIDO`, 409) |
| `PATCH` | `/veiculos/{id}/status` | Callback do servico-vendas-veiculos para atualizar status |

Erros seguem um formato padrao (`ErrorResponse`): `400` para validacao
(Bean Validation) ou dado de dominio invalido, `404` para recurso nao
encontrado, `409` para conflito de estado (ex.: editar veiculo ja
vendido, transicao de status invalida).

## Como rodar

1. Suba a infraestrutura de banco (na raiz do repo, `C:\Dev\revenda-veiculos`):
   ```bash
   docker compose up -d
   ```
2. Rode a aplicacao:
   ```bash
   ./mvnw spring-boot:run
   ```

A aplicacao sobe na porta `8081` e conecta no PostgreSQL do container
`revenda-postgres-core` (`localhost:5432/veiculos_core_db`, ver
`application.yml`). O schema e criado automaticamente
(`ddl-auto: update`) — ainda nao ha migrations (Flyway/Liquibase).

## Testado manualmente

Fluxo ponta a ponta validado com `curl` junto com o
`servico-vendas-veiculos` (cadastro -> sincronizacao -> venda -> webhook
de pagamento -> status final refletido de volta aqui). Ver
[CLAUDE.md](CLAUDE.md) para os detalhes e o resumo de entrega do
repositorio para os comandos usados.

## Testes

```bash
./mvnw test
```

Requer Docker rodando (o teste de integracao sobe um Postgres real via
Testcontainers). Cobertura via JaCoCo em `target/site/jacoco/index.html`
apos rodar os testes — sem gate de minimo aqui (so o
`servico-vendas-veiculos` tem essa exigencia no enunciado).

- **Unitarios** (`application/veiculo/usecase/*Test.java`):
  `CadastrarVeiculoUseCaseTest`, `EditarVeiculoUseCaseTest`,
  `AtualizarStatusVeiculoUseCaseTest` — Mockito, mockando os `port/out`.
- **Integracao** (`adapter/out/veiculo/persistence/jpa/repository/VeiculoRepositoryAdapterIT.java`):
  `@DataJpaTest` contra Postgres real (Testcontainers, nao H2).

**Ultima medicao:** 11 testes, 0 falhas, cobertura de linha ~73%
(controller e o client HTTP nao tem teste dedicado aqui — cobertos
indiretamente no teste BDD do `servico-vendas-veiculos`, que exercita o
fluxo completo entre os dois servicos).

## Em construcao

Este projeto ja sobe de verdade, tem o fluxo principal funcionando e
testes unitarios + de integracao. Ainda faltam, para as proximas
etapas:

- [ ] Teste BDD (Cucumber) tambem aqui, ou MockMvc para o `VeiculoController` (hoje sem cobertura direta).
- [ ] Migrations versionadas (Flyway/Liquibase) em vez de `ddl-auto: update`.
- [ ] Dockerfile da aplicacao (o `docker-compose.yml` atual sobe so os bancos).
- [ ] Pipeline CI/CD.
- [ ] Manifests Kubernetes (Kustomize).
- [ ] Resiliencia mais robusta na chamada HTTP ao servico-vendas (retry/circuit breaker) — hoje uma falha so gera um log de warning.
