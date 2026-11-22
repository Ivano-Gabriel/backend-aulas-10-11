# Evidências reais — GitHub Actions, 22/11/2026

- Repositório: https://github.com/Ivano-Gabriel/backend-aulas-10-11
- Execução de homologação: https://github.com/Ivano-Gabriel/backend-aulas-10-11/actions/runs/45077886157
- Pacote: https://github.com/Ivano-Gabriel/backend-aulas-10-11/actions/runs/45077886157/artifacts/14775643850
- **Resultado: SUCESSO** para todas as etapas do job `homologacao`.

## Testes automatizados

- `TarefaServiceTest`: 8 aprovados, 0 falhas, 0 erros, 0 ignorados.
- `TarefaControllerTest`: 4 aprovados, 0 falhas, 0 erros, 0 ignorados.
- Total: **12/12 testes aprovados**; Maven exibiu `BUILD SUCCESS`.
- A etapa Docker também executou `mvn clean package`, com 12 testes aprovados.

## Infraestrutura e observabilidade

O GitHub Actions comprovou PostgreSQL, execução do JAR com Java fora da IDE, cadastro e busca HTTP, health Actuator `UP`, build e execução via Docker Compose e recuperação de uma tarefa gravada anteriormente pelo JAR no mesmo banco de dados.

## Limitações

Os resultados são do runner Linux no GitHub Actions, **não da máquina do aluno**. Screenshots locais ainda devem ser produzidos. A execução original exibiu uma senha temporária de banco nas variáveis mostradas pelo runner, sem relação com produção; o workflow foi corrigido com `::add-mask::` para a execução seguinte.
