# CLAUDE.md — sistema-principal-veiculos

Contexto para o Claude Code manter coerencia entre sessoes futuras
neste repositorio. Ver tambem o documento de arquitetura original em
`arquitetura-revenda-veiculos.md` (na maquina do usuario, fora deste
repo) para a visao completa dos dois servicos.

## O que este servico e

Um dos dois microsservicos da plataforma de revenda de veiculos.
Responsavel pelo cadastro/edicao de veiculos (dados-mestre). O outro
servico, `servico-vendas-veiculos` (repo irmao, em
`../servico-vendas-veiculos`), cuida de listagens e vendas. Eles nunca
compartilham banco — toda comunicacao entre eles e HTTP.

## Build e comandos

```bash
./mvnw clean compile      # compilar
docker compose up -d      # na raiz do repo: sobe o Postgres deste servico e o do outro
./mvnw spring-boot:run    # rodar (porta 8081) — precisa do Postgres acima no ar
./mvnw test                # quando houver testes
```

## Estrutura de pastas (Clean Architecture)

```
src/main/java/com/revendaveiculos/sistemaprincipal/
├── adapter/
│   ├── in/
│   │   ├── controller/veiculo/VeiculoController    <- REST controller, depende so de application/port/in e do Presenter
│   │   ├── presenter/veiculo/VeiculoPresenter       <- formata a Entity de dominio devolvida pelo UseCase em VeiculoResponse
│   │   └── exception/GlobalExceptionHandler         <- @RestControllerAdvice, traduz exceptions -> HTTP
│   └── out/veiculo/
│       ├── persistence/jpa/{entity,mapper,repository}/   <- VeiculoEntity, VeiculoEntityMapper, VeiculoJpaRepository + VeiculoRepositoryAdapter (implementa VeiculoRepositoryPort)
│       └── client/                                       <- VendasServiceHttpAdapter (implementa VendasServicePort via RestTemplate)
├── application/
│   └── veiculo/
│       ├── dto/{request,response}/     <- DTOs da camada de aplicacao (records, com Bean Validation)
│       ├── mapper/                     <- DTO <-> domain (VeiculoMapper)
│       ├── port/
│       │   ├── in/                     <- interfaces que os UseCases implementam; controllers dependem delas
│       │   └── out/                    <- interfaces que os UseCases consomem; adapters/out implementam
│       └── usecase/                    <- implementa port/in, depende de port/out
├── domain/
│   ├── model/veiculo/                  <- Veiculo (entidade pura, regras de transicao de status), StatusVeiculo
│   ├── vo/                             <- Preco (Value Object imutavel, valida > 0)
│   └── exception/                      <- exceptions de dominio (RuntimeException)
└── infrastructure/config/veiculo/      <- RestTemplateConfig (bean RestTemplate usado pelo client HTTP)
```

## Decisoes arquiteturais

1. **Clean Architecture com porta de entrada/saida explicitas**,
   seguindo o padrao do repositorio de referencia do grupo
   (`oficina-service-mvp`, Fases 1-2) — nao o padrao
   controller→service→repository simplificado usado (por engano) na
   Fase 4 anterior. Controllers dependem so de `port/in`; UseCases
   implementam `port/in` e dependem de `port/out`; adapters de
   persistencia/HTTP implementam `port/out`.
1a. **Presenter dedicado** (`adapter/in/presenter/veiculo/VeiculoPresenter`),
   adicionado apos auditoria de Clean Architecture que apontou a
   ausencia dessa camada como lacuna de um feedback de trabalho
   anterior do curso. Os `port/in` (`CadastrarVeiculoInputPort`,
   `EditarVeiculoInputPort`, `AtualizarStatusVeiculoInputPort`) agora
   devolvem a Entity de dominio `Veiculo`, nao mais `VeiculoResponse`;
   os UseCases nao conhecem o formato de resposta HTTP. O Controller e
   quem chama `VeiculoPresenter.apresentar(veiculo)` para obter o DTO,
   so entao montando o `ResponseEntity`. Local escolhido:
   `adapter/in/presenter/` (mesmo nivel de `adapter/in/controller/`) —
   e a mesma camada de "Interface Adapters" do Clean Architecture, so
   que responsavel por formatar a saida em vez de receber a entrada.
   `VeiculoMapper` (`application/veiculo/mapper`) manteve so
   `paraDominio` (request -> Entity); a conversao Entity -> Response
   (que antes vivia em `VeiculoMapper.paraResponse`, chamada de dentro
   do UseCase) foi so movida para o Presenter, sem reescrever logica.
   Isso e uma mudanca estrutural (onde a formatacao acontece), nao uma
   mudanca de regra de negocio — a suite de testes inteira permanece
   verde depois da migracao (ver secao Testes).
2. **Domain model 100% livre de anotacao JPA.** `Veiculo` e
   `StatusVeiculo` (em `domain/model/veiculo`) sao objetos Java puros.
   `VeiculoEntity` (`adapter/out/veiculo/persistence/jpa/entity`) e a
   entidade JPA separada; `VeiculoEntityMapper` converte nos dois
   sentidos. `VeiculoEntity.id` usa `@GeneratedValue(IDENTITY)` — quem
   gera o id e este servico (dono do dado-mestre).
3. **Value Object `Preco`** (`domain/vo/Preco`) encapsula a regra "preco
   > 0" e arredondamento para 2 casas decimais. Imutavel, com factory
   `Preco.de(...)` que lanca `PrecoInvalidoException`.
4. **Maquina de estados do veiculo** vive dentro da propria entidade
   `Veiculo` (metodo privado `transicionarPara`, tabela
   `TRANSICOES_VALIDAS`): `DISPONIVEL -> RESERVADO -> VENDIDO`, com
   `RESERVADO -> DISPONIVEL` para cancelamento. Nenhuma transicao sai de
   `VENDIDO`. Veiculo vendido nao pode ser editado
   (`atualizarDados` lanca `TransicaoStatusInvalidaException`).
5. **`AtualizarStatusVeiculoUseCase`** existe porque o
   `servico-vendas-veiculos` precisa notificar este servico quando o
   status de uma venda muda. Exposto via `PATCH /veiculos/{id}/status`
   — rota interna, nao documentada publicamente no doc de arquitetura
   original mas necessaria para o fluxo funcionar. **Importante:** o
   `servico-vendas-veiculos` chama essa rota tanto na reserva
   (`PENDENTE -> RESERVADO`) quanto no resultado final
   (`PAGAMENTO_APROVADO -> VENDIDO` / `PAGAMENTO_CANCELADO -> DISPONIVEL`)
   — sem o passo intermediario de reserva, a maquina de estados daqui
   rejeitaria o salto direto `DISPONIVEL -> VENDIDO` com 409. Isso foi
   um bug real encontrado no teste manual ponta a ponta e corrigido no
   `EfetuarVendaUseCase` do outro servico (ver CLAUDE.md dele).
6. **`VendasServiceHttpAdapter`** (implementa `VendasServicePort`) usa
   `RestTemplate` (bean configurado em
   `infrastructure/config/veiculo/RestTemplateConfig`) e a URL vem de
   `application.yml` (`servico-vendas.base-url`), nunca hardcoded. Uma
   falha de rede so gera um log de `warn` — nao derruba o
   cadastro/edicao local, pois o outro servico foi desenhado para
   funcionar com uma copia local desatualizada.
7. **Bean Validation** nos DTOs de request
   (`CadastrarVeiculoRequest`, `EditarVeiculoRequest`,
   `AtualizarStatusVeiculoRequest`) com `@NotBlank`, `@NotNull`,
   `@Positive`, `@Min(1900)`. `GlobalExceptionHandler`
   (`adapter/in/exception`) traduz: `MethodArgumentNotValidException` e
   exceptions de dominio invalido -> `400`; `VeiculoNaoEncontradoException`
   -> `404`; `TransicaoStatusInvalidaException` -> `409`.
8. **Persistencia**: `ddl-auto: update` (sem Flyway/Liquibase ainda) —
   decisao pragmatica para esta etapa inicial, documentada como pendencia.
9. **`application.yml`** aponta para o PostgreSQL do container
   `revenda-postgres-core` (`veiculos_core_db`, porta `5432` do host) e
   define a porta HTTP do servico (`8081`) e a URL base do
   `servico-vendas-veiculos`. O `docker-compose.yml` que sobe esse
   banco fica na **raiz do repositorio**
   (`C:\Dev\revenda-veiculos\docker-compose.yml`), nao dentro deste
   projeto — ele sobe os dois bancos (deste servico e do
   `servico-vendas-veiculos`) de uma vez.

## Testado manualmente (fluxo ponta a ponta)

Validado com `docker compose up -d` + `mvn spring-boot:run` nos dois
servicos + `curl`: cadastro de veiculo aqui -> sincronizacao automatica
no `servico-vendas-veiculos` -> venda la -> reserva refletida aqui
(`RESERVADO`) -> webhook de pagamento aprovado -> status final
`VENDIDO` refletido aqui. Tambem validados os erros `400` (preco
invalido), `404` (veiculo inexistente) e `409` (transicao de status
invalida / editar veiculo vendido). Ver o resumo de entrega na raiz do
repositorio para os comandos `curl` exatos e as respostas recebidas.

## Testes

- **Unitarios** (`src/test/.../application/veiculo/usecase/*Test.java`):
  JUnit 5 + Mockito, mockando `port/out` — sem Spring, sem banco.
  Cobrem caminho feliz + erros de negocio (nao encontrado, transicao
  invalida / editar veiculo vendido, preco invalido).
- **Integracao** (`*RepositoryAdapterIT.java`, sufixo `IT` de proposito):
  `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` +
  Testcontainers (`postgres:16-alpine` de verdade, nao H2).
- **Surefire configurado para rodar tambem os `*IT`** na fase `test`
  (nao so os `*Test`) — decisao deliberada para que `mvnw test` sozinho
  rode a suite inteira, sem precisar de `mvnw verify`/Failsafe.
- **JaCoCo** (`jacoco-maven-plugin`) mede cobertura em todo `mvnw test`
  (relatorio em `target/site/jacoco/index.html`). **Sem gate de minimo
  aqui** — a exigencia de 80% do enunciado e so para o
  `servico-vendas-veiculos` (ver CLAUDE.md dele). Exclusoes do calculo:
  `*Application`, `infrastructure/config/**`, `dto/**`,
  `persistence/jpa/{entity,mapper}/**`, `domain/exception/**`,
  `adapter/in/exception/**` (boilerplate sem logica de negocio).
- **Ultima medicao:** 12 testes (9 unitarios + 3 integracao), 0
  falhas, ~71% de cobertura de linha (`mvnw verify`, com Docker/
  Testcontainers no ar). Inclui `VeiculoPresenterTest`, adicionado
  junto com o `VeiculoPresenter` (ver decisao arquitetural 1a).
  `VeiculoController` e `VendasServiceHttpAdapter` ficam sem teste
  direto aqui — sao exercitados indiretamente pelo teste BDD do
  `servico-vendas-veiculos` (que roda o fluxo completo entre os dois
  servicos) e por teste manual (ver secao acima).

## O que ainda falta (proximas etapas)

Migrations versionadas, Dockerfile da aplicacao, CI/CD, Kubernetes,
resiliencia mais robusta na chamada HTTP (retry/circuit breaker), teste
BDD ou MockMvc proprio para o `VeiculoController`. Ver checklist
detalhado no README.md.
