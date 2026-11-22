# API de Tarefas — Trabalho Individual | Aulas 10 e 11

**Autor:** Ivano Gabriel Silva Melo  
**Disciplina:** Back-End Frameworks — Ciência da Computação, 4º período  
**Tema:** Testes automatizados, JAR, Docker, PostgreSQL, logs e Actuator  
**Data:** 22/11/2026

> Este projeto é uma API didática separada do Nummo. Não contém código nem dados de produção do Nummo. O relatório de pesquisa está em [`relatorio/RELATORIO.md`](relatorio/RELATORIO.md).

## 1. Tecnologias e estrutura

Java 21; Spring Boot 3.5.7; Maven; Spring Web; Spring Data JPA; PostgreSQL 16; Jakarta Bean Validation; JUnit 5; Mockito; MockMvc; Spring Boot Actuator; Docker e Docker Compose.

- `entity/Tarefa.java`: entidade persistida com id e seis atributos adicionais (título, descrição, prioridade, concluída, criação e prazo).
- `dto/`: entradas validadas e respostas seguras da API.
- `controller/`: rotas HTTP e status de resposta.
- `service/`: regras de negócio (cadastro, listagem, busca, atualização, conclusão e exclusão).
- `repository/`: integração JPA com PostgreSQL.
- `exception/`: respostas 400 e 404.
- `src/test/`: 8 cenários unitários de Service com JUnit/Mockito e 4 de Controller com MockMvc.

### Rotas

| Método | Rota | Resultado esperado |
|---|---|---|
| POST | `/api/tarefas` | 201 Created + Location |
| GET | `/api/tarefas` | 200 OK |
| GET | `/api/tarefas/{id}` | 200 OK ou 404 Not Found |
| PUT | `/api/tarefas/{id}` | 200 OK ou 404 Not Found |
| PATCH | `/api/tarefas/{id}/concluir` | 200 OK ou 404 Not Found |
| DELETE | `/api/tarefas/{id}` | 204 No Content ou 404 Not Found |
| GET | `/actuator/health` | 200 UP quando os componentes monitorados estão saudáveis |

`POST` e `PUT` exigem título não vazio, prioridade entre `BAIXA`, `MEDIA` e `ALTA`, descrição de até 500 caracteres e título de até 120. `prazo` é opcional, no formato `AAAA-MM-DD`.

## 2. Pré-requisitos

- **Executar com Docker Compose:** Docker Desktop (Windows) ou Docker Engine com plugin Compose (Linux). Não é necessário instalar Java ou PostgreSQL na máquina.
- **Executar JAR fora da IDE:** JDK/JRE 21, Maven 3.9+ (para gerar o JAR) e PostgreSQL em execução.
- Porta 8080 disponível para a API e, para teste do JAR local, porta 5432 disponível para PostgreSQL.

## 3. Executar API + PostgreSQL com Docker Compose

No terminal, a partir da raiz do repositório:

```bash
cp .env.example .env
```

**Windows PowerShell:** use `Copy-Item .env.example .env`. Abra `.env`, substitua `ESCOLHA_UMA_SENHA_LOCAL_FORTE` por uma senha sua e **não publique o arquivo `.env`**. Em seguida:

```bash
docker compose up --build -d
docker compose ps
docker compose logs --tail=30 api
```

Testar em `http://localhost:8080/actuator/health`. Espera-se uma resposta como `{"status":"UP"}` após a inicialização. O Actuator não verifica automaticamente todas as regras de negócio. Para parar sem apagar o banco:

```bash
docker compose down
```

**Não use `docker compose down -v` se desejar preservar os dados.** O volume `postgres_data` mantém os registros ao remover os containers.

## 4. Testes automatizados (Aula 10)

```bash
mvn test
mvn clean package
```

O primeiro comando roda os testes, e o segundo limpa os artefatos anteriores, compila, executa os testes e cria `target/backend-aulas-10-11-1.0.0.jar`. **Não use `-DskipTests` para gerar as evidências da atividade.**

### Service

`TarefaServiceTest` usa `@ExtendWith(MockitoExtension.class)`, `@Mock` para o Repository e `@InjectMocks` para o Service real. Possui **4 cenários de sucesso** e **4 de erro**. Não precisa de PostgreSQL para rodar.

### Controller

`TarefaControllerTest` usa `@WebMvcTest`, `@MockitoBean` e `MockMvc` para validar HTTP 200, 201, 400 e 404 sem subir a aplicação inteira nem o PostgreSQL.

## 5. Executar JAR sem IntelliJ (Aula 11)

Se ainda não subiu o PostgreSQL, configure `.env` conforme a seção 3 e rode apenas o banco:

```bash
docker compose up -d db
```

O Compose publica a porta PostgreSQL apenas em `127.0.0.1:5432` para uso local. Configure as variáveis do processo, utilizando a **mesma senha do seu `.env`**. Exemplo **PowerShell**:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/tarefas_db"
$env:DB_USER="aluno"
$env:DB_PASSWORD="SENHA_QUE_VOCE_DEFINIU"
mvn clean package
java -jar target/backend-aulas-10-11-1.0.0.jar
```

Exemplo Bash (Linux/macOS):

```bash
export DB_URL='jdbc:postgresql://localhost:5432/tarefas_db'
export DB_USER='aluno'
export DB_PASSWORD='SENHA_QUE_VOCE_DEFINIU'
mvn clean package
java -jar target/backend-aulas-10-11-1.0.0.jar
```

Acesse `http://localhost:8080/actuator/health` e depois `/api/tarefas`. Pare o processo Java com `Ctrl+C` antes de subir também o serviço `api` no Docker, para não ocupar a mesma porta 8080.

## 6. Exercitar o CRUD

Exemplo com curl no Linux/macOS ou `curl.exe` no PowerShell:

```bash
curl -i http://localhost:8080/api/tarefas
curl -i -X POST http://localhost:8080/api/tarefas -H 'Content-Type: application/json' -d '{"titulo":"Estudar Mockito","descricao":"Preparar testes","prioridade":"ALTA","prazo":"2026-12-01"}'
curl -i http://localhost:8080/api/tarefas/1
curl -i -X PUT http://localhost:8080/api/tarefas/1 -H 'Content-Type: application/json' -d '{"titulo":"Estudar JUnit","descricao":"Refazer questões","prioridade":"MEDIA","prazo":null}'
curl -i -X PATCH http://localhost:8080/api/tarefas/1/concluir
curl -i -X DELETE http://localhost:8080/api/tarefas/1
curl -i http://localhost:8080/api/tarefas/999
```

No Windows PowerShell, a forma mais simples de enviar JSON é `Invoke-RestMethod`, por exemplo:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/tarefas -Method Post -ContentType 'application/json' -Body '{"titulo":"Estudar Mockito","descricao":"Praticar","prioridade":"ALTA"}'
```

O ID pode variar conforme o banco. Substitua o ID `1` pelo ID devolvido no POST.

## 7. Como funciona o Compose

- `db` executa PostgreSQL 16.
- `api` é construída pelo `Dockerfile` em duas etapas (Maven compila, Java 21 executa).
- A API conecta ao banco por `jdbc:postgresql://db:5432/tarefas_db`, porque **`db` é o nome de serviço na rede do Compose**. `localhost` dentro do container apontaria para o próprio container.
- `postgres_data` preserva os dados em um volume gerenciado pelo Docker.
- `depends_on.condition: service_healthy` aguarda o healthcheck do PostgreSQL antes de iniciar a API. Isto não substitui tratamento de falhas/reconexão em produção.
- Senhas são recebidas de variáveis de ambiente vindas de `.env` local, que está no `.gitignore`. Não há credenciais reais no código.

## 8. Logs e Actuator

```bash
docker compose logs --tail=60 api
curl http://localhost:8080/actuator/health
```

Os logs de negócio registram apenas o ID da tarefa criada/atualizada/excluída/concluída, evitando imprimir senha, token ou corpo integral de requisição. Só o endpoint `/actuator/health` está exposto. `UP` indica que os indicadores ativos responderam positivamente; **não garante todas as regras de negócio**.

## 9. Evidências e critérios de entrega

- Abra [`evidencias/INSTRUCOES.md`](evidencias/INSTRUCOES.md) e execute o checklist na sua máquina.
- A automação `.github/workflows/ci.yml` está preparada para executar testes, gerar JAR, iniciar a API sem IDE, subir containers e verificar HTTP/Actuator depois que for publicada no GitHub.
- **CI aprovado em 22/11/2026:** 12 testes (8 Service + 4 Controller), Maven BUILD SUCCESS, JAR executado fora da IDE, PostgreSQL, Docker Compose, health UP e tarefa persistida entre execuções. Veja [`evidencias/RESULTADOS_CI.md`](evidencias/RESULTADOS_CI.md).
- **Execução local na máquina do aluno:** ainda pendente; a CI não substitui a prática individual.
- Capturas reais devem mostrar comando e resultado, possuir legenda e não expor senhas. Não são fornecidos screenshots inventados.

## 10. Referências

A lista completa de fontes oficiais, com títulos, instituições, URLs e data de acesso, consta em `relatorio/RELATORIO.md`.
