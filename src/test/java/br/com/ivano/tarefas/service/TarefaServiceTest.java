package br.com.ivano.tarefas.service;

import br.com.ivano.tarefas.dto.TarefaRequest;
import br.com.ivano.tarefas.dto.TarefaResponse;
import br.com.ivano.tarefas.entity.Prioridade;
import br.com.ivano.tarefas.entity.Tarefa;
import br.com.ivano.tarefas.exception.RecursoNaoEncontradoException;
import br.com.ivano.tarefas.exception.RegraNegocioException;
import br.com.ivano.tarefas.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository repository;

    @InjectMocks
    private TarefaService service;

    // SUCESSO 1: demonstra Arrange, Act, Assert, when e verify.
    @Test
    void deveCadastrarTarefaValida() {
        // ARRANGE: cria o pedido e ensina o mock a devolver uma entidade com ID.
        TarefaRequest request = new TarefaRequest("Estudar JUnit", "Fazer exercícios", Prioridade.ALTA, LocalDate.of(2026, 12, 1));
        when(repository.save(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa entidade = invocation.getArgument(0);
            entidade.setId(1L);
            return entidade;
        });

        // ACT: executa a regra de negócio real, sem PostgreSQL.
        TarefaResponse resultado = service.cadastrar(request);

        // ASSERT: verifica o resultado e a interação com o Repository.
        assertEquals(1L, resultado.id());
        assertEquals("Estudar JUnit", resultado.titulo());
        assertEquals(Prioridade.ALTA, resultado.prioridade());
        assertFalse(resultado.concluida());
        verify(repository, times(1)).save(any(Tarefa.class));
    }

    // SUCESSO 2
    @Test
    void deveBuscarTarefaExistente() {
        Tarefa tarefa = new Tarefa("Revisar API", "Testes", Prioridade.MEDIA, null);
        tarefa.setId(8L);
        when(repository.findById(8L)).thenReturn(Optional.of(tarefa));

        TarefaResponse resultado = service.buscarPorId(8L);

        assertEquals("Revisar API", resultado.titulo());
        verify(repository).findById(8L);
    }

    // SUCESSO 3
    @Test
    void deveConcluirTarefa() {
        Tarefa tarefa = new Tarefa("Ler Docker", "", Prioridade.BAIXA, null);
        tarefa.setId(4L);
        when(repository.findById(4L)).thenReturn(Optional.of(tarefa));
        when(repository.save(any(Tarefa.class))).thenAnswer(i -> i.getArgument(0));

        TarefaResponse resultado = service.concluir(4L);

        assertTrue(resultado.concluida());
        verify(repository).save(tarefa);
    }

    // SUCESSO 4
    @Test
    void deveListarTarefas() {
        Tarefa tarefa = new Tarefa("Ler livro", "Capítulo 3", Prioridade.MEDIA, null);
        when(repository.findAll()).thenReturn(List.of(tarefa));

        List<TarefaResponse> resultado = service.listar();

        assertEquals(1, resultado.size());
        assertEquals("Ler livro", resultado.get(0).titulo());
    }

    // ERRO 1
    @Test
    void deveFalharAoBuscarTarefaInexistente() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(999L));
        verify(repository).findById(999L);
    }

    // ERRO 2
    @Test
    void deveRejeitarTituloEmBrancoSemSalvar() {
        TarefaRequest request = new TarefaRequest("  ", "Sem título", Prioridade.MEDIA, null);

        assertThrows(RegraNegocioException.class, () -> service.cadastrar(request));
        verify(repository, never()).save(any(Tarefa.class));
    }

    // ERRO 3
    @Test
    void deveFalharAoAtualizarRegistroInexistente() {
        when(repository.findById(12L)).thenReturn(Optional.empty());
        TarefaRequest request = new TarefaRequest("Atualizar", "", Prioridade.ALTA, null);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.atualizar(12L, request));
        verify(repository, never()).save(any());
    }

    // ERRO 4
    @Test
    void deveFalharAoExcluirRegistroInexistente() {
        when(repository.existsById(9L)).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.excluir(9L));
        verify(repository, never()).deleteById(any());
    }
}
