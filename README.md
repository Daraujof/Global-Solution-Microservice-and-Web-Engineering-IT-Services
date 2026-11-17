# Tô no Direito — API REST (Spring Boot + SQL Server)

> **"O direito já existe. A gente ajuda a chegar até você."**

Global Solution 2026 · FIAP · **Microservice and Web Engineering & IT Services** (Turma 3SIR · Prof. Antonio Carlos de Lima Júnior)

| Integrante | RM |
|---|---|
| Felipe Teixeira | 556100 |
| Gustavo Oda | 556754 |
| Maria Eduarda | 554593 |

## Links da entrega

| Evidência | Link |
|---|---|
| API no Azure (Swagger na raiz) | https://tonodireito-api-9dd995.azurewebsites.net |
| Imagem no Docker Hub | https://hub.docker.com/r/dudinhaaraujoo/to-no-direito-api |
| Workflow (GitHub Actions) | https://github.com/Daraujof/Global-Solution-Microservice-and-Web-Engineering-IT-Services/actions |

## 1. Objetivo da solução

No Brasil existem **32,1 mi de pessoas idosas**, **18,6 mi de pessoas com deficiência** e **7,7 mi de famílias** que têm direito ao desconto na conta de luz e não usam. O direito existe — falta chegar até a pessoa.

O **Tô no Direito** é um app simples (funciona até por voz) em que a pessoa responde algumas perguntas e descobre quais direitos provavelmente tem, recebe um plano de ação (por onde começar, o que levar, onde ir) e acompanha cada pedido.

Este repositório é o **microsserviço/API REST** que sustenta a solução, com persistência em **Microsoft SQL Server**:

| Entidade (tabela) | Papel no app |
|---|---|
| **Direito** (`direitos`) | Cada direito é um dado, com a **lei/fonte oficial** por trás (BPC, Tarifa Social, Carteira da Pessoa Idosa…) |
| **Documento** (`documentos`) | "O que levar": documentos necessários para pedir cada direito e onde emitir |
| **Triagem** (`triagens`) | Respostas **anônimas** da triagem — faixa etária e faixa de renda, nunca nome/CPF/valores exatos (LGPD: *"Regras no banco. Pessoas, nunca."*) |
| **Pedido** (`pedidos`) | Acompanhamento: `QUERO_PEDIR → JA_PEDI → AGUARDANDO_RESPOSTA → CONSEGUI / NEGADO` |

**Relacionamentos:** `Direito 1—N Documento` · `Direito 1—N Pedido` · `Triagem 1—N Pedido` (uma triagem não pode ter dois pedidos do mesmo direito).

## 2. Arquitetura

```
Controller (HTTP, /api/v1)  →  Service (regras de negócio)  →  Repository (Spring Data JPA)  →  SQL Server
      ↑ Request DTO / Response DTO (ModelMapper)          ↑ Entity (JPA)
```

```
src/main/java/br/com/fiap/tonodireito
├── controller/   DireitoController, DocumentoController, TriagemController, PedidoController, HomeController (/ → Swagger)
├── service/      regras de negócio (nenhuma Controller acessa Repository)
├── repository/   interfaces JpaRepository
├── entity/       Direito, Documento, Triagem, Pedido + enums
├── dto/request/  DTOs de entrada com Bean Validation
├── dto/response/ DTOs de saída + ErrorResponse padronizado
├── exception/    GlobalExceptionHandler (400/404/409/500 sem stack trace)
└── config/       ModelMapper, OpenAPI, carga inicial de dados
```

**Stack:** Java 21 · Spring Boot 3.5 · Spring Data JPA/Hibernate · Bean Validation · ModelMapper · springdoc-openapi (Swagger UI) · Microsoft SQL Server (mssql-jdbc) · Docker · GitHub Actions · Azure App Service for Containers + Azure SQL.

### Regras de negócio (camada Service)
- Nome de direito é único (409 se repetido).
- Direito com pedidos vinculados não pode ser excluído (409) — pode ser desativado com `ativo=false`.
- Direito inativo não aceita novos pedidos (400).
- Pedido com status diferente de `QUERO_PEDIR` exige `dataSolicitacao` (400); datas não podem estar no futuro.
- Uma triagem não pode ter dois pedidos do mesmo direito (409).
- Triagem é anônima; UF normalizada em maiúsculas. Excluir a triagem exclui seus pedidos; excluir o direito exclui seus documentos.

## 3. Endpoints (versão v1)

Base: `/api/v1` · Documentação interativa: **`/`** (redireciona para `/swagger-ui/index.html`) · OpenAPI JSON: `/v3/api-docs`

| Recurso | POST (201) | GET lista (200) | GET /{id} (200) | PUT /{id} (200) | DELETE /{id} (204) |
|---|---|---|---|---|---|
| Direitos | `/api/v1/direitos` | `/api/v1/direitos` | ✔ | ✔ | ✔ |
| Documentos | `/api/v1/documentos` | `/api/v1/documentos?direitoId=` | ✔ | ✔ | ✔ |
| Triagens | `/api/v1/triagens` | `/api/v1/triagens` | ✔ | ✔ | ✔ |
| Pedidos | `/api/v1/pedidos` | `/api/v1/pedidos?status=&triagemId=` | ✔ | ✔ | ✔ |

**Códigos HTTP:** `201 Created` (com header `Location`) · `200 OK` · `204 No Content` · `400 Bad Request` (validação, JSON mal formado, regra de negócio) · `404 Not Found` (recurso ou relacionamento inexistente) · `409 Conflict` (duplicidade/vínculos) · `500` genérico sem detalhes internos.

Exemplo de erro padronizado:
```json
{
  "timestamp": "2026-11-16T20:51:58-03:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos na requisição",
  "caminho": "/api/v1/direitos",
  "campos": [ { "campo": "nome", "mensagem": "O nome é obrigatório" } ]
}
```

## 4. Executar localmente

### 4.1 SQL Server (Docker)
```bash
docker network create tnd
docker run -d --name sqlserver --network tnd -p 1433:1433 \
  -e ACCEPT_EULA=Y -e "MSSQL_SA_PASSWORD=<SuaSenhaForte#1>" \
  mcr.microsoft.com/mssql/server:2022-latest

# criar o banco
docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa -P "<SuaSenhaForte#1>" \
  -Q "CREATE DATABASE tonodireito"
```
As tabelas são criadas automaticamente pelo Hibernate (`ddl-auto=update`) e 3 direitos de exemplo são carregados na primeira execução.

### 4.2 Spring Boot (Maven)
Configuração por variáveis de ambiente (nenhuma senha no repositório):

| Variável | Exemplo |
|---|---|
| `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=tonodireito;encrypt=true;trustServerCertificate=true` |
| `DB_USERNAME` | `sa` |
| `DB_PASSWORD` | `<SuaSenhaForte#1>` |
| `PORT` (opcional) | `8080` |

```bash
export DB_URL="jdbc:sqlserver://localhost:1433;databaseName=tonodireito;encrypt=true;trustServerCertificate=true"
export DB_USERNAME=sa
export DB_PASSWORD="<SuaSenhaForte#1>"
mvn spring-boot:run          # ou: mvn clean package && java -jar target/app.jar
```
Acesse http://localhost:8080 → Swagger UI.

Testes automatizados (H2 em modo SQL Server): `mvn test`

### 4.3 Docker
```bash
docker build -t to-no-direito-api .
docker run -d --name api --network tnd -p 8080:8080 \
  -e "DB_URL=jdbc:sqlserver://sqlserver:1433;databaseName=tonodireito;encrypt=true;trustServerCertificate=true" \
  -e DB_USERNAME=sa -e "DB_PASSWORD=<SuaSenhaForte#1>" \
  to-no-direito-api
```

## 5. Docker Hub

- Repositório: **https://hub.docker.com/r/dudinhaaraujoo/to-no-direito-api**
- Imagem: `dudinhaaraujoo/to-no-direito-api`
- Tags: `latest`, `1.0.<nº da execução do workflow>` e `sha-<commit>`

```bash
docker pull dudinhaaraujoo/to-no-direito-api:latest
docker run -p 8080:8080 -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... dudinhaaraujoo/to-no-direito-api:latest
```

## 6. Continuous Delivery (GitHub Actions)

Workflow: [`.github/workflows/cd-dockerhub.yml`](.github/workflows/cd-dockerhub.yml) — dispara automaticamente a cada **push na branch `main`** (e manualmente via *Run workflow*):

1. Checkout → JDK 21
2. `mvn clean package` (compila e roda os testes)
3. Login no Docker Hub com os secrets `DOCKERHUB_USERNAME` e `DOCKERHUB_TOKEN`
4. Build da imagem (Dockerfile multi-stage) e push com as tags `latest`, `1.0.N` e `sha-xxxxxxx`

Histórico de execuções: https://github.com/Daraujof/Global-Solution-Microservice-and-Web-Engineering-IT-Services/actions

## 7. Implantação no Azure

- **Azure App Service for Containers** (Linux, plano B1) rodando a imagem `dudinhaaraujoo/to-no-direito-api:latest` do Docker Hub
- **Azure SQL Database** (Microsoft SQL Server, banco `tonodireito`), com firewall liberado para serviços do Azure
- Conexão via App Settings `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (não versionadas) e `WEBSITES_PORT=8080`

**URL da API:** https://tonodireito-api-9dd995.azurewebsites.net

Toda a infraestrutura é criada pelo script [`azure/deploy.sh`](azure/deploy.sh), executado no **Azure Cloud Shell**:
```bash
curl -sL https://raw.githubusercontent.com/Daraujof/Global-Solution-Microservice-and-Web-Engineering-IT-Services/main/azure/deploy.sh | bash -s -- <usuario-dockerhub>
```
Para atualizar a aplicação após um novo push (nova imagem `latest`): `az webapp restart -g rg-tonodireito -n <nome-do-app>`.

## 8. Como testar

**Pelo Swagger:** abra a URL da API (a raiz `/` abre o Swagger UI) e use *Try it out*. Sugestão de roteiro:

1. `GET /api/v1/direitos` → 200 com os direitos de exemplo (BPC, Tarifa Social, Carteira da Pessoa Idosa)
2. `POST /api/v1/triagens` → 201
   ```json
   {"faixaEtaria":"DE_65_A_79","faixaRendaPerCapita":"ATE_1_4_SM","inscritoCadUnico":"NAO","temDeficiencia":"NAO_SEI","pessoasNaCasa":2,"municipio":"São Paulo","uf":"SP"}
   ```
3. `POST /api/v1/pedidos` → 201 `{"status":"QUERO_PEDIR","triagemId":1,"direitoId":1}`
4. `PUT /api/v1/pedidos/{id}` → 200 `{"status":"AGUARDANDO_RESPOSTA","dataSolicitacao":"2026-11-10","protocolo":"INSS-2026-000123","triagemId":1,"direitoId":1}`
5. `POST /api/v1/documentos` → 201 `{"nome":"Conta de luz recente","obrigatorio":true,"ondeEmitir":"Distribuidora","direitoId":2}`
6. Erros: `GET /api/v1/direitos/999` → 404 · `POST /api/v1/direitos` com `{}` → 400 · `DELETE /api/v1/direitos/1` com pedido vinculado → 409
7. `DELETE /api/v1/pedidos/{id}` → 204

**Pelo terminal (roteiro completo de CRUD):**
```bash
bash scripts/teste-api.sh https://tonodireito-api-9dd995.azurewebsites.net
```

**Persistência no SQL Server:** os dados criados continuam disponíveis após reiniciar o container (`az webapp restart ...`) e podem ser consultados no *Query editor* do banco `tonodireito` no portal do Azure:
```sql
SELECT * FROM direitos; SELECT * FROM documentos; SELECT * FROM triagens; SELECT * FROM pedidos;
```
