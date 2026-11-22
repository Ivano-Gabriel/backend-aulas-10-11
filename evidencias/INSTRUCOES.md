# Evidências de execução — preencher somente após testar

**Estado em 22/11/2026:** implementação preparada, mas **testes, JAR, Docker e Actuator ainda não foram executados neste ambiente**. Não usar este arquivo como prova de que as verificações passaram.

## Checklist para a entrega

- [ ] `mvn clean package` terminou com BUILD SUCCESS e **sem pular testes**; registrar captura do terminal com data e comando, e relatório `target/surefire-reports`.
- [ ] `java -jar target/backend-aulas-10-11-1.0.0.jar` iniciou fora do IntelliJ; registrar terminal com mensagem de inicialização.
- [ ] `docker compose up --build -d` retornou containers operacionais; registrar `docker compose ps`.
- [ ] `docker compose logs --tail=50 api` mostrou logs de funcionamento sem expor senha/token.
- [ ] `GET http://localhost:8080/actuator/health` retornou HTTP 200 e `{"status":"UP"}`; registrar saída real do terminal.
- [ ] Realizar cadastro/listagem/busca/atualização/exclusão, capturando status HTTP e IDs.
- [ ] Persistência: criar tarefa, executar `docker compose down`, subir novamente e recuperar a tarefa. **Não remover o volume** antes do teste.
- [ ] Registrar no relatório a data, SO, versões Java/Maven/Docker, números reais de testes executados/aprovados/falhos.

## Legendas sugeridas (somente após captura)

- Figura 1 — Execução real dos testes automatizados via Maven.
- Figura 2 — Execução do JAR fora da IDE.
- Figura 3 — Containers da API e PostgreSQL em funcionamento.
- Figura 4 — Resposta HTTP 200 do endpoint `/actuator/health`.
- Figura 5 — Persistência de registro após reiniciar os containers.

### Segurança

Antes de enviar capturas, oculte credenciais de banco, tokens, paths privados e outros dados pessoais. Não fabricar resultados de teste ou alterar manualmente logs para parecer sucesso.
