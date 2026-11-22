package br.com.ivano.tarefas.controller;

import br.com.ivano.tarefas.dto.TarefaResponse;
import br.com.ivano.tarefas.entity.Prioridade;
import br.com.ivano.tarefas.exception.RecursoNaoEncontradoException;
import br.com.ivano.tarefas.service.TarefaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TarefaController.class)
class TarefaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarefaService service;

    @Test
    void deveResponder200NaListagem() throws Exception {
        when(service.listar()).thenReturn(List.of(new TarefaResponse(1L, "Estudar", "JUnit", Prioridade.ALTA, false, Instant.parse("2026-11-22T12:00:00Z"), null)));

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Estudar"));
    }

    @Test
    void deveResponder404QuandoIdNaoExiste() throws Exception {
        when(service.buscarPorId(999L)).thenThrow(new RecursoNaoEncontradoException("Tarefa não encontrada: 999"));

        mockMvc.perform(get("/api/tarefas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Tarefa não encontrada: 999"));
    }

    @Test
    void deveResponder400ComTituloInvalido() throws Exception {
        mockMvc.perform(post("/api/tarefas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"\",\"descricao\":\"\",\"prioridade\":\"MEDIA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void deveResponder201AoCadastrar() throws Exception {
        when(service.cadastrar(any())).thenReturn(new TarefaResponse(17L, "Estudar", "", Prioridade.MEDIA, false, Instant.parse("2026-11-22T12:00:00Z"), null));

        mockMvc.perform(post("/api/tarefas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Estudar\",\"descricao\":\"\",\"prioridade\":\"MEDIA\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tarefas/17"))
                .andExpect(jsonPath("$.id").value(17));
    }
}
