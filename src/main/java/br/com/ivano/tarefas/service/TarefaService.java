package br.com.ivano.tarefas.service;

import br.com.ivano.tarefas.dto.TarefaRequest;
import br.com.ivano.tarefas.dto.TarefaResponse;
import br.com.ivano.tarefas.entity.Tarefa;
import br.com.ivano.tarefas.exception.RecursoNaoEncontradoException;
import br.com.ivano.tarefas.exception.RegraNegocioException;
import br.com.ivano.tarefas.repository.TarefaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TarefaService {
    private static final Logger log = LoggerFactory.getLogger(TarefaService.class);
    private final TarefaRepository repository;

    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TarefaResponse cadastrar(TarefaRequest request) {
        validarTitulo(request);
        Tarefa tarefa = new Tarefa(request.titulo().trim(), request.descricao(), request.prioridade(), request.prazo());
        Tarefa salva = repository.save(tarefa);
        log.info("Tarefa criada com id={}", salva.getId());
        return TarefaResponse.de(salva);
    }

    @Transactional(readOnly = true)
    public List<TarefaResponse> listar() {
        return repository.findAll().stream().map(TarefaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public TarefaResponse buscarPorId(Long id) {
        return TarefaResponse.de(encontrar(id));
    }

    @Transactional
    public TarefaResponse atualizar(Long id, TarefaRequest request) {
        validarTitulo(request);
        Tarefa tarefa = encontrar(id);
        tarefa.atualizar(request.titulo().trim(), request.descricao(), request.prioridade(), request.prazo());
        Tarefa salva = repository.save(tarefa);
        log.info("Tarefa atualizada com id={}", id);
        return TarefaResponse.de(salva);
    }

    @Transactional
    public TarefaResponse concluir(Long id) {
        Tarefa tarefa = encontrar(id);
        tarefa.concluir();
        Tarefa salva = repository.save(tarefa);
        log.info("Tarefa concluída com id={}", id);
        return TarefaResponse.de(salva);
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Tarefa não encontrada: " + id);
        }
        repository.deleteById(id);
        log.info("Tarefa excluída com id={}", id);
    }

    private Tarefa encontrar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new RecursoNaoEncontradoException("Tarefa não encontrada: " + id));
    }

    private void validarTitulo(TarefaRequest request) {
        if (request == null || request.titulo() == null || request.titulo().isBlank()) {
            throw new RegraNegocioException("Título não pode ficar em branco");
        }
        if (request.titulo().length() > 120) {
            throw new RegraNegocioException("Título deve ter até 120 caracteres");
        }
        if (request.prioridade() == null) {
            throw new RegraNegocioException("Prioridade é obrigatória");
        }
    }
}
