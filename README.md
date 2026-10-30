# Desafio Full Stack — Tela de Notas

Aplicação para consultar e gerenciar notas operacionais. O frontend em Next.js consome a API REST em Spring Boot; a API persiste as notas em PostgreSQL. A interface segue o protótipo e as cores de `materials/Oxyn - Desafio Full Stack/materials/ui-design.pdf`.

## Estrutura

```text
backend/notes-api/
  src/main/java/com/oxyn/notes_api/
    note/controller/       Endpoints REST
    note/dto/              Contratos de entrada e saída
    note/usecase/          Listagem, CRUD e exportação
    note/specification/    Filtros de consulta
    note/entity/           Entidade JPA
    note/repository/       Persistência PostgreSQL
    note/mapper/           Conversão entre entidade e DTO
    shared/                Tratamento de erros
  src/main/java/db/migration/  Carga inicial via Flyway
  src/main/resources/db/migration/  Criação da tabela via Flyway
  postman/             Coleção de exemplos da API
  compose.yaml         Container do PostgreSQL
frontend/
  src/app/             Página, providers e rotas intermediárias da API
  src/features/notes/  Tela, estilos, hooks, serviço HTTP, tipos e validação
materials/Oxyn - Desafio Full Stack/materials/
  notes.csv            Dados originais da carga inicial
  ui-design.pdf        Referência visual
```

O navegador chama as rotas `/api/notes` do Next.js. Elas validam as requisições e as encaminham para `/api/v1/notes` do Spring Boot, usando `NOTES_API_URL`. O backend executa os casos de uso e consulta o banco. O frontend não lê `notes.csv` diretamente.

## Requisitos para executar

- Java 17 e Maven Wrapper (incluído em `backend/notes-api`)
- Node.js 20 ou superior e npm
- Docker com Docker Compose para o PostgreSQL

### 1. Banco de dados

Na raiz do repositório:

```bash
docker compose -f backend/notes-api/compose.yaml up -d
```

O banco fica disponível em `localhost:5434`. O Compose atual contém **somente o PostgreSQL**; a API e o frontend são executados localmente pelos comandos abaixo. A aplicação cria a tabela e importa o CSV automaticamente ao iniciar pela primeira vez.

### 2. Backend

Em outro terminal:

```bash
cd backend/notes-api
./mvnw spring-boot:run
```

A API fica em `http://localhost:8080/api/v1`. Por padrão, ela usa `jdbc:postgresql://localhost:5434/notes_db`, usuário `notes` e senha `notes`. Para outra instância do banco, configure `DB_URL`, `DB_USER` e `DB_PASSWORD` no ambiente antes de iniciar.

### 3. Frontend

Em um terceiro terminal:

```bash
cd frontend
cp .env.example .env.local
npm ci
npm run dev
```

Acesse `http://localhost:3000`. Se a porta estiver ocupada, use `npm run dev -- -p 3001` e acesse `http://localhost:3001`. O arquivo `.env.local` define `NOTES_API_URL=http://localhost:8080/api/v1/`; ajuste o valor caso a API use outra URL.

## Carga inicial

O Flyway executa `V1__create_notes_table.sql` para criar a tabela e `V2__seed_notes.java` para importar as 100 notas de `materials/notes.csv`. O Maven incorpora esse CSV ao pacote do backend durante o build. A migração preserva os UUIDs do arquivo, ignora registros com IDs já existentes e não apaga notas criadas anteriormente. Como a migração é versionada, ela não repete a importação em cada inicialização; depois da carga, as consultas da API usam apenas o PostgreSQL.

Para conferir a carga:

```bash
curl 'http://localhost:8080/api/v1/notes?page=0&size=5'
```

Em um banco novo, a resposta deve informar `totalElements: 100`. Em um banco já usado, o total pode ser maior.

## Endpoints da API

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/v1/notes` | Lista com filtros opcionais `site`, `equipment`, `startDate`, `endDate` e paginação `page`, `size` |
| `POST` | `/api/v1/notes` | Cria uma nota |
| `GET` | `/api/v1/notes/{id}` | Busca uma nota por UUID |
| `PUT` | `/api/v1/notes/{id}` | Atualiza uma nota |
| `DELETE` | `/api/v1/notes/{id}` | Exclui uma nota |
| `GET` | `/api/v1/notes/export` | Baixa todas as notas dos filtros em CSV UTF-8, sem paginação |

`page` começa em zero; `size` aceita de 1 a 100. As datas dos filtros usam `YYYY-MM-DD` e abrangem os dias informados em UTC. Para criar ou atualizar uma nota, envie JSON com `site`, `equipment`, `variable`, `timestamp` (ISO 8601), `author` e `message`. Todos são obrigatórios. A coleção [Postman](backend/notes-api/postman/notes-api.postman_collection.json) contém exemplos de requisição e resposta, incluindo a exportação.

Exemplo de listagem filtrada:

```bash
curl 'http://localhost:8080/api/v1/notes?equipment=Gerador&startDate=2024-01-01&endDate=2024-12-31&page=0&size=5'
```

Exemplo de exportação:

```bash
curl -OJ 'http://localhost:8080/api/v1/notes/export?equipment=Gerador'
```

## Verificações

```bash
cd backend/notes-api && ./mvnw test
cd frontend && npm run lint && npm run build
```

Os testes de integração do backend usam o PostgreSQL configurado em `application.properties`, então o container do banco precisa estar disponível durante `./mvnw test`.
