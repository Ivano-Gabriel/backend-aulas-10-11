package br.com.ivano.tarefas.dto;

import br.com.ivano.tarefas.entity.Prioridade;
import br.com.ivano.tarefas.entity.Tarefa;
import java.time.Instant;
import java.time.LocalDate;

public record TarefaResponse(
        Long id,
        String titulo,
        String descricao,
        Prioridade prioridade,
        boolean concluida,
        Instant criadaEm,
        LocalDate prazo
) {
    public static TarefaResponse de(Tarefa tarefa) {
        return new TarefaResponse(
                tarefa.getId(), tarefa.getTitulo(), tarefa.getDescricao(),
                tarefa.getPrioridade(), tarefa.isConcluida(), tarefa.getCriadaEm(), tarefa.getPrazo());
    }
}
