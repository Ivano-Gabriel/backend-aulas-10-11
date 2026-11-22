# Trabalho Individual de Pesquisa e Aplicação — Aulas 10 e 11

**Título:** Testes automatizados, aplicação executável, Docker e observabilidade em uma API Spring Boot  
**Aluno:** Ivano Gabriel Silva Melo  
**Curso:** Ciência da Computação — 4º período  
**Disciplina:** Back-End Frameworks  
**Data:** 22 de novembro de 2026  
**Repositório individual:** https://github.com/Ivano-Gabriel/backend-aulas-10-11  
**Orientação de uso:** este texto é um material de trabalho para o aluno revisar, compreender, adaptar e validar em seu ambiente antes da entrega.

## 1. Introdução

Uma API que funciona apenas na máquina de quem a desenvolveu ainda depende de condições difíceis de reproduzir: versões instaladas, banco local, configurações privadas e validação manual. Para evoluir, ela precisa de verificações repetíveis, empacotamento independente da IDE, configuração por ambiente e mecanismos de acompanhamento.

A implementação estudada é a **API de Tarefas**, uma API REST em Java 21 e Spring Boot com Spring Data JPA, PostgreSQL, JUnit, Mockito, MockMvc, Docker Compose e Actuator. Ela permite cadastrar, listar, buscar, atualizar, concluir e excluir tarefas. Uma tarefa possui `id`, `titulo`, `descricao`, `prioridade`, `concluida`, `criadaEm` e `prazo`.

**Questão central:** o que falta a uma API Spring Boot para se tornar confiável, testável e executável em outros ambientes? É preciso separar responsabilidades, automatizar verificações, empacotar o programa, manter segredos fora do código, executar com dependências reproduzíveis e observar sua saúde operacional. Ainda são necessárias práticas de segurança, evidências reais de teste e documentação de execução.

> **Distinção metodológica:** as respostas conceituais das seções 2 e 4 baseiam-se nas referências técnicas citadas. As seções de implementação descrevem os arquivos que foram preparados. A execução de testes, do JAR, dos containers e do Actuator **ainda não foi comprovada nesta sessão** e permanece como etapa obrigatória de homologação, descrita na seção 9.

## 2. Parte I — Pesquisa sobre testes automatizados (12 questões)

**1. O que é um teste automatizado?**  
É um programa que executa uma funcionalidade e compara o resultado observado com o esperado. Na API, um teste cria uma tarefa e confere se o Service devolve título e identificador corretos.

**2. Qual a diferença entre teste manual e automatizado?**  
No teste manual, alguém usa Postman, navegador ou terminal e confere os dados. No automatizado, uma rotina executa as mesmas verificações sempre que necessário. O teste manual ajuda na exploração; o automatizado evita repetir verificações de regressão manualmente.

**3. O que são Arrange, Act e Assert?**  
São três momentos para organizar um teste: *Arrange* (preparar cenário e dependências), *Act* (executar a ação) e *Assert* (comparar a saída com o esperado). No teste `deveCadastrarTarefaValida`, eles estão sinalizados por comentários.

**4. Qual é a função do JUnit?**  
Oferecer estrutura para criar e executar casos de teste Java, com `@Test` e asserções como `assertEquals`, `assertTrue` e `assertThrows`. Ele define se um comportamento passou ou falhou segundo as condições declaradas no teste.

**5. Qual é a função do Mockito?**  
Permitir a criação e configuração de objetos simulados para substituir dependências da classe testada. Por exemplo, faz um `TarefaRepository` responder sem conectar ao PostgreSQL e permite conferir chamadas com `verify`.

**6. O que é um Mock?**  
É uma dependência simulada e controlada dentro do teste. No projeto, `repository` é um mock que representa a camada de persistência enquanto o `TarefaService` continua sendo real.

**7. Por que um teste unitário de Service não deveria depender diretamente do PostgreSQL?**  
Porque o objetivo é isolar regras de negócio: conexão, dados preexistentes e disponibilidade do banco tornam esse teste mais lento e menos previsível. Testes de integração separados devem validar a comunicação real com PostgreSQL.

**8. Qual a diferença entre `@Mock` e `@InjectMocks`?**  
`@Mock` cria o objeto simulado (`TarefaRepository`). `@InjectMocks` cria o objeto testado (`TarefaService`) e tenta injetar nele os mocks disponíveis, geralmente pelo construtor.

**9. Para que servem `when(...).thenReturn(...)`, `verify(...)` e `assertThrows(...)`?**  
`when(...).thenReturn(...)` define uma resposta do mock; `verify(...)` verifica a interação com uma dependência; `assertThrows(...)` confirma que uma ação dispara a exceção esperada. A API usa `thenAnswer(...)` em um teste de cadastro para devolver a entidade salva com ID simulado, além de `thenReturn(...)` em buscas.

**10. Diferença entre teste de Service e teste de Controller?**  
O de Service examina regras e decisões de negócio; o de Controller verifica contrato HTTP, como rotas, serialização JSON, status 201/400/404 e validação de parâmetros. São recortes diferentes, complementares.

**11. Para que serve o MockMvc?**  
Simula requisições HTTP dentro do ambiente de teste Spring MVC, permitindo testar resposta e status sem iniciar um servidor HTTP real nem usar Postman. A classe `TarefaControllerTest` usa MockMvc.

**12. Por que 100% de cobertura não garante correção?**  
Cobertura mede que linhas ou ramos foram exercitados, não se as asserções são adequadas nem se casos importantes foram previstos. Um teste pode passar por toda a função sem testar uma falha relevante.

### 2.1. Exemplo obrigatório comentado — Teste de Service

Este exemplo é retirado da estrutura desenvolvida em `src/test/java/br/com/ivano/tarefas/service/TarefaServiceTest.java` e reduzido aqui apenas para destacar a sequência do teste:

```java
@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository repository; // Dependência simulada.

    @InjectMocks
    private TarefaService service; // Service real que recebe o mock.

    @Test
    void deveCadastrarTarefaValida() {
        // Arrange: preparar dados e comportamento do Repository.
        TarefaRequest entrada = new TarefaRequest(
            "Estudar JUnit", "Fazer exercícios", Prioridade.ALTA, null
        );
        when(repository.save(any(Tarefa.class))).thenAnswer(chamada -> {
            Tarefa tarefa = chamada.getArgument(0);
            tarefa.setId(1L);
            return tarefa;
        });

        // Act: executar a regra de negócio.
        TarefaResponse resultado = service.cadastrar(entrada);

        // Assert: conferir o retorno e a chamada ao Repository.
        assertEquals("Estudar JUnit", resultado.titulo());
        assertEquals(1L, resultado.id());
        verify(repository).save(any(Tarefa.class));
    }
}
```

**Explicação:** a extensão inicializa o Mockito no JUnit 5. `@Mock` evita que a gravação dependa do banco; `@InjectMocks` fornece esse Repository ao Service. No Arrange, o mock aceita `save` e devolve o objeto com ID de exemplo. O Act chama `cadastrar`; o Assert compara campos da resposta e usa `verify` para demonstrar que o Service pediu a gravação. O exemplo não prova que o PostgreSQL salvou algo: isso exige teste de integração.

### 2.2. Estratégia de testes da API

| Camada | Cenário | Ferramenta | Arquivo |
|---|---|---|---|
| Service | Cadastrar tarefa válida | JUnit + Mockito | `TarefaServiceTest` |
| Service | Buscar tarefa existente | JUnit + Mockito | `TarefaServiceTest` |
| Service | Concluir e listar | JUnit + Mockito | `TarefaServiceTest` |
| Service | Buscar/atualizar/excluir inexistente | `assertThrows` | `TarefaServiceTest` |
| Service | Recusar título vazio | `assertThrows` + `verify(never())` | `TarefaServiceTest` |
| Controller | Responder 200 e 201 | MockMvc | `TarefaControllerTest` |
| Controller | Responder 400 e 404 | MockMvc | `TarefaControllerTest` |

Há **12 métodos de teste escritos**: oito de Service e quatro de Controller. O número de testes **aprovados ainda não foi apurado pela execução do Maven**.

## 3. Aplicação prática — API Spring Boot

### 3.1. Estrutura e persistência

O `TarefaController` recebe as requisições e delega ao `TarefaService`; o Service aplica validações e consulta o `TarefaRepository`; este estende `JpaRepository<Tarefa, Long>` para persistir a entidade `Tarefa` no PostgreSQL. Essa divisão facilita isolar a camada de regras e simular a persistência nos testes.

### 3.2. Operações implementadas

| Operação | Endpoint | Resposta planejada |
|---|---|---|
| Cadastro | `POST /api/tarefas` | `201 Created` |
| Listagem | `GET /api/tarefas` | `200 OK` |
| Busca por ID | `GET /api/tarefas/{id}` | `200 OK` ou `404 Not Found` |
| Atualização | `PUT /api/tarefas/{id}` | `200 OK` ou `404 Not Found` |
| Conclusão | `PATCH /api/tarefas/{id}/concluir` | `200 OK` ou `404 Not Found` |
| Exclusão | `DELETE /api/tarefas/{id}` | `204 No Content` ou `404 Not Found` |

Exemplo de JSON enviado no cadastro:

```json
{
  "titulo": "Revisar Docker",
  "descricao": "Estudar containers e Compose",
  "prioridade": "ALTA",
  "prazo": "2026-12-01"
}
```

`TarefaRequest` usa Bean Validation (`@NotBlank`, `@NotNull`, `@Size`). `ApiExceptionHandler` traduz erro de validação para 400 e recurso inexistente para 404. `TarefaResponse` evita devolver a entidade diretamente.

## 4. Parte II — Build, configuração externa, Docker e observabilidade (16 questões)

**1. O que significa fazer build?**  
Transformar código e recursos em artefatos executáveis ou distribuíveis, normalmente passando por compilação e testes. Nosso artefato final será um JAR.

**2. O que `mvn clean package` executa?**  
`clean` remove artefatos anteriores (como `target/`); `package` percorre as fases necessárias até empacotar, incluindo compilar e executar testes comuns. O Spring Boot Maven Plugin prepara o JAR executável. Se os testes falharem, o build normalmente falha também.

**3. O que é um arquivo JAR?**  
É um arquivo Java Archive que reúne classes e recursos. No Spring Boot, o JAR executável também inclui as dependências e o carregador necessário para iniciar a aplicação, mas não dispensa a presença de um runtime Java compatível.

**4. Como executar Spring Boot sem IntelliJ?**  
Com `java -jar target/backend-aulas-10-11-1.0.0.jar`, após gerar o JAR com Maven e configurar o banco. A IDE não é necessária para iniciar o processo.

**5. Por que não colocar senhas ou segredos no código?**  
Porque arquivos commitados podem circular entre pessoas e sistemas, além de permanecerem no histórico Git. Credenciais devem ser fornecidas por ambiente; o projeto publica somente `.env.example` com exemplo não secreto.

**6. Como variáveis de ambiente ajudam?**  
Elas permitem mudar usuário, senha, endereço do banco e porta sem editar Java. `application.properties` usa `${DB_URL}`, `${DB_USER}` e `${DB_PASSWORD}`.

**7. O que são profiles do Spring?**  
Perfis ativam configurações específicas de ambientes ou condições. Pode-se usar `application-dev.properties` para desenvolvimento e `application-prod.properties` para produção e selecionar com `SPRING_PROFILES_ACTIVE=prod`. A API da atividade usa um único arquivo com variáveis externas, sem precisar criar perfis adicionais.

**8. Diferença entre imagem Docker e container?**  
Imagem é um pacote imutável de aplicação e ambiente de execução; container é uma instância criada a partir da imagem, que pode estar em execução ou parada.

**9. Função de `FROM`, `WORKDIR`, `COPY`, `ENTRYPOINT`?**  
`FROM` escolhe a base; `WORKDIR` define a pasta de trabalho; `COPY` transfere arquivos para a imagem; `ENTRYPOINT` fixa o comando inicial. O Dockerfile da atividade utiliza uma etapa de Maven e outra etapa apenas com Java 21.

**10. Para que serve Docker Compose?**  
Descreve múltiplos serviços e suas relações em YAML, como a API e o PostgreSQL, facilitando iniciar ou parar toda a solução com `docker compose up` ou `docker compose down`.

**11. Por que a API em container não deve usar `localhost` para outro container?**  
Porque `localhost` aponta para o próprio container. Na rede do Compose, a API deve acessar o PostgreSQL pelo nome do serviço `db`, usando `jdbc:postgresql://db:5432/tarefas_db`.

**12. Para que servem volumes Docker?**  
Mantêm dados fora da camada efêmera de um container. O volume nomeado `postgres_data` preserva registros do banco mesmo quando containers são removidos, desde que não se apague o volume.

**13. Qual a função dos logs?**  
Registrar eventos de execução úteis para identificar falhas, operações e comportamento operacional. A API registra em nível INFO os IDs de tarefas criadas, atualizadas, concluídas e excluídas.

**14. Quais informações não devem aparecer nos logs?**  
Senhas, tokens, segredos, dados bancários, chaves de API e dados pessoais desnecessários. Também não se deve registrar indiscriminadamente corpos de requisições ou exceções com dados sensíveis.

**15. Para que serve o Spring Boot Actuator?**  
Fornece endpoints de monitoramento e gerenciamento, como saúde da aplicação, métricas e informações operacionais. A configuração desta API expõe somente `health` na web.

**16. O que representa a resposta `/actuator/health`?**  
A condição agregada dos indicadores de saúde ativos. `{"status":"UP"}` indica que esses indicadores estão saudáveis naquele momento, mas não prova que todos os fluxos de negócio estejam corretos.

### 4.1. Exemplo obrigatório comentado — Dockerfile

O Dockerfile da API é multistage, permitindo a construção e a execução em ambientes separados:

```dockerfile
# Compila e executa os testes com Maven e JDK 21.
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package

# Imagem final contém Java 21, não Maven.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/backend-aulas-10-11-1.0.0.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Explicação:** a primeira etapa baixa as dependências e gera o pacote com Maven; a segunda leva apenas o JAR para uma imagem com Java Runtime 21. `USER 10001` evita execução como root; `EXPOSE 8080` documenta a porta interna; `ENTRYPOINT` inicia a API. Não há senha inserida na imagem.

### 4.2. Exemplo obrigatório — Docker Compose com PostgreSQL

O `compose.yaml` define dois serviços (`db` e `api`). O trecho a seguir mostra as partes essenciais:

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: tarefas_db
      POSTGRES_USER: ${DB_USER:?Configure DB_USER no .env}
      POSTGRES_PASSWORD: ${DB_PASSWORD:?Configure DB_PASSWORD no .env}
    volumes:
      - postgres_data:/var/lib/postgresql/data

  api:
    build: .
    environment:
      DB_URL: jdbc:postgresql://db:5432/tarefas_db
      DB_USER: ${DB_USER:?Configure DB_USER no .env}
      DB_PASSWORD: ${DB_PASSWORD:?Configure DB_PASSWORD no .env}
    depends_on:
      db:
        condition: service_healthy

volumes:
  postgres_data:
```

**Explicação:** `image` escolhe PostgreSQL 16; `build: .` cria a imagem da API; `environment` fornece as configurações externas; `db` identifica o banco na rede dos serviços; `postgres_data` persiste os arquivos do banco. O Compose real também define portas e healthcheck via `pg_isready`, necessário para satisfazer o `service_healthy` declarado acima. Para iniciar: `docker compose up --build -d`; para acompanhar: `docker compose logs api`; para parar sem apagar os dados: `docker compose down`.

### 4.3. Configuração aplicada

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/tarefas_db}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=never
```

A URL padrão usa `localhost` apenas **quando a aplicação roda fora dos containers**, diretamente no computador. No Compose, `DB_URL` substitui esse valor pelo host `db`. `show-details=never` reduz exposição de detalhes operacionais. A implementação usa `spring.jpa.hibernate.ddl-auto=update` por simplicidade didática; em produção, o ideal é avaliar migrações controladas de esquema.

## 5. Estudo de caso — API que só funciona no computador do aluno

A API fictícia falha em outras máquinas porque depende de configurações locais, não possui testes, traz credenciais no repositório e não apresenta uma forma padronizada de execução e monitoramento.

| Solução proposta | Problema resolvido | Aplicação na API de tarefas | Cuidados |
|---|---|---|---|
| Testes unitários no Service | Regressões de regra passam despercebidas | JUnit/Mockito simulam o Repository, cenários de sucesso e falha | Não usar mock como prova de persistência real |
| Testes do Controller | Respostas HTTP podem estar incorretas | MockMvc verifica 200, 201, 400 e 404 | Validar rotas e mensagens, sem supor regras de negócio cobertas |
| Build e JAR | Dependência da IDE | `mvn clean package` e `java -jar ...` | Usar Java compatível e não ignorar falhas de teste |
| Variáveis de ambiente | Senhas expostas e configuração rígida | `DB_URL`, `DB_USER`, `DB_PASSWORD` em `application.properties` | Não versionar `.env`, não imprimir senhas |
| Dockerfile | Diferenças na instalação do Java | Duas etapas, build Maven e runtime Java 21 | Base oficial, imagem reduzida, usuário não root |
| Docker Compose | Inicialização manual de API/banco | Serviços `api` e `db`, hostname `db` | Aguardar banco ficar pronto; considerar limites de recursos |
| Volume PostgreSQL | Dados somem ao remover container | Volume nomeado `postgres_data` | `down -v` apaga volume; backups continuam necessários |
| Logs | Falta de rastreabilidade | Registra operações por ID | Não registrar credenciais ou dados pessoais desnecessários |
| Actuator | Ninguém consegue verificar a saúde automaticamente | `GET /actuator/health` | Expor apenas o necessário; UP não garante regras de negócio |

**Resultado da proposta:** o projeto passa a possuir meios técnicos para teste automatizado e execução reproduzível. A comprovação final depende de executá-lo e registrar as evidências. O exercício não exige autenticação, microsserviços, nuvem ou cobertura integral.

## 6. Questões de verificação da aprendizagem

1. **Qual dependência deve ser simulada no teste de Service?** O `TarefaRepository`, porque conecta a lógica à persistência.
2. **O que acontece se o Mock não tiver comportamento configurado?** Em geral, Mockito devolve valores padrão (`null`, `false`, zero e, em muitos casos, `Optional.empty()` ou coleções vazias, conforme o tipo e a versão), o que pode impedir o fluxo esperado; por isso configuramos o comportamento necessário ao cenário.
3. **`assertEquals` x `verify`?** `assertEquals` compara valores; `verify` confere interações com dependências simuladas.
4. **Teste unitário deve iniciar toda a aplicação?** Não; o Service pode ser testado sem contexto Spring e sem PostgreSQL.
5. **O que o Maven coloca em `target`?** Classes compiladas, relatórios de testes, artefatos e o JAR resultante, entre outros arquivos de build.
6. **Por que o JAR executa sem IDE?** Porque o arquivo é um pacote executável Spring Boot iniciado pelo runtime Java.
7. **Por que não colocar segredos no Dockerfile?** Porque acabam em camadas/metadados de build e podem vazar pelo repositório ou por imagens distribuídas.
8. **O que é `localhost` dentro de `api`?** O próprio container da API, não o `db`.
9. **Parar x remover container?** Parar interrompe sua execução mantendo o container existente; remover apaga a instância, mas não necessariamente imagens nem volumes independentes.
10. **Sem volume, o que acontece com o banco ao remover container?** Os dados guardados apenas no filesystem daquele container não são preservados de modo confiável para outro container.
11. **`UP` comprova regras de negócio?** Não. É saúde dos indicadores, não validação funcional completa.
12. **Por que configurar cuidadosamente o Actuator?** Para não publicar detalhes internos, métricas ou endpoints administrativos em redes que não deveriam acessá-los.

## 7. Conclusão

Testar e empacotar uma API significa deixar de depender exclusivamente de observações manuais ou das configurações do computador do programador. A API de tarefas apresenta arquitetura em camadas, CRUD persistente, testes isolados com Mockito, casos HTTP com MockMvc, JAR, Dockerfile, Compose com banco e volume, logs sem dados sensíveis e Actuator com exposição restrita. Esses recursos tornam a aplicação mais verificável e portátil, mas a confiabilidade efetiva só poderá ser afirmada após a execução real e revisão dos resultados.

## 8. Referências consultadas

1. **SPRING.** *Testing Spring Boot Applications*. Spring Boot Reference Documentation. Disponível em: https://docs.spring.io/spring-boot/3.5/reference/testing/spring-boot-applications.html. Acesso em: 22 nov. 2026.
2. **SPRING.** *Externalized Configuration*. Spring Boot Reference Documentation. Disponível em: https://docs.spring.io/spring-boot/3.5/reference/features/external-config.html. Acesso em: 22 nov. 2026.
3. **SPRING.** *Profiles*. Spring Boot Reference Documentation. Disponível em: https://docs.spring.io/spring-boot/3.5/reference/features/profiles.html. Acesso em: 22 nov. 2026.
4. **SPRING.** *Endpoints*. Spring Boot Actuator Reference. Disponível em: https://docs.spring.io/spring-boot/3.5/reference/actuator/endpoints.html. Acesso em: 22 nov. 2026.
5. **SPRING DATA.** *Defining Repository Interfaces*. Spring Data JPA Reference. Disponível em: https://docs.spring.io/spring-data/jpa/reference/repositories/definition.html. Acesso em: 22 nov. 2026.
6. **JUNIT TEAM.** *JUnit 5 User Guide*. Disponível em: https://docs.junit.org/5.13.1/user-guide/index.html. Acesso em: 22 nov. 2026.
7. **MOCKITO CONTRIBUTORS.** *Mockito API Documentation*. Disponível em: https://javadoc.io/static/org.mockito/mockito-core/5.24.0/org.mockito/org/mockito/Mockito.html. Acesso em: 22 nov. 2026.
8. **APACHE SOFTWARE FOUNDATION.** *Introduction to the Build Lifecycle*. Maven. Disponível em: https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html. Acesso em: 22 nov. 2026.
9. **DOCKER INC.** *Compose Quickstart*. Disponível em: https://docs.docker.com/compose/gettingstarted/. Acesso em: 22 nov. 2026.
10. **DOCKER INC.** *Define and manage volumes in Docker Compose*. Disponível em: https://docs.docker.com/reference/compose-file/volumes/. Acesso em: 22 nov. 2026.
11. **DOCKER INC.** *Set environment variables within your container's environment*. Disponível em: https://docs.docker.com/compose/how-tos/environment-variables/set-environment-variables/. Acesso em: 22 nov. 2026.
12. **DOCENTE DA DISCIPLINA.** *Aula 10 — Testes em Aplicações Spring Boot*; *Aula 11 — Aplicação Executável, Docker e Actuator*. Materiais de aula fornecidos à turma, 2026.

## 9. Repositório e evidências de execução

**Endereço:** https://github.com/Ivano-Gabriel/backend-aulas-10-11 — repositório criado, publicação dos arquivos feita em 22/11/2026; execução dos testes e da API ainda depende de homologação.

**Situação de verificação em 22/11/2026:**

| Comprovação exigida | Situação nesta versão | Onde executar/registrar |
|---|---|---|
| Código com CRUD e PostgreSQL | Arquivos preparados, execução ainda não verificada | `src/main`, README |
| JUnit/Mockito + dois sucessos/dois erros | 8 testes de Service escritos, não executados | `src/test`, `mvn clean package` |
| Controller/MockMvc | 4 testes HTTP escritos, não executados | `TarefaControllerTest` |
| JAR fora da IDE | Comando documentado, não executado | `java -jar target/...jar` |
| Dockerfile + Compose + volume | Arquivos preparados, containers não executados | `Dockerfile`, `compose.yaml` |
| Logs | Código configurado, logs de execução ainda não capturados | `docker compose logs api` |
| `GET /actuator/health` | Endpoint configurado, resposta não observada ainda | `curl http://localhost:8080/actuator/health` |

**Evidências reais para completar a entrega:** capturas das execuções de Maven, JAR, `docker compose ps`, logs, health e teste de persistência. Cada captura deve conter uma legenda, como as indicadas no arquivo `evidencias/INSTRUCOES.md`. Uma execução automatizada adicional foi especificada em `.github/workflows/ci.yml`, mas seu resultado só contará como evidência depois de a pipeline efetivamente rodar e concluir.
