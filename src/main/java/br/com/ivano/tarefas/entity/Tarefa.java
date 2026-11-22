package br.com.ivano.tarefas.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tarefas")
public class Tarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridade prioridade;

    @Column(nullable = false)
    private boolean concluida;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    private LocalDate prazo;

    protected Tarefa() {
    }

    public Tarefa(String titulo, String descricao, Prioridade prioridade, LocalDate prazo) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.prazo = prazo;
        this.concluida = false;
        this.criadaEm = Instant.now();
    }

    @PrePersist
    void prepararData() {
        if (criadaEm == null) {
            criadaEm = Instant.now();
        }
    }

    public void atualizar(String titulo, String descricao, Prioridade prioridade, LocalDate prazo) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.prazo = prazo;
    }

    public void concluir() {
        this.concluida = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public Prioridade getPrioridade() { return prioridade; }
    public boolean isConcluida() { return concluida; }
    public Instant getCriadaEm() { return criadaEm; }
    public LocalDate getPrazo() { return prazo; }
}
