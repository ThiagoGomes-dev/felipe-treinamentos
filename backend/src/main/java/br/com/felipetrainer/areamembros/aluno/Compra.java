package br.com.felipetrainer.areamembros.aluno;

import br.com.felipetrainer.areamembros.conteudo.Curso;
import jakarta.persistence.*;
import java.time.Instant;

/** Venda recebida da Eduzz. A transação é única, então o mesmo aviso não cria acesso duas vezes. */
@Entity
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    private Curso curso;

    private String transacao;
    private String status;
    private Instant atualizadaEm = Instant.now();

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
    public String getTransacao() { return transacao; }
    public void setTransacao(String transacao) { this.transacao = transacao; }
    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.atualizadaEm = Instant.now();
    }
    public Instant getAtualizadaEm() { return atualizadaEm; }
}
