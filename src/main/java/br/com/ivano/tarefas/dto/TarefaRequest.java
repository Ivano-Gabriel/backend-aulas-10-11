package br.com.ivano.tarefas.dto;

import br.com.ivano.tarefas.entity.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TarefaRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 120, message = "Título deve ter até 120 caracteres")
        String titulo,
        @Size(max = 500, message = "Descrição deve ter até 500 caracteres")
        String descricao,
        @NotNull(message = "Prioridade é obrigatória")
        Prioridade prioridade,
        LocalDate prazo
) {}
