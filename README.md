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
- Arquitetura em camadas inspirada em Clean Architecture, com Presenter
  dedicado (`adapter/in/presenter`) separando a formatacao da resposta
  HTTP do UseCase (ver [ARCHITECTURE.md](ARCHITECTURE.md))

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

## Instruções de Execução

### Pré-requisitos
- JDK 17 ou superior (o Maven Wrapper baixa o Maven sozinho)
- Docker rodando
- Infraestrutura de banco já no ar (ver repositório
  [infra-databases-revenda-veiculos](https://github.com/eduardofrauches/infra-databases-revenda-veiculos), passo anterior)

### 1. Clonar o repositório
```bash
git clone https://github.com/eduardofrauches/sistema-principal-veiculos.git
cd sistema-principal-veiculos
```

### 2. Subir os bancos (se ainda não tiver feito)
Clone [infra-databases-revenda-veiculos](https://github.com/eduardofrauches/infra-databases-revenda-veiculos) como pasta irmã
(mesmo nível) deste repositório e siga o README de lá.

### 3. Rodar a aplicação
```bash
./mvnw spring-boot:run
```

A aplicação sobe na porta `8081` e conecta no PostgreSQL do
container `revenda-postgres-core`
(`localhost:5432/veiculos_core_db`, ver `application.yml`). O schema
é criado automaticamente (`ddl-auto: update`).

## Kubernetes

Manifests Kustomize em `k8s/` (`base/` + `overlays/local/`, para uso
com Minikube). **Importante:** o banco de dados (`postgres-core`) **nao
esta neste repositorio** — ele vive em um repositorio separado,
[infra-databases-revenda-veiculos](https://github.com/eduardofrauches/infra-databases-revenda-veiculos),
porque a infraestrutura de banco e compartilhada e nao pertence a
nenhum dos dois microsservicos individualmente. **Aplique os
manifests desse repositorio antes de subir este servico no cluster**
— sem o banco no ar, os pods deste Deployment nunca ficam `Ready`
(falham a `readinessProbe`/`livenessProbe` em `/actuator/health`).

```bash
# 1. Bancos (repositorio infra-databases-revenda-veiculos)
kubectl apply -k .
kubectl rollout status statefulset/postgres-core --timeout=120s

# 2. Este servico (a partir deste repositorio)
minikube image load sistema-principal-veiculos:local
kubectl apply -k k8s/overlays/local
```

## Testado manualmente

Fluxo ponta a ponta validado com `curl` junto com o
`servico-vendas-veiculos` (cadastro -> sincronizacao -> venda -> webhook
de pagamento -> status final refletido de volta aqui). Ver
[ARCHITECTURE.md](ARCHITECTURE.md) para os detalhes arquiteturais.

## Testando manualmente (exemplos de requisicao)

Exemplos prontos para os 3 endpoints deste servico (porta `8081`).

> **Atencao (Windows/PowerShell):** o `curl` do PowerShell e um apelido
> (alias) para `Invoke-WebRequest` e nao aceita a sintaxe `-H`/`-d` do
> curl tradicional. Chamar `curl.exe` diretamente tambem pode falhar,
> porque o parsing de linha de comando do Windows reinterpreta as aspas
> do JSON antes de repassar ao programa — o servidor acaba recebendo um
> JSON corrompido e devolve `400`/`500`. **No PowerShell, use sempre
> `Invoke-RestMethod`**, como nos exemplos abaixo. Em Linux/Mac/Git Bash,
> os exemplos com `curl` funcionam normalmente.

### Cadastrar veiculo (`POST /veiculos`)

```bash
curl -X POST http://localhost:8081/veiculos \
  -H "Content-Type: application/json" \
  -d '{"marca":"Fiat","modelo":"Uno","ano":2020,"cor":"Branco","preco":35000.00,"placa":"ABC1D23"}'
```

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/veiculos" -ContentType "application/json" -Body '{"marca":"Fiat","modelo":"Uno","ano":2020,"cor":"Branco","preco":35000.00,"placa":"ABC1D23"}'
```

### Editar veiculo (`PUT /veiculos/{id}`)

```bash
curl -X PUT http://localhost:8081/veiculos/1 \
  -H "Content-Type: application/json" \
  -d '{"marca":"Fiat","modelo":"Uno","ano":2020,"cor":"Prata","preco":34000.00,"placa":"ABC1D23"}'
```

```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:8081/veiculos/1" -ContentType "application/json" -Body '{"marca":"Fiat","modelo":"Uno","ano":2020,"cor":"Prata","preco":34000.00,"placa":"ABC1D23"}'
```

### Atualizar status (`PATCH /veiculos/{id}/status`) — uso interno, chamado pelo servico-vendas-veiculos

```bash
curl -X PATCH http://localhost:8081/veiculos/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"RESERVADO"}'
```

```powershell
Invoke-RestMethod -Method Patch -Uri "http://localhost:8081/veiculos/1/status" -ContentType "application/json" -Body '{"status":"RESERVADO"}'
```

*(valores possiveis de `status`: `DISPONIVEL`, `RESERVADO`, `VENDIDO`)*

### Respostas de erro possiveis

| Situacao | Status |
|---|---|
| Campo obrigatorio faltando ou invalido (ex: `preco` <= 0) | `400` |
| Corpo da requisicao ausente ou JSON mal formado | `400` |
| Veiculo nao encontrado | `404` |
| Editar veiculo ja vendido / transicao de status invalida | `409` |
| Metodo HTTP nao suportado nessa rota | `405` |

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
  Os UseCases devolvem a Entity de dominio `Veiculo` (nao mais o DTO de
  resposta), entao as asserções verificam a Entity retornada.
- **Presenter** (`adapter/in/presenter/veiculo/VeiculoPresenterTest.java`):
  cobre a conversao `Veiculo` -> `VeiculoResponse`, isolada do UseCase.
- **Integracao** (`adapter/out/veiculo/persistence/jpa/repository/VeiculoRepositoryAdapterIT.java`):
  `@DataJpaTest` contra Postgres real (Testcontainers, nao H2).

**Ultima medicao:** 12 testes, 0 falhas, cobertura de linha ~71%
(controller e o client HTTP nao tem teste dedicado aqui — cobertos
indiretamente no teste BDD do `servico-vendas-veiculos`, que exercita o
fluxo completo entre os dois servicos).

## Em construcao

Este projeto ja sobe de verdade, tem o fluxo principal funcionando,
testes unitarios + de integracao, Dockerfile, pipeline de CI/CD e
manifests Kubernetes. Ainda faltam, para as proximas etapas:

- [ ] Teste BDD (Cucumber) tambem aqui, ou MockMvc para o `VeiculoController` (hoje sem cobertura direta).
- [ ] Migrations versionadas (Flyway/Liquibase) em vez de `ddl-auto: update`.
- [ ] Push da imagem Docker para um registry (Docker Hub/GHCR) — hoje o estagio `docker` do CI so builda localmente.
- [ ] Overlay Kubernetes para nuvem (`k8s/overlays/aws` ou equivalente) — hoje so existe `overlays/local`.
- [ ] Gerenciamento de segredos de verdade (Sealed Secrets, Vault, External Secrets) no lugar do `Secret` placeholder.
- [ ] Resiliencia mais robusta na chamada HTTP ao servico-vendas (retry/circuit breaker) — hoje uma falha so gera um log de warning.
